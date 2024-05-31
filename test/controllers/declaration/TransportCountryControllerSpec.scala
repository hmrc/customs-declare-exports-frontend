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
import forms.declaration.ModeOfTransportCode.{FixedTransportInstallations, PostalConsignment, Rail}
import forms.declaration.TransportCountry
import forms.declaration.TransportCountry.transportCountry
import forms.declaration.countries.Country
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.transport_country

import scala.collection.immutable.ListMap
import scala.concurrent.Future

class TransportCountryControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val page = mock[transport_country]
  val codeListConnector = mock[CodeListConnector]

  val controller = new TransportCountryController(mockAuthAction, mockJourneyAction, navigator, mockExportsCacheService, mcc, page)(
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
          s"Destination country is '$country' and" when {

            "the 'displayPage' method is invoked" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withDestinationCountry(Country(Some(country)))))
              verifyRedirection(controller.displayPage(getRequest()))
            }

            "the 'submitForm' method is invoked" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withDestinationCountry(Country(Some(country)))))

              val formData = Json.obj(transportCountry -> countryCode)
              verifyRedirection(controller.submitForm(postRequest(formData)))
            }
          }
        }

        "'Transport Leaving the Border' is 'Rail and" when {

          "the 'displayPage' method is invoked" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withTransportLeavingTheBorder(Some(Rail))))
            verifyRedirection(controller.displayPage(getRequest()))
          }

          "the 'submitForm' method is invoked" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withTransportLeavingTheBorder(Some(Rail))))

            val formData = Json.obj(transportCountry -> countryCode)
            verifyRedirection(controller.submitForm(postRequest(formData)))
          }
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { request =>
      "return 303 (SEE_OTHER) and redirect to the 'next page'" when {
        List(FixedTransportInstallations, PostalConsignment).foreach { modeOfTransport =>
          s"'Transport Leaving the Border' is '$modeOfTransport'" should {

            "the 'displayPage' method is invoked" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withTransportLeavingTheBorder(Some(modeOfTransport))))
              verifyRedirection(controller.displayPage(getRequest()))
            }

            "the 'submitForm' method is invoked" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withTransportLeavingTheBorder(Some(modeOfTransport))))

              val formData = Json.obj(transportCountry -> countryCode)
              verifyRedirection(controller.submitForm(postRequest(formData)))
            }
          }

          s"'Inland Mode of Transport' is '$modeOfTransport'" should {

            "the 'displayPage' method is invoked" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withInlandModeOfTransportCode(modeOfTransport)))
              verifyRedirection(controller.displayPage(getRequest()))
            }

            "the 'submitForm' method is invoked" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withInlandModeOfTransportCode(modeOfTransport)))

              val formData = Json.obj(transportCountry -> countryCode)
              verifyRedirection(controller.submitForm(postRequest(formData)))
            }
          }
        }
      }

      "return 200 (OK)" when {

        "the 'displayPage' method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verify(page, times(1)).apply(any(), any())(any(), any())
          theResponseForm.value mustBe empty
        }

        "the 'displayPage' method is invoked and cache contains data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withTransportCountry(Some(countryCode))))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verify(page, times(1)).apply(any(), any())(any(), any())
          theResponseForm.value.get.countryCode mustBe Some(countryCode)
        }
      }

      "the 'submitForm' method is invoked and" when {
        "the user selects a country from the dropdown" should {
          "update the model" in {
            withNewCaching(request.cacheModel)

            val formData = Json.obj(transportCountry -> countryCode)
            val result = controller.submitForm(postRequest(formData))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe nextPage(request.declarationType)

            val transport = theCacheModelUpdated.transport
            transport.transportCrossingTheBorderNationality.value mustBe TransportCountry(Some(countryCode))
            verifyAudit()
          }
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

        "the 'submitForm' method is invoked" in {
          withNewCaching(request.cacheModel)

          val formData = Json.obj(transportCountry -> countryCode)
          val result = controller.submitForm(postRequest(formData))
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
          verifyNoAudit()
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "no value is entered" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(fieldIdOnError(transportCountry) -> "")
        val result = controller.submitForm(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.transportInformation.transportCountry.country.error.empty"
      }

      "the entered value is incorrect or not a list's option" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(fieldIdOnError(transportCountry) -> "!@#$")
        val result = controller.submitForm(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.transportInformation.transportCountry.country.error.invalid"
      }
    }

    def verifyRedirection(result: Future[Result]): Assertion = {
      status(result) mustBe SEE_OTHER

      val declaration = theCacheModelUpdated
      declaration.transport.transportCrossingTheBorderNationality mustBe None

      thePageNavigatedTo mustBe nextPage(declaration.`type`)
    }
  }
}
