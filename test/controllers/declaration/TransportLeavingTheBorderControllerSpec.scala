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
import base.ExportsTestData.{modifierForPC1040, valuesRequiringToSkipInlandOrBorder}
import controllers.declaration.routes.{
  InlandOrBorderController,
  InlandTransportDetailsController,
  SupervisingCustomsOfficeController,
  WarehouseIdentificationController
}
import controllers.helpers.TransportSectionHelper._
import controllers.helpers.{InlandOrBorderHelper, SupervisingCustomsOfficeHelper}
import forms.declaration.InlandOrBorder.{Border, Inland}
import forms.section3.LocationOfGoods.suffixForGVMS
import forms.declaration.ModeOfTransportCode.{meaningfulModeOfTransportCodes, Maritime, RoRo}
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.{BorderTransport, ModeOfTransportCode, TransportLeavingTheBorder}
import forms.section3.LocationOfGoods
import models.DeclarationType._
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

class TransportLeavingTheBorderControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  private val transportLeavingTheBorder = mock[transport_leaving_the_border]

  private val inlandOrBorderHelper = instanceOf[InlandOrBorderHelper]
  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]

  val controller = new TransportLeavingTheBorderController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mcc,
    transportLeavingTheBorder,
    inlandOrBorderHelper = inlandOrBorderHelper,
    supervisingCustomsOfficeHelper
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aStandardDeclaration)
    when(transportLeavingTheBorder.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(transportLeavingTheBorder)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[TransportLeavingTheBorder] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TransportLeavingTheBorder]])
    verify(transportLeavingTheBorder).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "TransportLeavingTheBorderController.displayOutcomePage" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache is not empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDepartureTransport(ModeOfTransportCode.Rail)))

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(TransportLeavingTheBorder(Some(ModeOfTransportCode.Rail)))
        }
      }
    }
  }

  "TransportLeavingTheBorderController.submitForm" when {

    onEveryDeclarationJourney() { request =>
      "the user does not select any option" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitForm()(postRequest(Json.obj()))
          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }

      s"LocationOfGood's value is present and ends with '$suffixForGVMS'" should {
        val goodsLocation = withGoodsLocation(LocationOfGoods(s"GBAUFEMLHR$suffixForGVMS"))

        "update successfully the cache" when {
          s"ModeOfTransportCode selected is 'RoRo'" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, goodsLocation))

            val body = Json.obj("transportLeavingTheBorder" -> RoRo.value)
            await(controller.submitForm()(postRequest(body)))

            theCacheModelUpdated.transportLeavingBorderCode mustBe Some(RoRo)
            verifyAudit()
          }
        }

        "return 400 (BAD_REQUEST)" when {
          meaningfulModeOfTransportCodes.filter(_ != RoRo).foreach { modeOfTransportCode =>
            s"ModeOfTransportCode is '${modeOfTransportCode}'" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, goodsLocation))

              val body = Json.obj("transportLeavingTheBorder" -> modeOfTransportCode.value)

              val result = controller.submitForm()(postRequest(body))
              status(result) must be(BAD_REQUEST)
              verifyNoAudit()
            }
          }
        }
      }

      postalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
        s"modeOfTransportCode is $modeOfTransportCode" should {
          "reset the cache for 'Departure Transport', 'Border transport' and 'Transport Country'" in {
            val departureTransport = withDepartureTransport(Maritime, "10", "identifier")
            val borderTransport = withBorderTransport(BorderTransport("type", "number"))
            val transportCountry = withTransportCountry(Some("IT"))
            withNewCaching(aDeclarationAfter(request.cacheModel, departureTransport, borderTransport, transportCountry))

            val body = Json.obj("transportLeavingTheBorder" -> modeOfTransportCode.value.value)
            await(controller.submitForm()(postRequest(body)))

            val transport = theCacheModelUpdated.transport
            transport.meansOfTransportOnDepartureType mustBe None
            transport.meansOfTransportOnDepartureIDNumber mustBe None
            transport.meansOfTransportCrossingTheBorderType mustBe None
            transport.meansOfTransportCrossingTheBorderIDNumber mustBe None
            transport.transportCrossingTheBorderNationality mustBe None

            verifyAudit()
          }
        }
      }
    }
  }

  "TransportLeavingTheBorderController.submitForm" when {

    meaningfulModeOfTransportCodes.foreach { modeOfTransportCode =>
      s"ModeOfTransportCode is '${modeOfTransportCode}'" should {
        val body = Json.obj("transportLeavingTheBorder" -> modeOfTransportCode.value)

        onJourney(STANDARD, OCCASIONAL, SUPPLEMENTARY, CLEARANCE) { request =>
          "update the cache after a successful bind" in {
            withNewCaching(request.cacheModel)

            await(controller.submitForm()(postRequest(body)))

            theCacheModelUpdated.transportLeavingBorderCode.value mustBe modeOfTransportCode
            verifyAudit()
          }
        }

        onJourney(STANDARD, OCCASIONAL, SUPPLEMENTARY) { request =>
          "redirect to /warehouse-details after a successful bind" when {
            warehouseRequiredProcedureCodes.foreach { suffix =>
              s"user had entered a Procedure Code ending with '$suffix'" in {
                val item = withItem(anItem(withProcedureCodes(Some(s"10$suffix"))))
                withNewCaching(aDeclarationAfter(request.cacheModel, item))

                val result = controller.submitForm()(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe WarehouseIdentificationController.displayPage
                verifyAudit()
              }
            }
          }

          "redirect to /supervising-customs-office after a successful bind" in {
            withNewCaching(request.cacheModel)

            val result = controller.submitForm()(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe SupervisingCustomsOfficeController.displayPage
            verifyAudit()
          }
        }

        "redirect to /inland-or-border after a successful bind" when {
          "cache contains '1040' as procedure code, '000' as APC and" when {
            additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
              s"AdditionalDeclarationType is $additionalType and" in {
                withNewCaching(withRequest(additionalType, modifierForPC1040).cacheModel)

                val result = controller.submitForm()(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage

                val expectedPage =
                  if (modeOfTransportCode == RoRo) InlandTransportDetailsController.displayPage
                  else InlandOrBorderController.displayPage

                thePageNavigatedTo mustBe expectedPage
                verifyAudit()
              }
            }
          }
        }

        "redirect to to /inland-transport-details after a successful bind" when {
          "cache contains '1040' as procedure code, '000' as APC and" when {
            List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
              "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" in {
                withNewCaching(withRequest(additionalType, modifierForPC1040).cacheModel)

                val result = controller.submitForm()(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe InlandTransportDetailsController.displayPage
                verifyAudit()
              }
            }

            additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
              s"AdditionalDeclarationType is $additionalType and" when {
                "the user has previously entered a value which requires to skip the /inland-or-border page" in {
                  valuesRequiringToSkipInlandOrBorder.foreach { modifier =>
                    initMockNavigatorForMultipleCallsInTheSameTest()
                    val declaration = withRequest(additionalType, modifier, modifierForPC1040).cacheModel

                    // This is a special case for this test that as specified would
                    // instead expect to land on /inland-transport-details
                    val landOnInlandOrBorder =
                      modeOfTransportCode != RoRo && declaration.transportLeavingBorderCode == Some(RoRo)

                    val expectedPage =
                      if (landOnInlandOrBorder) InlandOrBorderController.displayPage
                      else InlandTransportDetailsController.displayPage

                    withNewCaching(declaration)

                    val result = controller.submitForm()(postRequest(body))

                    await(result) mustBe aRedirectToTheNextPage
                    thePageNavigatedTo mustBe expectedPage

                    verifyAudit()
                  }
                }
              }
            }
          }
        }

        onClearance { request =>
          "redirect to the 'Warehouse Identification' page after a successful bind" in {
            withNewCaching(request.cacheModel)

            val result = controller.submitForm()(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe WarehouseIdentificationController.displayPage
            verifyAudit()
          }
        }
      }
    }

    val resetInlandOrBorderConditions = List(
      (STANDARD_FRONTIER, Maritime, Border, Some(Border)),
      (STANDARD_FRONTIER, RoRo, Border, None),
      (STANDARD_PRE_LODGED, Maritime, Border, Some(Border)),
      (OCCASIONAL_FRONTIER, RoRo, Border, None),
      (OCCASIONAL_PRE_LODGED, Maritime, Border, Some(Border)),
      (SUPPLEMENTARY_SIMPLIFIED, Maritime, Inland, Some(Inland)),
      (SUPPLEMENTARY_EIDR, Maritime, Border, None),
      (CLEARANCE_FRONTIER, Maritime, Border, None),
      (CLEARANCE_PRE_LODGED, Maritime, Inland, None)
    )

    resetInlandOrBorderConditions.foreach { data =>
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
              await(controller.submitForm()(postRequest(body)))

              theCacheModelUpdated.locations.inlandOrBorder mustBe expectedCachedInlandOrBorder
            }
          }
        }
      }
    }
  }
}
