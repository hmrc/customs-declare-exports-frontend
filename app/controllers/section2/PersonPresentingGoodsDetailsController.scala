/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes.ExporterEoriNumberController
import forms.section2.PersonPresentingGoodsDetails.form
import forms.section2.PersonPresentingGoodsDetails
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.person_presenting_goods_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PersonPresentingGoodsDetailsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  personPresentingGoodsDetailsPage: person_presenting_goods_details
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(CLEARANCE)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.personPresentingGoodsDetails match {
      case Some(data) => Ok(personPresentingGoodsDetailsPage(frm.fill(data)))
      case _          => Ok(personPresentingGoodsDetailsPage(frm))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType(CLEARANCE)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(personPresentingGoodsDetailsPage(formWithErrors))),
        validData => updateCache(validData).map(_ => navigator.continueTo(ExporterEoriNumberController.displayPage))
      )
  }

  private def updateCache(validData: PersonPresentingGoodsDetails)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.copy(parties = model.parties.copy(personPresentingGoodsDetails = Some(validData))))
}
