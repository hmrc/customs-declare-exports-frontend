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

package base

import config.AppConfig
import controllers.general.ErrorHandler
import controllers.helpers.{Add, AddField, SaveAndContinue}
import mock.{ErrorHandlerMocks, FeatureFlagMocks, JourneyActionMocks, VerifiedEmailMocks}
import models.ExportsDeclaration
import models.requests.{JourneyRequest, SessionHelper}
import models.responses.FlashKeys
import org.mockito.Mockito.when
import play.api.data.{Form, FormError}
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.test.Helpers.contentAsString
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import play.twirl.api.Html
import services.cache.ExportsItemBuilder
import utils.FakeRequestCSRFSupport._
import views.html.general.error_template

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

trait ControllerWithoutFormSpec extends ControllerSpec {

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] =
    mockFormForDisplayRequest
}

trait ControllerSpec
    extends UnitSpec with DefaultAwaitTimeout with ErrorHandlerMocks with ExportsItemBuilder with FeatureFlagMocks with JourneyTypeTestRunner
    with JourneyActionMocks with MockAuthAction with MockConnectors with MockExportCacheService with MockNavigator with VerifiedEmailMocks {
  implicit val ec: ExecutionContext = ExecutionContext.global

  protected val mcc = stubMessagesControllerComponents()

  protected val errorHandler = new ErrorHandler(mcc.messagesApi, instanceOf[error_template])(instanceOf[AppConfig], global)

  protected def addActionUrlEncoded(field: String = ""): (String, String) =
    if (field.isEmpty) (Add.toString, field) else (AddField.toString, field)

  protected val saveAndContinueActionUrlEncoded: (String, String) = (SaveAndContinue.toString, "")

  protected def viewOf(result: Future[Result]) = Html(contentAsString(result))

  protected def getRequest(declaration: ExportsDeclaration): JourneyRequest[AnyContentAsEmpty.type] =
    getJourneyRequest(declaration)

  protected def postRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withSession(SessionHelper.declarationUuid -> "declaration-id")
      .withJsonBody(body)
      .withCSRFToken

  protected def postRequest(body: JsValue, declaration: ExportsDeclaration): Request[AnyContent] =
    FakeRequest("POST", "")
      .withSession(SessionHelper.declarationUuid -> declaration.id)
      .withJsonBody(body)
      .withCSRFToken

  protected def postRequestWithSession(body: JsValue, sessionData: Seq[(String, String)]): Request[AnyContent] =
    FakeRequest("POST", "")
      .withSession(sessionData: _*)
      .withJsonBody(body)
      .withCSRFToken

  protected def postRequestAsFormUrlEncoded(body: (String, String)*): Request[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "")
      .withSession(SessionHelper.declarationUuid -> "declaration-id")
      .withFormUrlEncodedBody(body: _*)
      .withCSRFToken

  protected def postRequestWithSubmissionError: Request[AnyContentAsEmpty.type] =
    FakeRequest("POST", "")
      .withFlash((FlashKeys.errorMessage, submissionError))
      .withSession(SessionHelper.declarationUuid -> "declaration-id")
      .withCSRFToken

  protected def deleteRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("DELETE", "")
      .withSession(SessionHelper.declarationUuid -> "declaration-id")
      .withJsonBody(body)
      .withCSRFToken

  private val submissionField = ""
  private val submissionError = "some error"
  protected val submissionFormError = FormError(submissionField, submissionError)

  protected def getRequestWithSubmissionErrors: Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "")
      .withFlash((FlashKeys.errorMessage, submissionError))
      .withSession((SessionHelper.declarationUuid -> "declaration-id"))
      .withCSRFToken

  protected def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_]

  protected val mockFormForDisplayRequest: Form[_] = {
    val form = mock[Form[_]]
    when(form.errors).thenReturn(Seq(submissionFormError))
    form
  }

  def fieldIdOnError(fieldId: String): String = s"$fieldId-autocomp"

  "Controller" should {
    "return form with submission errors" in {
      val form = getFormForDisplayRequest(getRequestWithSubmissionErrors)
      form.errors mustBe Seq(submissionFormError)
    }
  }
}
