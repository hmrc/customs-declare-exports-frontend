/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.controllers.declaration

import controllers.declaration.DocumentsProducedController
import controllers.util.Remove
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.common.DateSpec.correctDate
import forms.declaration.additionaldocuments.DocumentIdentifierAndPartSpec.correctDocumentIdentifierAndPart
import forms.declaration.additionaldocuments.DocumentWriteOffSpec.correctDocumentWriteOff
import forms.declaration.additionaldocuments.DocumentsProduced
import models.declaration.DocumentsProducedData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, times, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.documents_produced

class DocumentProducedControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val documentProducedPage = mock[documents_produced]

  val controller = new DocumentsProducedController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    documentProducedPage
  )(ec)

  val itemId = "itemId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
    when(documentProducedPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(documentProducedPage)
  }

  val documentsProduced = DocumentsProduced(Some("1234"), None, None, None, None, None, None)

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(documentProducedPage, times(noOfInvocations)).apply(any(), any(), any())(any(), any())

  "Document Produced controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {

        val result = controller.displayPage(itemId)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()
      }

      "display page method is invoked with data in cache" in {

        withNewCaching(aDeclaration(withItems(anItem(withDocumentsProduced(documentsProduced)))))

        val result = controller.displayPage(itemId)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val wrongAction = ("WrongAction", "")

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(wrongAction))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in {

        val incorrectForm = Seq(("documentTypeCode", "12345"), addActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "user put duplicated item" in {

        withNewCaching(aDeclaration(withItems(anItem(withDocumentsProduced(documentsProduced)))))

        val duplicatedForm = Seq(("documentTypeCode", "1234"), addActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "user reach maximum amount of items" in {

        withNewCaching(
          aDeclaration(
            withItems(
              anItem(
                withDocumentsProducedData(
                  DocumentsProducedData(Seq.fill(DocumentsProducedData.maxNumberOfItems)(documentsProduced))
                )
              )
            )
          )
        )

        val correctForm = Seq(("documentTypeCode", "4321"), addActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {

        val incorrectForm = Seq(("documentTypeCode", "12345"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "user put duplicated item" in {

        withNewCaching(aDeclaration(withItems(anItem(withDocumentsProduced(documentsProduced)))))

        val duplicatedForm = Seq(("documentTypeCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "user reach maximum amount of items" in {

        withNewCaching(
          aDeclaration(
            withItems(
              anItem(
                withDocumentsProducedData(
                  DocumentsProducedData(Seq.fill(DocumentsProducedData.maxNumberOfItems)(documentsProduced))
                )
              )
            )
          )
        )

        val correctForm = Seq(("documentTypeCode", "4321"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in {

        val correctForm = Seq(("documentTypeCode", "1234"), addActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
      }

      "user save correct data" in {

        val correctForm = Seq(("documentTypeCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
      }

      "user save empty form without new item" in {

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(saveAndContinueActionUrlEncoded))

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
      }

      "user remove existing item" in {

        withNewCaching(aDeclaration(withItems(anItem(withDocumentsProduced(documentsProduced)))))

        val removeAction = (Remove.toString, "0")

        val result = controller.saveForm(itemId)(postRequestAsFormUrlEncoded(removeAction))

        status(result) mustBe OK
        checkViewInteractions()
      }
    }
  }
}
