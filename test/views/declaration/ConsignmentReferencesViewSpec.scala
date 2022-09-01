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

package views.declaration

import base.ExportsTestData._
import base.{Injector, TestHelper}
import controllers.declaration.routes
import forms.declaration.ConsignmentReferences
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED}
import forms.{Ducr, Lrn, Mrn}
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.consignment_references
import views.tags.ViewTest

@ViewTest
class ConsignmentReferencesViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val incorrectDUCR = "7GB000000000000-1234512345123451234512345"
  private val consignmentReferencesPage = instanceOf[consignment_references]

  private def createView(maybeForm: Option[Form[ConsignmentReferences]], mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document =
    consignmentReferencesPage(
      mode,
      maybeForm.getOrElse(ConsignmentReferences.form(request.declarationType, request.cacheModel.additionalDeclarationType))
    )(request, messages)

  private def createView()(implicit request: JourneyRequest[_]): Document =
    createView(Some(ConsignmentReferences.form(request.declarationType, request.cacheModel.additionalDeclarationType)))(request)

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

  "Consignment References View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        createView().getElementById("title").text() mustBe messages("declaration.consignmentReferences.header")
      }

      "display section header" in {
        createView().getElementById("section-header").text() must include(messages("declaration.section.1"))
      }

      val createViewWithMode: Mode => Document = mode => createView(None, mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "display empty input with label for DUCR" in {

        val expectedBodyTextListMessageKeys = Seq(
          "declaration.consignmentReferences.ducr.paragraph.bullet1",
          "declaration.consignmentReferences.ducr.paragraph.bullet2",
          "declaration.consignmentReferences.ducr.paragraph.bullet3",
          "declaration.consignmentReferences.ducr.paragraph.bullet4",
          "declaration.consignmentReferences.ducr.paragraph.bullet5"
        )
        val view = createView()

        view.getElementsByAttributeValue("for", "ducr_ducr").text mustBe messages("declaration.consignmentReferences.ducr.info")
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("declaration.consignmentReferences.ducr.paragraph")
        expectedBodyTextListMessageKeys.foreach { messageKey =>
          view.getElementsByClass("govuk-list").get(0) must containMessage(messageKey)
        }
        view.getElementById("ducr_ducr-hint").text mustBe messages("declaration.consignmentReferences.ducr.hint")
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "not display empty input with label for MRN" in {
        createView().getElementsByAttributeValue("for", "mrn") mustBe empty
      }

      "display empty input with label for LRN" in {
        val view = createView()

        view.getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.consignmentReferences.lrn.info")
        view.getElementById("lrn-hint").text() mustBe messages("declaration.consignmentReferences.lrn.hint")
        view.getElementById("lrn").attr("value") mustBe empty
      }

      "display inset text for DUCR" in {
        val expectedInsetText =
          Seq(messages("declaration.consignmentReferences.ducr.inset.1"), messages("declaration.consignmentReferences.ducr.inset.2")).mkString(" ")

        createView().getElementsByClass("govuk-inset-text").get(0).text mustBe expectedInsetText
      }

      "display inset text for LRN" in {
        createView().getElementsByClass("govuk-inset-text").get(1).text() mustBe messages("declaration.consignmentReferences.lrn.inset")
      }
    }

    onJourney(SUPPLEMENTARY) { req =>
      "with AdditionalDeclarationType of SUPPLEMENTARY_SIMPLIFIED" should {
        implicit val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_SIMPLIFIED)))

        "display empty input with label for DUCR" in {
          val view = createView()
          val expectedHintText =
            Seq(messages("declaration.consignmentReferences.supplementary.ducr.hint1"), messages("declaration.consignmentReferences.ducr.hint"))
              .mkString(" ")

          view.getElementsByAttributeValue("for", "ducr_ducr").text() mustBe messages("declaration.consignmentReferences.ducr.info")
          view.getElementById("ducr_ducr-hint").text() mustBe expectedHintText
          view.getElementById("ducr_ducr").attr("value") mustBe empty
        }

        "display empty input with label for MRN" in {
          val view = createView()
          val expectedHintText = Seq(
            messages("declaration.consignmentReferences.supplementary.mrn.hint1"),
            messages("declaration.consignmentReferences.supplementary.mrn.hint2")
          ).mkString(" ")

          view.getElementsByAttributeValue("for", "mrn").text() mustBe messages("declaration.consignmentReferences.supplementary.mrn.info")
          view.getElementById("mrn-hint").text() mustBe expectedHintText
          view.getElementById("mrn").attr("value") mustBe empty
        }

        "display empty input with label for LRN" in {
          val view = createView()

          view.getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.info")
          view.getElementById("lrn-hint").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.hint")
          view.getElementById("lrn").attr("value") mustBe empty
        }

        "do not display inset text for DUCR or LRN" in {
          createView().getElementsByClass("govuk-inset-text").size() mustBe 0
        }
      }

      "with AdditionalDeclarationType of SUPPLEMENTARY_EIDR" should {
        implicit val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_EIDR)))

        "display empty input with label for DUCR" in {
          val view = createView()
          val expectedHintText =
            Seq(messages("declaration.consignmentReferences.supplementary.ducr.hint1"), messages("declaration.consignmentReferences.ducr.hint"))
              .mkString(" ")

          view.getElementsByAttributeValue("for", "ducr_ducr").text() mustBe messages("declaration.consignmentReferences.ducr.info")
          view.getElementById("ducr_ducr-hint").text() mustBe expectedHintText
          view.getElementById("ducr_ducr").attr("value") mustBe empty
        }

        "display empty input with label for EIDR Date Stamp" in {
          val view = createView()

          view.getElementsByAttributeValue("for", "eidrDateStamp").text() mustBe messages("declaration.consignmentReferences.supplementary.eidr.info")
          view.getElementById("eidrDateStamp-hint").text() mustBe messages("declaration.consignmentReferences.supplementary.eidr.hint1")
          view.getElementById("eidrDateStamp").attr("value") mustBe empty
        }

        "display empty input with label for LRN" in {
          val view = createView()

          view.getElementsByAttributeValue("for", "lrn").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.info")
          view.getElementById("lrn-hint").text() mustBe messages("declaration.consignmentReferences.supplementary.lrn.hint")
          view.getElementById("lrn").attr("value") mustBe empty
        }

        "do not display inset text for DUCR or LRN" in {
          createView().getElementsByClass("govuk-inset-text").size() mustBe 0
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Declarant Details' page" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.DeclarantDetailsController.displayPage().url)
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Declaration Type' page" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.AdditionalDeclarationTypeController.displayPage().url)
      }
    }
  }

  "Consignment References View for invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error for empty LRN" in {
        val view =
          createView(
            ConsignmentReferences
              .form(request.declarationType, request.cacheModel.additionalDeclarationType)
              .fillAndValidate(ConsignmentReferences(Ducr(ducr), Lrn("")))
          )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when LRN is longer then 22 characters" in {
        val view = createView(
          ConsignmentReferences
            .form(request.declarationType, request.cacheModel.additionalDeclarationType)
            .fillAndValidate(ConsignmentReferences(Ducr(ducr), Lrn(TestHelper.createRandomAlphanumericString(23))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when LRN contains special character" in {
        val view =
          createView(
            ConsignmentReferences
              .form(request.declarationType, request.cacheModel.additionalDeclarationType)
              .fillAndValidate(ConsignmentReferences(Ducr(ducr), Lrn("#@#$")))
          )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }

      "display error when DUCR is incorrect and LRN empty" in {
        val view =
          createView(
            ConsignmentReferences
              .form(request.declarationType, request.cacheModel.additionalDeclarationType)
              .fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn("")))
          )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when DUCR is incorrect and LRN is longer then 22 characters" in {
        val view = createView(
          ConsignmentReferences
            .form(request.declarationType, request.cacheModel.additionalDeclarationType)
            .fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn(TestHelper.createRandomAlphanumericString(23))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when DUCR is incorrect and LRN contains special character" in {
        val view =
          createView(
            ConsignmentReferences
              .form(request.declarationType, request.cacheModel.additionalDeclarationType)
              .fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), Lrn("$$%")))
          )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")

        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }
    }

    onJourney(SUPPLEMENTARY) { req =>
      "with AdditionalDeclarationType of SUPPLEMENTARY_SIMPLIFIED" should {
        implicit val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_SIMPLIFIED)))

        "display error for empty MRN" in {
          val view =
            createView(
              ConsignmentReferences
                .form(request.declarationType, request.cacheModel.additionalDeclarationType)
                .fillAndValidate(ConsignmentReferences(Ducr(ducr), Lrn(TestHelper.createRandomAlphanumericString(22)), Some(Mrn(""))))
            )

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#mrn")

          view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.mrn.error.empty")
        }

        "display error for invalid MRN" in {
          val view =
            createView(
              ConsignmentReferences
                .form(request.declarationType, request.cacheModel.additionalDeclarationType)
                .fillAndValidate(ConsignmentReferences(Ducr(ducr), Lrn(TestHelper.createRandomAlphanumericString(22)), Some(Mrn("wsfsdf£"))))
            )

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#mrn")

          view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.mrn.error.invalid")
        }
      }

      "with AdditionalDeclarationType of SUPPLEMENTARY_EIDR" should {
        implicit val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_EIDR)))

        "display error for empty EIDR Date Stamp" in {
          val view =
            createView(
              ConsignmentReferences
                .form(request.declarationType, request.cacheModel.additionalDeclarationType)
                .fillAndValidate(ConsignmentReferences(Ducr(ducr), Lrn(TestHelper.createRandomAlphanumericString(22)), None, Some("")))
            )

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eidrDateStamp")

          view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.eidr.error.empty")
        }

        "display error for invalid EIDR Date Stamp" in {
          val view =
            createView(
              ConsignmentReferences
                .form(request.declarationType, request.cacheModel.additionalDeclarationType)
                .fillAndValidate(ConsignmentReferences(Ducr(ducr), Lrn(TestHelper.createRandomAlphanumericString(22)), None, Some("123456789")))
            )

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eidrDateStamp")

          view must containErrorElementWithMessageKey("declaration.consignmentReferences.supplementary.eidr.error.invalid")
        }
      }
    }
  }

  "Consignment References View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in DUCR input" in {
        val view =
          createView(
            ConsignmentReferences
              .form(request.declarationType, request.cacheModel.additionalDeclarationType)
              .fill(ConsignmentReferences(Ducr(ducr), Lrn("")))
          )

        view.getElementById("ducr_ducr").attr("value") mustBe ducr
      }

      "display data in LRN input" in {
        val view = createView(
          ConsignmentReferences
            .form(request.declarationType, request.cacheModel.additionalDeclarationType)
            .fill(ConsignmentReferences(Ducr(""), Lrn(lrn)))
        )

        view.getElementById("lrn").attr("value") mustBe lrn
      }
    }

    onJourney(SUPPLEMENTARY) { req =>
      "with AdditionalDeclarationType of SUPPLEMENTARY_SIMPLIFIED" should {
        implicit val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_SIMPLIFIED)))

        "display data in MRN input" in {
          val view =
            createView(
              ConsignmentReferences
                .form(request.declarationType, request.cacheModel.additionalDeclarationType)
                .fill(ConsignmentReferences(Ducr(ducr), Lrn(lrn), Some(Mrn(mrn))))
            )

          view.getElementById("mrn").attr("value") mustBe mrn
        }
      }

      "with AdditionalDeclarationType of SUPPLEMENTARY_SIMPLIFIED" should {
        implicit val request = journeyRequest(aDeclaration(withType(req.declarationType), withAdditionalDeclarationType(SUPPLEMENTARY_EIDR)))

        "display data in EIDR Date Stamp input" in {
          val view =
            createView(
              ConsignmentReferences
                .form(request.declarationType, request.cacheModel.additionalDeclarationType)
                .fill(ConsignmentReferences(Ducr(ducr), Lrn(lrn), None, Some(eidrDateStamp)))
            )

          view.getElementById("eidrDateStamp").attr("value") mustBe eidrDateStamp
        }
      }
    }
  }
}
