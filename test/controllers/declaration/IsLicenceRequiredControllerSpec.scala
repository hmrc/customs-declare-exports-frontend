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
import controllers.declaration.routes.{AdditionalDocumentAddController, AdditionalDocumentsController, AdditionalDocumentsRequiredController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.Yes
import forms.declaration.CommodityDetails
import forms.declaration.additionaldocuments.AdditionalDocument
import models.DeclarationType
import models.declaration.AdditionalDocuments
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.is_licence_required

class IsLicenceRequiredControllerSpec extends ControllerSpec with AuditedControllerSpec with MockTaggedCodes with OptionValues {

  private val itemId = "itemId"
  private val commodityDetails = CommodityDetails(Some("1234567890"), Some("description"))
  private val declaration = aDeclaration(withItem(anItem(withItemId(itemId), withCommodityDetails(commodityDetails))))

  private val mockPage = mock[is_licence_required]

  private val controller = new IsLicenceRequiredController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    taggedAuthCodes,
    mockPage
  )(ec, auditService)

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(declaration)
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any[String], captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit =
    reset(mockPage, navigator)
  super.afterEach()

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  "IsLicenceRequired Controller" should {

    onJourney(DeclarationType.STANDARD, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED, DeclarationType.SUPPLEMENTARY) { _ =>
      "return 200 (OK)" that {
        "display page method is invoked" in {
          withNewCaching(declaration)

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "user submits invalid answer" in {
          withNewCaching(declaration)

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "documents have been added" when {
          val declaration = aDeclaration(
            withItem(
              anItem(
                withItemId(itemId),
                withAdditionalDocuments(AdditionalDocuments(Yes, Seq(AdditionalDocument(Some("1234"), None, None, None, None, None, None))))
              )
            )
          )

          "user submits valid Yes answer" in {
            withNewCaching(declaration)

            val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "Yes"): _*))
            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe AdditionalDocumentsController.displayPage(itemId)
            verifyAudit()
          }

          "user submits valid No answer" in {
            withNewCaching(declaration)

            val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "No"): _*))
            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe AdditionalDocumentsController.displayPage(itemId)
            verifyAudit()
          }
        }

        "documents have not been added" when {
          "user submits valid Yes answer" in {
            withNewCaching(declaration)

            val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "Yes"): _*))
            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe AdditionalDocumentAddController.displayPage(itemId)
            verifyAudit()
          }

          "user submits valid No answer" when {
            "authorisation from List" in {
              val declaration = aDeclaration(
                withItem(anItem(withItemId(itemId), withCommodityDetails(commodityDetails))),
                withAuthorisationHolders(authorisationTypeCode = Some(taggedAuthCodes.codesRequiringDocumentation.head))
              )

              withNewCaching(declaration)

              val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "No"): _*))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe AdditionalDocumentAddController.displayPage(itemId)
              verifyAudit()
            }

            "NO authorisation from List" in {
              withNewCaching(declaration)

              val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "No"): _*))
              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe AdditionalDocumentsRequiredController.displayPage(itemId)
              verifyAudit()
            }
          }
        }
      }
    }
  }
}
