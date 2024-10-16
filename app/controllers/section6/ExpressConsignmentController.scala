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

package controllers.section6

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section6.routes.{ContainerController, TransportPaymentController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section6.express_consignment

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExpressConsignmentController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  expressConsignmentPage: express_consignment
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private lazy val emptyKey = "declaration.transportInformation.expressConsignment.empty"

  private val validTypes = Seq(STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE)

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val frm = form(errorKey = emptyKey).withSubmissionErrors
    request.cacheModel.transport.expressConsignment match {
      case Some(yesNoAnswer) => Ok(expressConsignmentPage(frm.fill(yesNoAnswer)))
      case _                 => Ok(expressConsignmentPage(frm))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form(errorKey = emptyKey)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(expressConsignmentPage(formWithErrors))),
        yesNoAnswer => updateCache(yesNoAnswer).map(_ => navigator.continueTo(nextPage(yesNoAnswer)))
      )
  }

  private def nextPage(yesNoAnswer: YesNoAnswer): Call =
    if (yesNoAnswer.answer == YesNoAnswers.yes) TransportPaymentController.displayPage
    else ContainerController.displayContainerSummary

  private def updateCache(yesNoAnswer: YesNoAnswer)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      model.copy(transport =
        model.transport.copy(
          expressConsignment = Some(yesNoAnswer),
          transportPayment = if (yesNoAnswer.answer == YesNoAnswers.no) None else model.transport.transportPayment
        )
      )
    }
}
