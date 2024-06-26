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

package views.section1

import base.ExportsTestData._
import base.{Injector, TestHelper}
import controllers.section1.routes.{AdditionalDeclarationTypeController, DeclarantDetailsController}
import forms.section1.ConsignmentReferences.form
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType.{STANDARD_PRE_LODGED, SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED}
import forms.section1.ConsignmentReferences
import forms.{Ducr, Lrn, Mrn}
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.section1.consignment_references
import views.tags.ViewTest

@ViewTest
class ConsignmentReferencesViewSpec extends PageWithButtonsSpec with Injector {

  val incorrectDUCR = "7GB000000000000-1234512345123451234512345"

  val page = instanceOf[consignment_references]

  override val typeAndViewInstance = (STANDARD, page(form(STANDARD, Some(STANDARD_PRE_LODGED)))(_, _))

  def createView(maybeForm: Option[Form[ConsignmentReferences]])(implicit request: JourneyRequest[_]): Document =
    page(maybeForm.getOrElse(form(request.declarationType, request.cacheModel.additionalDeclarationType)))(request, messages)

  def createView(implicit request: JourneyRequest[_]): Document =
    createView(Some(form(request.declarationType, request.cacheModel.additionalDeclarationType)))(request)

  private def createView(form: Form[ConsignmentReferences])(implicit request: JourneyRequest[_]): Document =
    createView(Some(form))(request)

