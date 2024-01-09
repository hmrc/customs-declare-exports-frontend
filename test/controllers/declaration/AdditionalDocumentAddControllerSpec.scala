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

import base.{AuditedControllerSpec, ControllerSpec, MockTaggedCodes}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.Yes
import forms.declaration.additionaldocuments.AdditionalDocument
import mock.ErrorHandlerMocks
import models.declaration.AdditionalDocuments
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additionalDocuments.additional_document_add

class AdditionalDocumentAddControllerSpec extends ControllerSpec with AuditedControllerSpec with ErrorHandlerMocks with MockTaggedCodes {

  val additionalDocumentAddPage = mock[additional_document_add]

  val controller = new AdditionalDocumentAddController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    taggedAuthCodes,
    taggedAdditionalDocumentCodes,
    additionalDocumentAddPage
  )(ec, auditService)

  val itemId = "itemId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))
    when(additionalDocumentAddPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(additionalDocumentAddPage)
  }

  def theResponseForm: Form[AdditionalDocument] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalDocument]])
    verify(additionalDocumentAddPage).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(additionalDocumentAddPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val additionalDocument = AdditionalDocument(Some("1234"), None, None, None, None, None, None)

  "AdditionalDocumentAddController" should {

    "return 200 (OK)" when {
      "display page method is invoked" in {
        val result = controller.displayPage(itemId)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {
      def verifyBadRequest(incorrectForm: Seq[(String, String)]): HtmlFormat.Appendable = {
        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()
        verifyPageInvoked()
      }

      "user entered an empty document type code" in {
        verifyBadRequest(Seq(("documentTypeCode", "")))
      }

      "user entered an invalid document type code" in {
        verifyBadRequest(Seq(("documentTypeCode", "12345")))
      }

      "user entered measurement unit without quantity" in {
        verifyBadRequest(Seq(("documentWriteOff.measurementUnit", "KGM")))
      }

      "user entered quantity without measurement unit" in {
        verifyBadRequest(Seq(("documentWriteOff.documentQuantity", "1000")))
      }

      "user entered qualifier without measurement unit" in {
        verifyBadRequest(Seq(("documentWriteOff.qualifier", "A"), ("documentWriteOff.documentQuantity", "1000")))
      }

      "user put duplicated item" in {
        withNewCaching(aDeclaration(withItems(anItem(withItemId("itemId"), withAdditionalDocuments(None, additionalDocument)))))

        val duplicatedForm = Seq(("documentTypeCode", "1234"))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
        verifyNoAudit()
      }

      "user reach maximum amount of items" in {
        val additionalDocuments = AdditionalDocuments(Yes, Seq.fill(AdditionalDocuments.maxNumberOfItems)(additionalDocument))
        val item = anItem(withItemId("itemId"), withAdditionalDocuments(additionalDocuments))
        withNewCaching(aDeclaration(withItems(item)))

        val correctForm = Seq(("documentTypeCode", "4321"))

        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user correctly add new item" in {
        val correctForm = Json.toJson(additionalDocument)
        val result = controller.submitForm(itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)
        verifyPageInvoked(0)

        val savedDocuments = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalDocuments)
        savedDocuments mustBe Some(AdditionalDocuments(YesNoAnswer.Yes, Seq(additionalDocument)))
        verifyAudit()
      }
    }

    "AdditionalDocuments isRequired is changed to true if currently set to false" when {
      "user adds a document" in {
        val modifier = withAdditionalDocuments(AdditionalDocuments(YesNoAnswer.No, Seq()))
        withNewCaching(aDeclaration(withItems(anItem(withItemId("itemId"), modifier))))

        val correctForm = Json.toJson(additionalDocument)
        val result = controller.submitForm(itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)
        verifyPageInvoked(0)

        val isRequired = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalDocuments.flatMap(_.isRequired))
        isRequired mustBe Yes
        verifyAudit()
      }
    }
  }
}
