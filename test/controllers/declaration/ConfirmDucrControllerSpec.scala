package controllers.declaration

import base.ControllerSpec
import forms.Ducr
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import mock.ErrorHandlerMocks
import models.DeclarationType.SUPPLEMENTARY
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.{verify, when}
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.declaration.confirm_ducr

import java.time.ZonedDateTime

class ConfirmDucrControllerSpec extends ControllerSpec with ErrorHandlerMocks {

    private val confirmDucrPage = mock[confirm_ducr]

    private val controller = new ConfirmDucrController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      mockErrorHandler,
      stubMessagesControllerComponents(),
      mockExportsCacheService,
      confirmDucrPage
    )

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage()(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(confirmDucrPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(confirmDucrPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    setupErrorHandler()
    authorizedUser()
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(confirmDucrPage)
    super.afterEach()
  }

  private val dummyTraderRef = "dummyRef"
  private val lastDigitOfYear = ZonedDateTime.now().getYear.toString.last
  private val eori = "12345"
  private val dummyDucr = Ducr(lastDigitOfYear + "GB" + eori + "-" + dummyTraderRef)

    "ConfirmDucrController" should {

      "return 200 OK" when {

        "display page method is invoked with trader reference in the cache" in {
          withNewCaching(aDeclaration(withTraderReference(dummyTraderRef)))

          val result = controller.displayPage()(getJourneyRequest())

          status(result) mustBe OK
          verify(confirmDucrPage).apply(any(), meq(dummyDucr))(any(), any())
        }
      }

      "return 400 bad request" when {

        "display page is invoked with no trader ref in cache" in {
          withNewCaching(aDeclaration())

          val result = controller.displayPage()(getJourneyRequest())

          status(result) mustBe BAD_REQUEST
          verify(mockErrorHandler).displayErrorPage()(any())
          verifyTheCacheIsUnchanged()
        }

        "form was submitted with no data" in {
          withNewCaching(aDeclaration(withTraderReference(dummyTraderRef)))

          val body = Json.obj(YesNoAnswer.formId -> "")
          val result = controller.submitForm()(postRequest(body, aDeclaration()))

          status(result) mustBe BAD_REQUEST
          verifyTheCacheIsUnchanged()
        }
      }

      "return 303 redirect" when {

        "form was submitted with Yes answer" in {
          val declaration = aDeclaration(withTraderReference(dummyTraderRef))
          withNewCaching(declaration)

          val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
          val result = controller.submitForm()(postRequest(body, aDeclaration()))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe ???
          theCacheModelCreated mustBe aDeclarationAfter(declaration, _.copy(ducrEntry = Some(dummyDucr)))
        }

        "form was submitted with No answer" in {
          withNewCaching(aDeclaration(withTraderReference(dummyTraderRef)))

          val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.no)
          val result = controller.submitForm()(postRequest(body, aDeclaration()))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe ???
          verifyTheCacheIsUnchanged()
        }

        "display page method is invoked on supplementary journey" in {
          withNewCaching(aDeclaration(withType(SUPPLEMENTARY)))

          val result = controller.displayPage()(getJourneyRequest())

          status(result) mustBe 303
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)
        }
      }
    }
}
