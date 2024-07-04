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

import base.{AuditedControllerSpec, ControllerSpec, Injector}
import connectors.CodeLinkConnector
import controllers.declaration.routes.NactCodeSummaryController
import forms.declaration.NactCode
import forms.declaration.NactCode.nactCodeKey
import forms.declaration.ZeroRatedForVat._
import forms.section4.NatureOfTransaction.{allowedTypes, BusinessPurchase, Sale}
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.ProcedureCodesData.lowValueDeclaration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.zero_rated_for_vat

import scala.concurrent.Future

class ZeroRatedForVatControllerSpec extends ControllerSpec with AuditedControllerSpec with Injector with OptionValues {

  val zeroRatedForVatPage = mock[zero_rated_for_vat]
  val codeLinkConnector = mock[CodeLinkConnector]

  val controller =
    new ZeroRatedForVatController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, codeLinkConnector, zeroRatedForVatPage)(
      ec,
      auditService
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()

    when(zeroRatedForVatPage(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(codeLinkConnector.getValidProcedureCodesForTag(any())).thenReturn(Seq.empty)
  }

  override protected def afterEach(): Unit = {
    reset(auditService, zeroRatedForVatPage)
    super.afterEach()
  }

  def theResponseForm: Form[NactCode] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[NactCode]])
    verify(zeroRatedForVatPage)(any(), formCaptor.capture(), any())(any(), any())
    formCaptor.getValue
  }

  private val lowValueItem = anItem(withItemId("id"), withProcedureCodes(additionalProcedureCodes = List(lowValueDeclaration)))

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(OCCASIONAL), withItem(lowValueItem)))
    await(controller.displayPage(lowValueItem.id)(request))
    theResponseForm
  }

  "ZeroRatedForVatController for 'low-value' declarations" when {

    List(OCCASIONAL, SIMPLIFIED).foreach { declarationType =>
      s"declaration type is $declarationType" should {
        val declaration = aDeclaration(withType(declarationType), withItem(lowValueItem))

        "return 200 (OK)" when {

          "displayPage method is invoked with empty cache" in {
            withNewCaching(declaration)

            val result = controller.displayPage(lowValueItem.id)(getRequest())
            status(result) must be(OK)
          }

          "displayPage method is invoked with data in cache" in {
            val item = anItem(
              withItemId("id"),
              withNactExemptionCode(NactCode(VatZeroRatedYes)),
              withProcedureCodes(additionalProcedureCodes = List(lowValueDeclaration))
            )
            withNewCaching(aDeclaration(withType(declarationType), withItem(item)))

            val result = controller.displayPage(item.id)(getRequest())
            status(result) must be(OK)
          }
        }

        "return 303 (SEE_OTHER)" when {
          List(VatZeroRatedYes, VatZeroRatedReduced, VatZeroRatedExempt, VatZeroRatedPaid).foreach { vatZeroRated =>
            s"the selection is $vatZeroRated" in {
              withNewCaching(declaration)

              val correctForm = Json.obj(nactCodeKey -> vatZeroRated)
              val result = controller.submitForm(lowValueItem.id)(postRequest(correctForm))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(lowValueItem.id)
            }
          }
        }

        "return 400 (BAD_REQUEST)" when {

          "no selection is made" in {
            withNewCaching(declaration)

            val wrongForm = Json.obj("zeroRatedForVat" -> "")
            val result = controller.submitForm(lowValueItem.id)(postRequest(wrongForm))

            status(result) must be(BAD_REQUEST)
            verifyNoAudit()
          }

          "incorrect data are submitted" in {
            withNewCaching(declaration)

            val wrongForm = Json.obj("zeroRatedForVat" -> "wrong")
            val result = controller.submitForm(lowValueItem.id)(postRequest(wrongForm))

            status(result) must be(BAD_REQUEST)
            verifyNoAudit()
          }
        }
      }
    }
  }

  "ZeroRatedForVatController for 'eligible for Zero VAT' STANDARD declarations" when {
    List(BusinessPurchase, Sale).foreach { natureOfTransaction =>
      s"'Nature of transaction' is $natureOfTransaction" should {
        val item = anItem()
        val declaration = aDeclaration(withNatureOfTransaction(natureOfTransaction), withItem(item))

        "return 200 (OK)" when {

          "displayPage method is invoked with empty cache" in {
            withNewCaching(declaration)

            val result = controller.displayPage(item.id)(getRequest())
            status(result) must be(OK)
          }

          "displayPage method is invoked with data in cache" in {
            val item = anItem(withItemId("id"), withNactExemptionCode(NactCode(VatZeroRatedYes)))
            withNewCaching(aDeclaration(withNatureOfTransaction(natureOfTransaction), withItem(item)))

            val result = controller.displayPage(item.id)(getRequest())
            status(result) must be(OK)
          }
        }

        "return 303 (SEE_OTHER)" when {
          List(VatZeroRatedYes, VatZeroRatedReduced, VatZeroRatedExempt, VatZeroRatedPaid).foreach { vatZeroRated =>
            s"the selection is $vatZeroRated" in {
              withNewCaching(declaration)

              val correctForm = Json.obj(nactCodeKey -> vatZeroRated)
              val result = controller.submitForm(item.id)(postRequest(correctForm))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)
              verifyAudit()
            }
          }
        }

        "return 400 (BAD_REQUEST)" when {

          "no selection is made" in {
            withNewCaching(declaration)

            val wrongForm = Json.obj("zeroRatedForVat" -> "")
            val result = controller.submitForm(item.id)(postRequest(wrongForm))

            status(result) must be(BAD_REQUEST)
            verifyNoAudit()
          }

          "incorrect data are submitted" in {
            withNewCaching(declaration)

            val wrongForm = Json.obj("zeroRatedForVat" -> "wrong")
            val result = controller.submitForm(item.id)(postRequest(wrongForm))

            status(result) must be(BAD_REQUEST)
            verifyNoAudit()
          }
        }
      }
    }
  }

  "ZeroRatedForVatController" should {
    "be redirected to /national-additional-codes-list" when {
      val item = anItem()

      "the declaration is NOT 'low-value' and" when {

        List(OCCASIONAL, SIMPLIFIED).foreach { declarationType =>
          s"the declaration type is $declarationType and" when {
            val declaration = aDeclaration(withType(declarationType), withItem(item))

            "the 'displayPage' method is invoked" in {
              verifyRedirect(declaration, controller.displayPage(item.id)(getRequest()))
            }

            "the 'submitForm' method is invoked" in {
              verifyRedirect(declaration, controller.submitForm(item.id)(postRequest(JsString(""))))
            }
          }
        }
      }

      "the STANDARD declaration is NOT 'eligible for Zero VAT' and" when {
        allowedTypes.diff(Set(BusinessPurchase, Sale)).foreach { natureOfTransaction =>
          s"'Nature of transaction is $natureOfTransaction and" when {
            val declaration = aDeclaration(withNatureOfTransaction(natureOfTransaction), withItem(item))

            "the 'displayPage' method is invoked" in {
              verifyRedirect(declaration, controller.displayPage(item.id)(getRequest()))
            }

            "the 'submitForm' method is invoked" in {
              verifyRedirect(declaration, controller.submitForm(item.id)(postRequest(JsString(""))))
            }
          }
        }
      }

      def verifyRedirect(declaration: ExportsDeclaration, fun: => Future[Result]): Assertion = {
        withNewCaching(declaration)

        val result = fun

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(NactCodeSummaryController.displayPage(item.id).url)

        theCacheModelUpdated.items.head.nactExemptionCode mustBe None
      }
    }
  }
}
