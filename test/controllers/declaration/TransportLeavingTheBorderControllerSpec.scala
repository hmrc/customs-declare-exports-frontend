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
import base.ExportsTestData.{itemWithPC, valuesRequiringToSkipInlandOrBorder}
import controllers.declaration.routes.{
  InlandOrBorderController,
  InlandTransportDetailsController,
  SupervisingCustomsOfficeController,
  WarehouseIdentificationController
}
import controllers.helpers.{InlandOrBorderHelper, SupervisingCustomsOfficeHelper}
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import controllers.routes.RootController
import forms.declaration.InlandOrBorder.{Border, Inland}
import forms.declaration.ModeOfTransportCode.{meaningfulModeOfTransportCodes, Maritime, RoRo}
import forms.declaration.TransportLeavingTheBorder.suffixForLocationOfGoods
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.{LocationOfGoods, ModeOfTransportCode, TransportLeavingTheBorder}
import models.DeclarationType
import models.DeclarationType._
import models.Mode.Normal
import models.declaration.ProcedureCodesData.warehouseRequiredProcedureCodes
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

  private val transportLeavingTheBorder = mock[transport_leaving_the_border]

  private val inlandOrBorderHelper = instanceOf[InlandOrBorderHelper]
  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]

  val controller = new TransportLeavingTheBorderController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    transportLeavingTheBorder,
    inlandOrBorderHelper = inlandOrBorderHelper,
    supervisingCustomsOfficeHelper
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
    val captor = ArgumentCaptor.forClass(classOf[Form[TransportLeavingTheBorder]])
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
          withNewCaching(aDeclarationAfter(request.cacheModel, withDepartureTransport(ModeOfTransportCode.Rail)))

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

  "TransportLeavingTheBorderController.submitForm" when {

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "the user does not select any option" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitForm(Normal)(postRequest(Json.obj()))
          status(result) must be(BAD_REQUEST)
        }
      }

      s"LocationOfGood's value is present and ends with '$suffixForLocationOfGoods'" should {
        val goodsLocation = withGoodsLocation(LocationOfGoods(s"GBAUFEMLHR$suffixForLocationOfGoods"))

        "update successfully the cache" when {
          s"ModeOfTransportCode selected is 'RoRo'" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, goodsLocation))

            val body = Json.obj("transportLeavingTheBorder" -> RoRo.value)
            await(controller.submitForm(Normal)(postRequest(body)))

            theCacheModelUpdated.transportLeavingBorderCode mustBe Some(RoRo)
          }
        }

        "return 400 (BAD_REQUEST)" when {
          meaningfulModeOfTransportCodes.filter(_ != RoRo).foreach { modeOfTransportCode =>
            s"ModeOfTransportCode is '${modeOfTransportCode}'" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, goodsLocation))

              val body = Json.obj("transportLeavingTheBorder" -> modeOfTransportCode.value)

              val result = controller.submitForm(Normal)(postRequest(body))
              status(result) must be(BAD_REQUEST)
            }
          }
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
          "redirect to /warehouse-details after a successful bind" when {
            warehouseRequiredProcedureCodes.foreach { suffix =>
              s"user had entered a Procedure Code ending with '$suffix'" in {
                val item = withItem(anItem(withProcedureCodes(Some(s"10$suffix"))))
                withNewCaching(aDeclarationAfter(request.cacheModel, item))

                val result = controller.submitForm(Normal)(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe WarehouseIdentificationController.displayPage()
              }
            }
          }

          "redirect to /supervising-customs-office after a successful bind" in {
            withNewCaching(request.cacheModel)

            val result = controller.submitForm(Normal)(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe SupervisingCustomsOfficeController.displayPage()
          }
        }

        "redirect to /inland-or-border after a successful bind" when {
          "cache contains '1040' as procedure code, '000' as APC and" when {
            val item = withItem(itemWithPC("1040"))

            additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
              s"AdditionalDeclarationType is $additionalType and" in {
                withNewCaching(withRequest(additionalType, item).cacheModel)

                val result = controller.submitForm(Normal)(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage

                val expectedPage =
                  if (modeOfTransportCode == RoRo) InlandTransportDetailsController.displayPage()
                  else InlandOrBorderController.displayPage()

                thePageNavigatedTo mustBe expectedPage
              }
            }
          }
        }

        "redirect to to /inland-transport-details after a successful bind" when {
          "cache contains '1040' as procedure code, '000' as APC and" when {
            val item = withItem(itemWithPC("1040"))

            List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
              "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" in {
                withNewCaching(withRequest(additionalType, item).cacheModel)

                val result = controller.submitForm(Normal)(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe InlandTransportDetailsController.displayPage()
              }
            }

            additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
              s"AdditionalDeclarationType is $additionalType and" when {
                "the user has previously entered a value which requires to skip the /inland-or-border page" in {
                  valuesRequiringToSkipInlandOrBorder.foreach { modifier =>
                    initMockNavigatorForMultipleCallsInTheSameTest
                    val declaration = withRequest(additionalType, modifier, item).cacheModel

                    // This is a special case for this test that as specified would
                    // instead expect to land on /inland-transport-details
                    val landOnInlandOrBorder =
                      modeOfTransportCode != RoRo && declaration.transportLeavingBorderCode == Some(RoRo)

                    val expectedPage =
                      if (landOnInlandOrBorder) InlandOrBorderController.displayPage()
                      else InlandTransportDetailsController.displayPage()

                    withNewCaching(declaration)

                    val result = controller.submitForm(Normal)(postRequest(body))

                    await(result) mustBe aRedirectToTheNextPage
                    thePageNavigatedTo mustBe expectedPage
                  }
                }
              }
            }
          }
        }

        onClearance { request =>
          "redirect to the 'Warehouse Identification' page after a successful bind" in {
            withNewCaching(request.cacheModel)

            val result = controller.submitForm(Normal)(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe WarehouseIdentificationController.displayPage()
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

    val inlandOrBorderIfOrNotToReset = List(
      (STANDARD_FRONTIER, Maritime, Border, Some(Border)),
      (STANDARD_FRONTIER, RoRo, Border, None),
      (STANDARD_PRE_LODGED, Maritime, Border, Some(Border)),
      (SUPPLEMENTARY_SIMPLIFIED, Maritime, Inland, Some(Inland)),
      (SUPPLEMENTARY_EIDR, Maritime, Border, None),
      (CLEARANCE_FRONTIER, Maritime, Border, None),
      (CLEARANCE_PRE_LODGED, Maritime, Inland, None)
    )

    inlandOrBorderIfOrNotToReset.foreach { data =>
      val additionalType = data._1
      val modeOfTransportCode = data._2
      val actualCachedInlandOrBorder = data._3
      val expectedCachedInlandOrBorder = data._4

      s"AdditionalDeclarationType is $additionalType and" when {
        s"the value selected on /transport-leaving-the-border is $modeOfTransportCode and" when {
          s"the cached InlandOrBorder is $actualCachedInlandOrBorder" should {
            s"${if (expectedCachedInlandOrBorder.isEmpty) "" else "not "} reset InlandOrBorder after a successful bind" in {
              withNewCaching(withRequest(additionalType, withInlandOrBorder(Some(actualCachedInlandOrBorder))).cacheModel)

              val body = Json.obj("transportLeavingTheBorder" -> modeOfTransportCode.value)
              await(controller.submitForm(Normal)(postRequest(body)))

              theCacheModelUpdated.locations.inlandOrBorder mustBe expectedCachedInlandOrBorder
            }
          }
        }
      }
    }
  }
}
