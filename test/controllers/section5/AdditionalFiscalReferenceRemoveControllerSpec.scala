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

package controllers.section5

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.section5.routes.{AdditionalFiscalReferencesController, FiscalInformationController}
import forms.common.YesNoAnswer
import forms.section5.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ListItem
import views.html.section5.fiscalInformation.additional_fiscal_reference_remove

class AdditionalFiscalReferenceRemoveControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockRemovePage = mock[additional_fiscal_reference_remove]

  val controller =
    new AdditionalFiscalReferenceRemoveController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockRemovePage)(
      ec,
      auditService
    )

  private val referenceId = ListItem.createId(0, fiscalReference)
  private val item = anItem()
  private val itemWithAdditionalReference = anItem(withItemId(item.id), withAdditionalFiscalReferenceData())

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockRemovePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRemovePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(itemWithAdditionalReference)))
    await(controller.displayPage(item.id, referenceId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theAdditionalReference: AdditionalFiscalReference = {
    val captor = ArgumentCaptor.forClass(classOf[AdditionalFiscalReference])
    verify(mockRemovePage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "AdditionalFiscalReferences Remove Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithAdditionalReference)))

          val result = controller.displayPage(item.id, referenceId)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theAdditionalReference mustBe fiscalReference
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithAdditionalReference)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(item.id, referenceId)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
          verifyNoAudit()
        }

      }
      "return 303 (SEE_OTHER)" when {

        "requested reference id invalid" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id, "ref-id")(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalFiscalReferencesController.displayPage(item.id)
          verifyNoAudit()
        }

        "user submits 'Yes' answer when single additional information exists" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithAdditionalReference)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, referenceId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe FiscalInformationController.displayPage(item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.additionalFiscalReferencesData) mustBe Some(AdditionalFiscalReferencesData(Seq.empty))
          verifyAudit()
        }

        val fiscalReference2 = AdditionalFiscalReference("PL", "54321")
        val itemWithTwoAdditionalReferences =
          anItem(withItemId(item.id), withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(fiscalReference, fiscalReference2))))

        "user submits 'Yes' answer when multiple additional information exists" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithTwoAdditionalReferences)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, referenceId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalFiscalReferencesController.displayPage(item.id)

          theCacheModelUpdated
            .itemBy(item.id)
            .flatMap(_.additionalFiscalReferencesData) mustBe Some(AdditionalFiscalReferencesData(Seq(fiscalReference2)))
          verifyAudit()
        }

        "user submits 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithTwoAdditionalReferences)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(item.id, referenceId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalFiscalReferencesController.displayPage(item.id)

          verifyTheCacheIsUnchanged()
          verifyNoAudit()
        }
      }
    }
  }
}
