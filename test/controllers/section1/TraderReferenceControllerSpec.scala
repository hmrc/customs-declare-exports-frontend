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

package controllers.section1

import base.ExportsTestData.eori
import base.{AuditedControllerSpec, ControllerSpec}
import controllers.actions.AmendmentDraftFilterSpec
import controllers.routes.RootController
import controllers.section1.routes.ConfirmDucrController
import forms.Ducr
import forms.section1.TraderReference.traderReferenceKey
import forms.section1.{ConsignmentReferences, TraderReference}
import models.DeclarationType.{allDeclarationTypesExcluding, SUPPLEMENTARY}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section1.trader_reference

import java.time.{LocalDate, ZonedDateTime}

class TraderReferenceControllerSpec extends ControllerSpec with AuditedControllerSpec with AmendmentDraftFilterSpec {

  private val traderReferencePage = mock[trader_reference]

  val controller =
    new TraderReferenceController(mockAuthAction, mockJourneyAction, navigator, mcc, mockExportsCacheService, traderReferencePage)(ec, auditService)

  def nextPageOnTypes: Seq[NextPageOnType] =
    allDeclarationTypesExcluding(SUPPLEMENTARY).map(NextPageOnType(_, ConfirmDucrController.displayPage))

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
  private val dummyDucr = Ducr(s"${lastDigitOfYear}GB${eori.dropWhile(_.isLetter)}-${dummyTraderRef.value}")
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
        verifyNoAudit()
      }

      "form was submitted with no data" in {
        withNewCaching(aDeclaration())

        val body = Json.obj(traderReferenceKey -> "")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        status(result) mustBe BAD_REQUEST
        verifyTheCacheIsUnchanged()
        verifyNoAudit()
      }
    }

    "return 303 redirect" when {

      "form was submitted with valid data" in {
        val declaration = aDeclaration(withCreatedDate(LocalDate.now()))
        withNewCaching(declaration)

        val body = Json.obj(traderReferenceKey -> dummyTraderRef.value)
        val result = controller.submitForm()(postRequest(body, declaration))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConfirmDucrController.displayPage
        theCacheModelUpdated().head mustBe aDeclarationAfter(declaration, withConsignmentReferences(dummyConRefs))
        verifyAudit()
      }

      "display page method is invoked on supplementary journey" in {
        withNewCaching(aDeclaration(withType(SUPPLEMENTARY)))

        val result = controller.displayPage(getJourneyRequest())

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoAudit()
      }
    }
  }
}
