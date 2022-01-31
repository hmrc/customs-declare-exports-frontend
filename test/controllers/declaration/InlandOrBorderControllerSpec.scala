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
import base.ExportsTestData.allValuesRequiringToSkipInlandOrBorder
import controllers.declaration.routes.{
  DepartureTransportController,
  ExpressConsignmentController,
  InlandTransportDetailsController,
  TransportContainerController
}
import controllers.helpers.InlandOrBorderHelper
import controllers.helpers.TransportSectionHelper._
import controllers.routes.RootController
import forms.declaration.InlandOrBorder
import forms.declaration.InlandOrBorder.{fieldId, Border, Inland}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED}
import models.Mode.Normal
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsNull, JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.inland_border

class InlandOrBorderControllerSpec extends ControllerSpec with OptionValues {

  private val inlandOrBorderPage = mock[inland_border]
  private val inlandOrBorderHelper = instanceOf[InlandOrBorderHelper]

  val controller = new InlandOrBorderController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    inlandOrBorderPage,
    inlandOrBorderHelper
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()

    when(inlandOrBorderPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(inlandOrBorderPage)
    super.afterEach()
  }

  def theResponseForm: Form[InlandOrBorder] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[InlandOrBorder]])
    verify(inlandOrBorderPage).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withAdditionalDeclarationType(STANDARD_FRONTIER)))
    await(controller.displayPage(Normal)(request))
    theResponseForm
  }

  def cacheRequest(additionalType: AdditionalDeclarationType, modifiers: ExportsDeclarationModifier*): Unit =
    withNewCaching(withRequest(additionalType, modifiers: _*).cacheModel)

  "InlandOrBorderController.displayPage" should {

    "return 200 (OK)" when {
      additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
        s"AdditionalDeclarationType is ${additionalType} and" when {

          "location.inlandOrBorder is not cached yet" in {
            cacheRequest(additionalType)
            val result = controller.displayPage(Normal)(getRequest())
            status(result) must be(OK)
            theResponseForm.value mustBe empty
          }

          "location.inlandOrBorder have been already cached" in {
            cacheRequest(additionalType, withInlandOrBorder(Some(Border)))
            val result = controller.displayPage(Normal)(getRequest())
            status(result) must be(OK)
            theResponseForm.value mustBe Some(Border)
          }
        }
      }
    }

    onJourney(CLEARANCE, SIMPLIFIED, OCCASIONAL) { request =>
      "redirect to the starting page" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(Normal)(getRequest())
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }

    "redirect to the starting page" when {

      "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" in {
        cacheRequest(SUPPLEMENTARY_EIDR)
        val result = controller.displayPage(Normal)(getRequest())
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }

      additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
        s"AdditionalDeclarationType is $additionalType and" when {
          "the user has previously entered a value which requires to skip the /inland-or-border page" in {
            allValuesRequiringToSkipInlandOrBorder.foreach { modifier =>
              cacheRequest(additionalType, modifier)
              val result = controller.displayPage(Normal)(getRequest())
              redirectLocation(result) mustBe Some(RootController.displayPage.url)
            }
          }
        }
      }
    }
  }

  "InlandOrBorderController.submitPage" when {

    additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType and" when {

        "the user selects 'Customs controlled location'" should {
          val body = Json.obj(fieldId -> JsString(Inland.location))
          val expectedNextPage = InlandTransportDetailsController.displayPage()

          "update the cache after a successful bind" in {
            cacheRequest(additionalType)

            await(controller.submitPage(Normal)(postRequest(body)))

            theCacheModelUpdated.locations.inlandOrBorder.value mustBe Inland
          }

          s"redirect to ${expectedNextPage.url}" in {
            cacheRequest(additionalType)

            val result = await(controller.submitPage(Normal)(postRequest(body)))

            result mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe expectedNextPage
          }
        }

        "the user selects 'UK Border'" should {
          val body = Json.obj(fieldId -> JsString(Border.location))
          val expectedNextPage = DepartureTransportController.displayPage()

          "update the cache after a successful bind" in {
            cacheRequest(additionalType)

            await(controller.submitPage(Normal)(postRequest(body)))

            theCacheModelUpdated.locations.inlandOrBorder.value mustBe Border
          }

          s"redirect to ${expectedNextPage.url}" in {
            cacheRequest(additionalType)

            val result = await(controller.submitPage(Normal)(postRequest(body)))

            result mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe expectedNextPage
          }
        }

        "the user does not select any option" should {
          "return 400 (BAD_REQUEST)" in {
            cacheRequest(additionalType)

            val body = Json.obj(fieldId -> JsNull)
            val result = controller.submitPage(Normal)(postRequest(body))

            status(result) mustBe BAD_REQUEST
          }
        }
      }
    }

    "the user selects 'UK Border' and" when {
      val body = Json.obj(fieldId -> JsString(Border.location))

      postalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
        s"TransportLeavingTheBorder is $modeOfTransportCode and" when {

          List(STANDARD_FRONTIER, STANDARD_PRE_LODGED).foreach { additionalType =>
            s"AdditionalDeclarationType is $additionalType" should {
              val expectedNextPage = ExpressConsignmentController.displayPage()

              s"redirect to ${expectedNextPage.url}" in {
                cacheRequest(additionalType, withBorderModeOfTransportCode(modeOfTransportCode))

                val result = await(controller.submitPage(Normal)(postRequest(body)))

                result mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe expectedNextPage
              }
            }
          }

          s"AdditionalDeclarationType is SUPPLEMENTARY_SIMPLIFIED" should {
            val expectedNextPage = TransportContainerController.displayContainerSummary()

            s"redirect to ${expectedNextPage.url}" in {
              cacheRequest(SUPPLEMENTARY_SIMPLIFIED, withBorderModeOfTransportCode(modeOfTransportCode))

              val result = await(controller.submitPage(Normal)(postRequest(body)))

              result mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe expectedNextPage
            }
          }
        }
      }
    }
  }

  "InlandOrBorderController.submitPage" should {

    onJourney(CLEARANCE, SIMPLIFIED, OCCASIONAL) { request =>
      "redirect to the starting page" in {
        withNewCaching(request.cacheModel)

        val result = controller.submitPage(Normal)(postRequest(JsString("")))
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }

    "redirect to the starting page" when {

      "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" in {
        cacheRequest(SUPPLEMENTARY_EIDR)
        val result = controller.submitPage(Normal)(postRequest(JsString("")))
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }

      additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
        s"AdditionalDeclarationType is $additionalType and" when {
          "the user has previously entered a value which requires to skip the /inland-or-border page" in {
            allValuesRequiringToSkipInlandOrBorder.foreach { modifier =>
              cacheRequest(additionalType, modifier)
              val result = controller.submitPage(Normal)(postRequest(JsString("")))
              redirectLocation(result) mustBe Some(RootController.displayPage.url)
            }
          }
        }
      }
    }
  }
}
