/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.declaration.procedurecodes.ProcedureCode
import forms.declaration.procedurecodes.ProcedureCode.form
import models.declaration.ProcedureCodesData
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.procedureCodes.procedure_codes

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProcedureCodesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  procedureCodesPage: procedure_codes
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    val filledForm = request.cacheModel.itemBy(itemId) match {
      case Some(exportItem) => exportItem.procedureCodes.fold(frm)(cachedData => frm.fill(cachedData.toProcedureCode()))
      case None             => frm
    }

    Ok(procedureCodesPage(mode, itemId, filledForm))
  }

  def submitProcedureCodes(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(procedureCodesPage(mode, itemId, formWithErrors))),
        correctProcedureCode =>
          updateCache(itemId, correctProcedureCode)
            .map(_ => navigator.continueTo(mode, routes.AdditionalProcedureCodesController.displayPage(_, itemId)))
      )
  }

  private def updateCache(itemId: String, procedureCodeEntered: ProcedureCode)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {

    val updateProcedureCode: ExportsDeclaration => ExportsDeclaration = { model =>
      model.updatedItem(itemId, item => {
        val newProcedureCode = Some(procedureCodeEntered.procedureCode)
        val newProcedureCodes = item.procedureCodes.fold(ProcedureCodesData(newProcedureCode, Seq.empty))(_.copy(procedureCode = newProcedureCode))

        item.copy(procedureCodes = Some(newProcedureCodes))
      })
    }

    val updateAdditionalProcedureCodes: ExportsDeclaration => ExportsDeclaration = { model =>
      model.updatedItem(
        itemId,
        item => {
          (for {
            procedureCodesData <- item.procedureCodes
            cachedProcedureCode <- procedureCodesData.procedureCode
            isNewCodeDifferentThanCached = cachedProcedureCode != procedureCodeEntered.procedureCode
            newProcedureCodesData = item.procedureCodes.map(_.copy(additionalProcedureCodes = Seq.empty)) if isNewCodeDifferentThanCached

            updatedItem = item.copy(procedureCodes = newProcedureCodesData)
          } yield updatedItem).getOrElse(item)
        }
      )
    }

    val removeFiscalInformationForCode: ExportsDeclaration => ExportsDeclaration = { model =>
      if (!ProcedureCodesData.osrProcedureCodes.contains(procedureCodeEntered.procedureCode))
        model.updatedItem(itemId, item => item.copy(fiscalInformation = None, additionalFiscalReferencesData = None))
      else model
    }

    val removePackageInformationForCode: ExportsDeclaration => ExportsDeclaration = { model =>
      if (request.isType(DeclarationType.CLEARANCE) && ProcedureCodesData.eicrProcedureCodes.contains(procedureCodeEntered.procedureCode))
        model.updatedItem(itemId, item => item.copy(packageInformation = None))
      else model
    }

    val removeWarehouseIdentificationForCode: ExportsDeclaration => ExportsDeclaration = { model =>
      if (request.isType(DeclarationType.CLEARANCE) || model.requiresWarehouseId)
        model
      else
        model.copy(locations = model.locations.copy(warehouseIdentification = None))
    }

    updateExportsDeclarationSyncDirect { model =>
      model
        .transform(updateAdditionalProcedureCodes)
        .transform(updateProcedureCode)
        .transform(removeFiscalInformationForCode)
        .transform(removePackageInformationForCode)
        .transform(removeWarehouseIdentificationForCode)
    }
  }

}
