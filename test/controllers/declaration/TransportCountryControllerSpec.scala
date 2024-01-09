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

import base.{AuditedControllerSpec, ControllerSpec}
import connectors.CodeListConnector
import controllers.declaration.routes.{ExpressConsignmentController, TransportContainerController}
import controllers.helpers.TransportSectionHelper.{Guernsey, Jersey}
import controllers.routes.RootController
import forms.declaration.TransportCountry
import forms.declaration.TransportCountry.{hasTransportCountry, transportCountry}
import forms.declaration.countries.Country
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.transport_country

import scala.collection.immutable.ListMap

class TransportCountryControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val page = mock[transport_country]
  val codeListConnector = mock[CodeListConnector]

  val controller =
    new TransportCountryController(mockAuthAction, mockJourneyAction, navigator, mockExportsCacheService, stubMessagesControllerComponents(), page)(
      ec,
      codeListConnector,
      auditService
    )

  val countryCode = "ZA"
  val countryName = "South Africa"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(page.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(codeListConnector.getCountryCodes(any())).thenReturn(ListMap(countryCode -> models.codes.Country(countryName, countryCode)))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(page, codeListConnector, auditService)
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  val itemId = "itemId"

  def theResponseForm: Form[TransportCountry] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TransportCountry]])
    verify(page).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  def nextPage: PartialFunction[DeclarationType, Call] = {
    case STANDARD | OCCASIONAL | SIMPLIFIED => ExpressConsignmentController.displayPage
    case SUPPLEMENTARY                      => TransportContainerController.displayContainerSummary
  }

  "TransportCountryController" should {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "return 303 (SEE_OTHER) and redirect to the 'next page'" when {
        List(Guernsey, Jersey).foreach { country =>
          s"the 'displayPage' method is invoked and Destination country is '$country'" in {
            val destinationCountry = withDestinationCountry(Country(Some(country)))
            withNewCaching(aDeclarationAfter(request.cacheModel, destinationCountry, withTransportCountry(Some("Some country"))))

            val result = controller.displayPage(getRequest())

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe nextPage(request.declarationType)

            val transport = theCacheModelUpdated.transport
            transport.transportCrossingTheBorderNationality mustBe None
          }

          s"the 'submitForm' method is invoked and Destination country is '$country'" in {
            val destinationCountry = withDestinationCountry(Country(Some(country)))
            withNewCaching(aDeclarationAfter(request.cacheModel, destinationCountry, withTransportCountry(Some("Some country"))))

            val formData = Json.obj(hasTransportCountry -> "Yes", transportCountry -> countryName)
            val result = controller.submitForm(postRequest(formData))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe nextPage(request.declarationType)

            val transport = theCacheModelUpdated.transport
            transport.transportCrossingTheBorderNationality mustBe None
          }
        }
      }
    }

    onJourney(STANDARD, OCCASIONAL, SUPPLEMENTARY, SIMPLIFIED) { request =>
      "return 200 (OK)" when {

        "the 'displayPage' method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verify(page, times(1)).apply(any(), any())(any(), any())
          theResponseForm.value mustBe empty
        }

        "the 'displayPage' method is invoked and cache contains data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withTransportCountry(Some(countryName))))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verify(page, times(1)).apply(any(), any())(any(), any())

          theResponseForm.value.get.countryName mustBe Some(countryName)
        }
      }
    }

    onClearance { request =>
      "redirect to the starting page" when {
        "the 'displayPage' method is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }
      }
    }
  }

  "TransportCountryController.submitForm" should {
    onJourney(STANDARD, OCCASIONAL, SIMPLIFIED, SUPPLEMENTARY) { request =>
      "return 303 (SEE_OTHER)" when {

        "the user selects the 'No' radio" in {
          withNewCaching(request.cacheModel)

          val formData = Json.obj(hasTransportCountry -> "No")
          val result = controller.submitForm(postRequest(formData))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage(request.declarationType)
          verifyAudit()
        }

        "the user selects the 'Yes' radio and a country from the dropdown" in {
          withNewCaching(request.cacheModel)

          val formData = Json.obj(hasTransportCountry -> "Yes", transportCountry -> countryName)
          val result = controller.submitForm(postRequest(formData))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage(request.declarationType)
          verifyAudit()
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val formData = Json.obj(hasTransportCountry -> "Yes", transportCountry -> "some country")
          val result = controller.submitForm(postRequest(formData))

          status(result) mustBe BAD_REQUEST
          verify(page, times(1)).apply(any(), any())(any(), any())
          verifyNoAudit()
        }
      }
    }

    onClearance { request =>
      "redirect to the starting page" in {
        withNewCaching(request.cacheModel)

        val formData = Json.obj(hasTransportCountry -> "Yes", transportCountry -> countryName)
        val result = controller.submitForm(postRequest(formData))
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoAudit()
      }
    }
  }
}
