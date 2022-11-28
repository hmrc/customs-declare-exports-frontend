package controllers.declaration

import base.ControllerSpec
import forms.declaration.TraderReference
import models.DeclarationType.SUPPLEMENTARY
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.trader_reference

class TraderReferenceControllerSpec extends ControllerSpec {

  private val traderReferencePage = mock[trader_reference]

  private val controller = new TraderReferenceController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockExportsCacheService,
    traderReferencePage
  )

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage()(request))
    theResponseForm
  }

  def theResponseForm: Form[TraderReference] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TraderReference]])
    verify(traderReferencePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(traderReferencePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    authorizedUser()
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(traderReferencePage)
    super.afterEach()
  }

  "TraderReferenceController" should {

    "return 200 OK" when {

      "display page method is invoked with nothing in cache" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage()(getJourneyRequest())

        status(result) mustBe OK
      }

      "display page method is invoked with data in cache" in {
        withNewCaching(aDeclaration(withTraderReference("dummyRef")))

        val result = controller.displayPage()(getJourneyRequest())

        status(result) mustBe OK
      }
    }

    "return 400 bad request" when {

      "form was submitted with invalid data" in {
        withNewCaching(aDeclaration())

        val body = Json.obj("traderReference" -> "!!!!!")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        status(result) mustBe BAD_REQUEST
        verifyTheCacheIsUnchanged()
      }

      "form was submitted with no data" in {
        withNewCaching(aDeclaration())

        val body = Json.obj("traderReference" -> "")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        status(result) mustBe BAD_REQUEST
        verifyTheCacheIsUnchanged()
      }
    }

    "return 303 redirect" when {

      "form was submitted with valid data" in {
        withNewCaching(aDeclaration())

        val body = Json.obj("traderReference" -> "INVOICE123/4")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.ConfirmDucrController.displayPage
        theCacheModelUpdated()
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
