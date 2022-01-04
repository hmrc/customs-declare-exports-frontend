/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import controllers.routes.RootController
import forms.declaration.ModeOfTransportCode.meaningfulModeOfTransportCodes
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.declaration.{ModeOfTransportCode, TransportLeavingTheBorder}
import models.DeclarationType
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode.Normal
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.transport_leaving_the_border

class TransportLeavingTheBorderControllerSpec extends ControllerSpec with OptionValues {

  val transportLeavingTheBorder = mock[transport_leaving_the_border]

  val controller = new TransportLeavingTheBorderController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    transportLeavingTheBorder
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(transportLeavingTheBorder.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(transportLeavingTheBorder)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Normal)(request))
    theResponseForm
  }

  def theResponseForm: Form[TransportLeavingTheBorder] = {
    val captor: ArgumentCaptor[Form[TransportLeavingTheBorder]] = ArgumentCaptor.forClass(classOf[Form[TransportLeavingTheBorder]])
    verify(transportLeavingTheBorder).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "TransportLeavingTheBorderController.displayPage" should {

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          val result = controller.displayPage(Normal)(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache is not empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDepartureTransport(ModeOfTransportCode.Rail, "", "")))

          val result = controller.displayPage(Normal)(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(TransportLeavingTheBorder(Some(ModeOfTransportCode.Rail)))
        }
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { request =>
      "redirect to the starting page on displayPage" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(Normal)(getRequest())
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "TransportLeavingTheBorderController.submitForm" should {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "return 400 (BAD_REQUEST)" when {
        "the user does not select any option" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitForm(Normal)(postRequest(Json.obj()))
          status(result) must be(BAD_REQUEST)
        }
      }
    }
  }

  "TransportLeavingTheBorderController.submitForm" when {

    meaningfulModeOfTransportCodes.foreach { modeOfTransportCode =>
      s"ModeOfTransportCode is '${modeOfTransportCode}'" should {
        val body = Json.obj("transportLeavingTheBorder" -> modeOfTransportCode.value)

        onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
          "update the cache after a successful bind" in {
            withNewCaching(request.cacheModel)

            await(controller.submitForm(Normal)(postRequest(body)))

            theCacheModelUpdated.transportLeavingBorderCode.value mustBe modeOfTransportCode
          }
        }

        onJourney(STANDARD, SUPPLEMENTARY) { request =>
          "redirect to the 'Warehouse Identification' page after a successful bind" when {
            "at least one Procedure Code requires Warehouse information (PCs ending with '07', '71', 78')" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withProcedureCodes(Some("1078"), Seq("000"))))))

              val result = controller.submitForm(Normal)(postRequest(body))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe routes.WarehouseIdentificationController.displayPage(Normal)
            }
          }

          "redirect to the 'Supervising Customs Office' page after a successful bind" in {
            withNewCaching(request.cacheModel)

            val result = controller.submitForm(Normal)(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.SupervisingCustomsOfficeController.displayPage(Normal)
          }
        }

        "redirect to the 'Inland or Border' page after a successful bind" when {

          additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
            s"AdditionalDeclarationType is $additionalType and" when {

              "cache contains '1040' as procedure code, '000' as APC" in {
                withNewCaching(withRequest(additionalType, withItem(anItem(withProcedureCodes(Some("1040"), Seq("000"))))).cacheModel)

                val result = controller.submitForm(Normal)(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe routes.InlandOrBorderController.displayPage(Normal)
              }
            }
          }
        }

        "redirect to the 'Inland Transport Details' page after a successful bind" when {

          List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
            "AdditionalDeclarationType is SUPPLEMENTARY_EIDR and" when {

              "cache contains '1040' as procedure code, '000' as APC" in {
                withNewCaching(withRequest(additionalType, withItem(anItem(withProcedureCodes(Some("1040"), Seq("000"))))).cacheModel)

                val result = controller.submitForm(Normal)(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe routes.InlandTransportDetailsController.displayPage(Normal)
              }
            }
          }
        }

        onClearance { request =>
          "redirect to the 'Warehouse Identification' page after a successful bind" in {
            withNewCaching(request.cacheModel)

            val result = controller.submitForm(Normal)(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.WarehouseIdentificationController.displayPage(Normal)
          }
        }
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { request =>
      "always redirect to the starting page" in {
        withNewCaching(request.cacheModel)
        val body = Json.obj("transportLeavingTheBorder" -> "any")

        val result = controller.submitForm(Normal)(postRequest(body))
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }
}
