/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.declaration

import config.AppConfig
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import forms.declaration.ConsignmentReferences
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.submissionMetric
import models.DeclarationFormats._
import models.declaration.SupplementaryDeclarationData
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{CustomsCacheService, ExportsItemsCacheIds, NRSService}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, MetaData}
import views.html.declaration.summary.{summary_page, summary_page_no_data}

import scala.concurrent.{ExecutionContext, Future}

class SummaryPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction, journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  exportsMetrics: ExportsMetrics,
  nrsService: NRSService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  implicit val appConfigImpl: AppConfig = appConfig

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetch(cacheId).map {
      case Some(cacheMap) if containsMandatoryData(cacheMap) => Ok(summary_page(SupplementaryDeclarationData(cacheMap)))
      case _                                                 => Ok(summary_page_no_data())
    }
  }

  private def containsMandatoryData(cacheMap: CacheMap): Boolean =
    cacheMap.getEntry[ConsignmentReferences](ConsignmentReferences.id).exists(_.lrn.nonEmpty)

  def submitSupplementaryDeclaration(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetch(cacheId).flatMap {
      case Some(cacheMap) =>
        exportsMetrics.startTimer(submissionMetric)
        val suppDecData = SupplementaryDeclarationData(cacheMap)
        val metaData = createMetaData(cacheMap, suppDecData)
        val ducr = suppDecData.consignmentReferences.flatMap(_.ducr)
        val lrn = suppDecData.consignmentReferences.map(_.lrn)

        customsDeclareExportsConnector
          .submitExportDeclaration(ducr.fold("")(_.ducr), lrn, metaData)
          .flatMap {
            case HttpResponse(ACCEPTED, _, _, _) =>
              customsCacheService.remove(cacheId).map { _ =>
                Redirect(controllers.declaration.routes.ConfirmationPageController.displayPage())
                  .flashing(prepareFlashScope(lrn.getOrElse("")))
              }
            case error =>
              Future.successful(handleError(s"Error from Customs Declarations API ${error.toString}"))
          }

      case None =>
        Future.successful(handleError(s"Could not obtain data from DB"))
    }
  }

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    Logger.error(logMessage)
    InternalServerError(
      errorHandler.standardErrorTemplate(
        pageTitle = messagesApi("global.error.title"),
        heading = messagesApi("global.error.heading"),
        message = messagesApi("global.error.message")
      )
    )
  }

  private def prepareFlashScope(lrn: String) =
    Flash(Map("LRN" -> lrn))

  //TODO : refactor to handle large data collection and move the logic of metadata creation to separate service
  private def createMetaData(cacheMap: CacheMap, suppDecData: SupplementaryDeclarationData): MetaData = {
    val metaData = MetaData.fromProperties(suppDecData.toMetadataProperties())

    val goodsShipmentWithGoodsItems = metaData.declaration.flatMap(
      _.goodsShipment.map(
        _.copy(
          governmentAgencyGoodsItems =
            cacheMap.getEntry[Seq[GovernmentAgencyGoodsItem]](ExportsItemsCacheIds.itemsId).getOrElse(Seq.empty)
        )
      )
    )
    metaData.copy(declaration = metaData.declaration.map(_.copy(goodsShipment = goodsShipmentWithGoodsItems)))
  }

}