  "Consignment References" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.consignmentReferences.header")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.info")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet1")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet2")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet3")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet4")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet5")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.inset.1")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.invalid")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.ducr.hint1")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.lrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.inset")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.length")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.error.specialCharacter")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.lrn.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.hint1")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.hint2")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.error.invalid")
    }
  }

  "Consignment References View" should {
    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        createView.getElementById("title").text() mustBe messages("declaration.consignmentReferences.header")
      }

      "display section header" in {
        createView.getElementById("section-header").text() must include(messages("declaration.section.1"))
      }

      "not display 'Exit and return' button" in {
        createView.getElementsContainingText("site.exit_and_complete_later") mustBe empty
      }

      checkSaveAndContinueButtonIsDisplayed(createView)
    }
  }

  "Consignment References View" should {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val view = createView

      "display empty input with label for DUCR" in {
        val expectedBodyTextListMessageKeys = Seq(
          "declaration.consignmentReferences.ducr.paragraph.bullet1",
          "declaration.consignmentReferences.ducr.paragraph.bullet2",
          "declaration.consignmentReferences.ducr.paragraph.bullet3",
          "declaration.consignmentReferences.ducr.paragraph.bullet4",
          "declaration.consignmentReferences.ducr.paragraph.bullet5"
        )

        view.getElementsByAttributeValue("for", "ducr_ducr").text mustBe messages("declaration.consignmentReferences.ducr.info")
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("declaration.consignmentReferences.ducr.paragraph")
        expectedBodyTextListMessageKeys.foreach { messageKey =>
          view.getElementsByClass("govuk-list").get(0) must containMessage(messageKey)
        }
        view.getElementById("ducr_ducr-hint").text mustBe messages("declaration.consignmentReferences.ducr.hint")
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "not display empty input with label for MRN" in {
        view.getElementsByAttributeValue("for", "mrn") mustBe empty
      }

      "display empty input with label for LRN" in {
        view.getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.consignmentReferences.lrn.info")
        view.getElementById("lrn-hint").text() mustBe messages("declaration.consignmentReferences.lrn.hint")
        view.getElementById("lrn").attr("value") mustBe empty
      }

      "display inset text for DUCR" in {
        val expectedInsetText =
          Seq(messages("declaration.consignmentReferences.ducr.inset.1"), messages("declaration.consignmentReferences.ducr.inset.2")).mkString(" ")

        view.getElementsByClass("govuk-inset-text").get(0).text mustBe expectedInsetText
      }

      "display inset text for LRN" in {
        view.getElementsByClass("govuk-inset-text").get(1).text() mustBe messages("declaration.consignmentReferences.lrn.inset")
      }
    }
  }

  "Consignment References View" when {

    "AdditionalDeclarationType is SUPPLEMENTARY_SIMPLIFIED" should {
      val view = createView(withRequest(SUPPLEMENTARY_SIMPLIFIED))

      "display empty input with label for DUCR" in {
        val expectedHintText =
          Seq(messages("declaration.consignmentReferences.supplementary.ducr.hint1"), messages("declaration.consignmentReferences.ducr.hint"))
            .mkString(" ")

        view.getElementsByAttributeValue("for", "ducr_ducr").text() mustBe messages("declaration.consignmentReferences.ducr.info")
        view.getElementById("ducr_ducr-hint").text() mustBe expectedHintText
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "display empty input with label for MRN" in {
        val expectedHintText = Seq(
          messages("declaration.consignmentReferences.supplementary.mrn.hint1"),
          messages("declaration.consignmentReferences.supplementary.mrn.hint2")
        ).mkString(" ")

        view.getElementsByAttributeValue("for", "mrn").text() mustBe messages("declaration.consignmentReferences.supplementary.mrn.info")
        view.getElementById("mrn-hint").text() mustBe expectedHintText
        view.getElementById("mrn").attr("value") mustBe empty
      }

      "display empty input with label for LRN" in {
        view.getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.info")
        view.getElementById("lrn-hint").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.hint")
        view.getElementById("lrn").attr("value") mustBe empty
      }

      "do not display inset text for DUCR or LRN" in {
        view.getElementsByClass("govuk-inset-text").size() mustBe 0
      }
    }

    "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" should {
      val view = createView(withRequest(SUPPLEMENTARY_EIDR))

      "display empty input with label for DUCR" in {
        val expectedHintText =
          Seq(messages("declaration.consignmentReferences.supplementary.ducr.hint1"), messages("declaration.consignmentReferences.ducr.hint"))
            .mkString(" ")

        view.getElementsByAttributeValue("for", "ducr_ducr").text() mustBe messages("declaration.consignmentReferences.ducr.info")
        view.getElementById("ducr_ducr-hint").text() mustBe expectedHintText
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "display empty input with label for EIDR Date Stamp" in {
        view.getElementsByAttributeValue("for", "eidrDateStamp").text() mustBe messages("declaration.consignmentReferences.supplementary.eidr.info")
        view.getElementById("eidrDateStamp-hint").text() mustBe messages("declaration.consignmentReferences.supplementary.eidr.hint1")
        view.getElementById("eidrDateStamp").attr("value") mustBe empty
      }

      "display empty input with label for LRN" in {
        view.getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.info")
        view.getElementById("lrn-hint").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.hint")
        view.getElementById("lrn").attr("value") mustBe empty
      }

      "do not display inset text for DUCR or LRN" in {
        view.getElementsByClass("govuk-inset-text").size() mustBe 0
      }
    }
  }

  "Consignment References View" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Declarant Details' page" in {
        val backButton = createView.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DeclarantDetailsController.displayPage.url)
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Declaration Type' page" in {
        val backButton = createView.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(AdditionalDeclarationTypeController.displayPage.url)
      }
    }
  }

  "Consignment References View for invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error for empty LRN" in {
        val view = viewOnInvalidInput(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(""))))
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when LRN is longer then 22 characters" in {
        val view = viewOnInvalidInput(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(TestHelper.createRandomAlphanumericString(23)))))
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when LRN contains special character" in {
        val view = viewOnInvalidInput(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn("#@#$"))))
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }

      "display error when DUCR is incorrect and LRN empty" in {
        val view = viewOnInvalidInput(ConsignmentReferences(Some(Ducr(incorrectDUCR)), Some(Lrn(""))))
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when DUCR is incorrect and LRN is longer then 22 characters" in {
        val view = viewOnInvalidInput(ConsignmentReferences(Some(Ducr(incorrectDUCR)), Some(Lrn(TestHelper.createRandomAlphanumericString(23)))))
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when DUCR is incorrect and LRN contains special character" in {
        val view = viewOnInvalidInput(ConsignmentReferences(Some(Ducr(incorrectDUCR)), Some(Lrn("$$%"))))
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }
    }
  }

  "Consignment References View for invalid input" when {

    "AdditionalDeclarationType is SUPPLEMENTARY_SIMPLIFIED" should {
      implicit val request = withRequest(SUPPLEMENTARY_SIMPLIFIED)

      "display error for empty MRN" in {
        val view =
          viewOnInvalidInput(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(TestHelper.createRandomAlphanumericString(22))), Some(Mrn(""))))
        view must containErrorElementWithTagAndHref("a", "#mrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.mrn.error.empty")
      }

      "display error for invalid MRN" in {
        val view =
          viewOnInvalidInput(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(TestHelper.createRandomAlphanumericString(22))), Some(Mrn("wsfsdf£"))))
        view must containErrorElementWithTagAndHref("a", "#mrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.mrn.error.invalid")
      }
    }
  }

  "Consignment References View for invalid input" when {

    "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" should {
      implicit val request = withRequest(SUPPLEMENTARY_EIDR)

      "display error for empty EIDR Date Stamp" in {
        val view =
          viewOnInvalidInput(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(TestHelper.createRandomAlphanumericString(22))), None, Some("")))
        view must containErrorElementWithTagAndHref("a", "#eidrDateStamp")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.eidr.error.empty")
      }

      "display error for invalid EIDR Date Stamp" in {
        val view =
          viewOnInvalidInput(
            ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(TestHelper.createRandomAlphanumericString(22))), None, Some("123456789"))
          )
        view must containErrorElementWithTagAndHref("a", "#eidrDateStamp")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.eidr.error.invalid")
      }
    }
  }

  "Consignment References View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in DUCR input" in {
        val consignmentReferences = ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn("")))
        val frm = form(request.declarationType, request.cacheModel.additionalDeclarationType).fill(consignmentReferences)
        val view = createView(frm)
        view.getElementById("ducr_ducr").attr("value") mustBe ducr
      }

      "display data in LRN input" in {
        val consignmentReferences = ConsignmentReferences(Some(Ducr("")), Some(Lrn(lrn)))
        val frm = form(request.declarationType, request.cacheModel.additionalDeclarationType).fill(consignmentReferences)
        val view = createView(frm)
        view.getElementById("lrn").attr("value") mustBe lrn
      }
    }
  }

  "Consignment References View when filled" should {

    "display data in MRN input" when {
      "AdditionalDeclarationType is SUPPLEMENTARY_SIMPLIFIED" in {
        implicit val request = withRequest(SUPPLEMENTARY_SIMPLIFIED)
        val consignmentReferences = ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(lrn)), Some(Mrn(mrn)))
        val frm = form(SUPPLEMENTARY, Some(SUPPLEMENTARY_SIMPLIFIED)).fill(consignmentReferences)
        val view = createView(frm)
        view.getElementById("mrn").attr("value") mustBe mrn
      }
    }

    "display data in EIDR Date Stamp input" when {
      "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" in {
        implicit val request = withRequest(SUPPLEMENTARY_EIDR)
        val consignmentReferences = ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(lrn)), None, Some(eidrDateStamp))
        val frm = form(SUPPLEMENTARY, Some(SUPPLEMENTARY_EIDR)).fill(consignmentReferences)
        val view = createView(frm)
        view.getElementById("eidrDateStamp").attr("value") mustBe eidrDateStamp
      }
    }
  }

  def viewOnInvalidInput(consignmentReferences: ConsignmentReferences)(implicit request: JourneyRequest[_]): Document = {
    val frm = form(request.declarationType, request.cacheModel.additionalDeclarationType).fillAndValidate(consignmentReferences)
    val view = createView(frm)
    view must haveGovukGlobalErrorSummary
    view
  }
}
