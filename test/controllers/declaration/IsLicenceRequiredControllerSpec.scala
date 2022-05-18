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
import features.Feature
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.Yes
import forms.declaration.CommodityDetails
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.declarationHolder.AuthorizationTypeCodes
import models.declaration.AdditionalDocuments
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.is_licence_required

class IsLicenceRequiredControllerSpec extends ControllerSpec with OptionValues {

  private val itemId = "itemId"
  private val commodityDetails = CommodityDetails(Some("1234567890"), Some("description"))
  private val declaration = aDeclaration(withItem(anItem(withItemId(itemId), withCommodityDetails(commodityDetails))))

  private val mockPage = mock[is_licence_required]

  private val controller =
    new IsLicenceRequiredController(
      mockAuthAction,
      mockJourneyAction,
      mockFeatureFlagAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockPage
    )(ec)

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(declaration)
    await(controller.displayPage(Mode.Normal, itemId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any[Mode], any[String], captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit =
    reset(mockPage, navigator)
  super.afterEach()

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  "IsLicenceRequired Controller" should {

    onJourney(DeclarationType.STANDARD, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED, DeclarationType.SUPPLEMENTARY) { _ =>
      when {
        mockFeatureSwitchConfig.isFeatureOn(Feature.waiver999L)
      } thenReturn true

      "return 200 (OK)" that {

        "display page method is invoked" in {
          withNewCaching(declaration)

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user submits invalid answer" in {
          withNewCaching(declaration)

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

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

            val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "Yes"): _*))
            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(Mode.Normal, itemId)

          }

          "user submits valid No answer" in {

            withNewCaching(declaration)

            val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "No"): _*))
            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(Mode.Normal, itemId)

          }
        }

        "documents have not been added" when {
          "user submits valid Yes answer" in {

            withNewCaching(declaration)

            val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "Yes"): _*))
            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(Mode.Normal, itemId)

          }

          "user submits valid No answer" when {
            "authorisation from List" in {

              val declaration = aDeclaration(
                withItem(anItem(withItemId(itemId), withCommodityDetails(commodityDetails))),
                withDeclarationHolders(authorisationTypeCode = Some(AuthorizationTypeCodes.codesRequiringDocumentation.head))
              )

              withNewCaching(declaration)

              val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "No"): _*))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(Mode.Normal, itemId)

            }

            "NO authorisation from List" in {

              withNewCaching(declaration)

              val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(Seq("yesNo" -> "No"): _*))
              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe routes.AdditionalDocumentsRequiredController.displayPage(Mode.Normal, itemId)

            }
          }

        }

      }
    }
  }
}
