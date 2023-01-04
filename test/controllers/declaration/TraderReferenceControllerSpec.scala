/*
 * Copyright 2023 HM Revenue & Customs
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

import base.ControllerSpec
import forms.Ducr
import forms.declaration.{ConsignmentReferences, TraderReference}
import forms.declaration.TraderReference.traderReferenceKey
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

import java.time.{LocalDate, ZonedDateTime}

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
    await(controller.displayPage(request))
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

  private val dummyTraderRef = TraderReference("INVOICE123/4")
  private val lastDigitOfYear = ZonedDateTime.now().getYear.toString.last
  private val dummyDucr = Ducr(s"${lastDigitOfYear}GB${authEori.dropWhile(_.isLetter)}-${dummyTraderRef.value}")
  private val dummyConRefs = ConsignmentReferences(Some(dummyDucr))

  "TraderReferenceController" should {

    "return 200 OK" when {

      "display page method is invoked with nothing in cache" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage(getJourneyRequest())

        status(result) mustBe OK
      }

      "display page method is invoked with data in cache" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage(getJourneyRequest(aDeclaration(withConsignmentReferences(dummyConRefs))))

        status(result) mustBe OK
      }
    }

    "return 400 bad request" when {

      "form was submitted with invalid data" in {
        withNewCaching(aDeclaration())

        val body = Json.obj(traderReferenceKey -> "!!!!!")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        status(result) mustBe BAD_REQUEST
        verifyTheCacheIsUnchanged()
      }

      "form was submitted with no data" in {
        withNewCaching(aDeclaration())

        val body = Json.obj(traderReferenceKey -> "")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        status(result) mustBe BAD_REQUEST
        verifyTheCacheIsUnchanged()
      }
    }

    "return 303 redirect" when {

      "form was submitted with valid data" in {
        val declaration = aDeclaration(withCreatedDate(LocalDate.now()))
        withNewCaching(declaration)

        val body = Json.obj(traderReferenceKey -> dummyTraderRef.value)
        val result = controller.submitForm()(postRequest(body, declaration))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.ConfirmDucrController.displayPage
        theCacheModelUpdated().head mustBe aDeclarationAfter(declaration, withConsignmentReferences(dummyConRefs))
      }

      "display page method is invoked on supplementary journey" in {
        withNewCaching(aDeclaration(withType(SUPPLEMENTARY)))

        val result = controller.displayPage(getJourneyRequest())

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)
      }
    }
  }
}
