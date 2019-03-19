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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import controllers.util.CacheIdGenerator.goodsItemCacheId
import forms.supplementary.CommodityMeasure.{commodityFormId, form, _}
import forms.supplementary.PackageInformation.formId
import forms.supplementary.{CommodityMeasure, PackageInformation}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.goods_measure

import scala.concurrent.{ExecutionContext, Future}

class CommodityMeasureController @Inject()(
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  cacheService: CustomsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    cacheService
      .fetchAndGetEntry[Seq[PackageInformation]](goodsItemCacheId, formId)
      .flatMap {
        case Some(packages) =>
          cacheService.fetchAndGetEntry[CommodityMeasure](goodsItemCacheId, commodityFormId).map {
            case Some(data) => Ok(goods_measure(form.fill(data)))
            case _          => Ok(goods_measure(form))
          }
        case _ => Future.successful(BadRequest(goods_measure(form.withGlobalError(ADD_ONE))))
      }
  }

  def submitForm(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CommodityMeasure]) => Future.successful(BadRequest(goods_measure(formWithErrors))),
        validForm =>
          cacheService.cache[CommodityMeasure](goodsItemCacheId, commodityFormId, validForm).map { _ =>
            Redirect(controllers.supplementary.routes.AdditionalInformationController.displayForm())
        }
      )
  }
}
