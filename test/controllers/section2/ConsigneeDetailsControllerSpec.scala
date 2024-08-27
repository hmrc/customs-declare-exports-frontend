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

import base.{AuditedControllerSpec, ControllerSpec}
import connectors.CodeListConnector
import controllers.section2.routes.{AdditionalActorsSummaryController, AuthorisationProcedureCodeChoiceController}
import forms.common.Address
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.{ConsigneeDetails, EntityDetails}
import models.DeclarationType._
import models.codes.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.consignee_details

import scala.collection.immutable.ListMap

class ConsigneeDetailsControllerSpec extends ControllerSpec with AuditedControllerSpec {

  val consigneeDetailsPage = mock[consignee_details]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new ConsigneeDetailsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, consigneeDetailsPage)(
    ec,
    mockCodeListConnector,
    auditService
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(consigneeDetailsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom, Great Britain, Northern Ireland", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(consigneeDetailsPage, mockCodeListConnector)

    super.afterEach()
  }

  def theResponseForm: Form[ConsigneeDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ConsigneeDetails]])
    verify(consigneeDetailsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  private val correctAddress = Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "GB")

  "Consignee Details controller" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withConsigneeDetails(None, Some(correctAddress))))

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
        }
      }

      "return 303 (SEE_OTHER) and redirect to other parties summary page" when {
        "form is correct" in {
          withNewCaching(request.cacheModel)
          testFormSubmitRedirectsTo(AdditionalActorsSummaryController.displayPage)
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {
      val prefix = "details.address"

      "no value is entered" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(s"$prefix.fullName" -> "", s"$prefix.addressLine" -> "", s"$prefix.townOrCity" -> "", s"$prefix.postCode" -> "")
        val result = controller.saveAddress(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.address.fullName.empty"
        errors(1).messages.head mustBe "declaration.address.addressLine.empty"
        errors(2).messages.head mustBe "declaration.address.townOrCity.empty"
        errors(3).messages.head mustBe "declaration.address.postCode.empty"
        errors(4).messages.head mustBe "declaration.address.country.empty"
      }

      "the entered values are incorrect" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(
          s"$prefix.fullName" -> "$".repeat(36),
          s"$prefix.addressLine" -> "$".repeat(36),
          s"$prefix.townOrCity" -> "$".repeat(36),
          s"$prefix.postCode" -> "$".repeat(10),
          s"$prefix.country" -> "TTTT"
        )
        val result = controller.saveAddress(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.address.fullName.error"
        errors(1).messages.head mustBe "declaration.address.fullName.length"
        errors(2).messages.head mustBe "declaration.address.addressLine.error"
        errors(3).messages.head mustBe "declaration.address.addressLine.length35MaxChars"
        errors(4).messages.head mustBe "declaration.address.townOrCity.error"
        errors(5).messages.head mustBe "declaration.address.townOrCity.length"
        errors(6).messages.head mustBe "declaration.address.postCode.error"
        errors(7).messages.head mustBe "declaration.address.postCode.length"
        errors(8).messages.head mustBe "declaration.address.country.error"
      }
    }

    onJourney(CLEARANCE) { request =>
      "return 303 (SEE_OTHER) and redirect to appropriate page" when {

        "form is correct and EIDR is false" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withEntryIntoDeclarantsRecords(YesNoAnswers.no)))
          testFormSubmitRedirectsTo(AuthorisationProcedureCodeChoiceController.displayPage)
        }

        "form is correct and EIDR is true" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withEntryIntoDeclarantsRecords(YesNoAnswers.yes)))
          testFormSubmitRedirectsTo(AuthorisationProcedureCodeChoiceController.displayPage)
        }
      }
    }
  }

  private def testFormSubmitRedirectsTo(expectedRedirectionLocation: Call): Unit = {
    val correctForm = Json.toJson(ConsigneeDetails(EntityDetails(None, Some(correctAddress))))

    val result = controller.saveAddress(postRequest(correctForm))

    await(result) mustBe aRedirectToTheNextPage
    thePageNavigatedTo mustBe expectedRedirectionLocation
    verifyAudit()
  }
}
