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
import controllers.section2.routes.{IsExsController, RepresentativeAgentController}
import forms.common.{Address, Eori}
import forms.section2.EntityDetails
import forms.section2.exporter.ExporterDetails
import models.DeclarationType._
import models.codes.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.exporter_address

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext

class ExporterDetailsControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val exporter_address = mock[exporter_address]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new ExporterDetailsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, exporter_address)(
    ExecutionContext.global,
    mockCodeListConnector,
    auditService
  )

  val address = Some(Address("CaptainAmerica", "Test Street", "Leeds", "LS18BN", "GB"))
  val eori = Some(Eori("GB213472539481923"))

  def theResponseForm: Form[ExporterDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ExporterDetails]])
    verify(exporter_address).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(exporter_address.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom, Great Britain, Northern Ireland", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(exporter_address, mockCodeListConnector)
    super.afterEach()
  }

  "Exporter Details Controller" should {

    onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { request =>
      "return 200 OK" when {

        "details are empty" in {
          withNewCaching(request.cacheModel)
          val response = controller.displayPage(getRequest())
          status(response) mustBe OK
        }

        "details are filled" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withExporterDetails(eori = eori, address = address)))
          val response = controller.displayPage(getRequest())
          status(response) mustBe OK
          val details = theResponseForm.value.value.details
          details.eori mustBe defined
          details.address mustBe defined
        }

      }

      "return 303 (SEE_OTHER) and redirect to representative details page" when {
        "correct form is submitted" in {
          withNewCaching(request.cacheModel)
          val exporterDetails = ExporterDetails(EntityDetails(eori, address))
          val body = Json.toJson(exporterDetails)
          val response = await(controller.saveAddress(postRequest(body)))

          response mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RepresentativeAgentController.displayPage
          verifyAudit()
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "return 303 (SEE_OTHER) and redirect to Is Exs page" when {
        "correct form is submitted" in {
          withNewCaching(request.cacheModel)
          val body = Json.obj("details" -> Json.obj("eori" -> "GB213472539481923"))
          val response = await(controller.saveAddress(postRequest(body)))

          response mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe IsExsController.displayPage
          verifyAudit()
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
          s"$prefix.addressLine" -> "$".repeat(71),
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
  }
}
