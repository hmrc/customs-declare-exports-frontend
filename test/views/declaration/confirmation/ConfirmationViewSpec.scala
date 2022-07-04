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

package views.declaration.confirmation

import base.{Injector, MockAuthAction}
import controllers.routes.{DeclarationDetailsController, FileUploadController, SubmissionsController}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.declaration.submissions.Submission
import models.declaration.submissions.EnhancedStatus._
import org.jsoup.nodes.Document
import org.scalatest.GivenWhenThen
import views.declaration.spec.UnitViewSpec
import views.helpers.Confirmation
import views.helpers.ViewDates.formatTimeDate
import views.html.declaration.confirmation.confirmation_page
import views.tags.ViewTest
import testdata.SubmissionsTestData._

import java.time.{ZoneOffset, ZonedDateTime}

@ViewTest
class ConfirmationViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[confirmation_page]
  private val submissionId = uuid

  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url
  private def filedUploadRoute(mrn: String) = FileUploadController.startFileUpload(mrn).url

  private def createView(submission: Option[Submission], declarationType: AdditionalDeclarationType = STANDARD_FRONTIER): Document = {
    val req = buildVerifiedEmailRequest(request, exampleUser)
    val confirmation = Confirmation(req.email, declarationType.toString, submission)
    page(confirmation)(req, messages)
  }

  "Confirmation View" when {

    "status of last received notification is 'RECEIVED'" should {
      val submission = createSubmission(statuses = Seq(RECEIVED))
      val view = createView(Some(submission))

      "display the expected panel" in {
        val panels = view.getElementsByClass("govuk-panel")
        panels.size mustBe 1

        val children = panels.get(0).children

        And("which should include the expected title")
        children.get(0).tagName mustBe "h1"
        children.get(0).text mustBe messages("declaration.confirmation.received.title")

        And("the provided MRN")
        children.get(1).tagName mustBe "div"
        children.get(1).text mustBe messages("declaration.confirmation.mrn", mrn)
      }

      "display the expected first body paragraph when DUCR and LRN have been defined" in {
        val text = view.getElementsByClass("govuk-body").get(0).text
        text mustBe messages(
          "declaration.confirmation.body.1",
          s" ${messages("declaration.confirmation.body.1.ducr", ducr)}",
          s" ${messages("declaration.confirmation.body.1.lrn", lrn)}",
          mrn
        )
      }

      "display the expected first body paragraph when DUCR have NOT been defined" in {

        val view = createView(Some(submission.copy(ducr = None)))
        val text = view.getElementsByClass("govuk-body").get(0).text
        text mustBe messages("declaration.confirmation.body.1", "", s" ${messages("declaration.confirmation.body.1.lrn", lrn)}", mrn)
      }

      "display the expected 'What happens next' section" in {
        view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.what.happens.next")

        val paragraph1 = view.getElementsByClass("govuk-body").get(1)
        paragraph1.text must include("example@example.com")
        paragraph1.text must include(messages("declaration.confirmation.declaration.details.link"))
        paragraph1.child(1) must haveHref(declarationDetailsRoute)

        val paragraph2 = view.getElementsByClass("govuk-body").get(2)
        paragraph2.text mustBe messages("declaration.confirmation.received.next.2", messages("declaration.confirmation.next.2.link"))
        paragraph2.child(0) must haveHref(appConfig.nationalClearanceHub)
      }

      "display the expected 'What can you do now' section" in {
        view.getElementsByTag("h2").get(1).text mustBe messages("declaration.confirmation.whatYouCanDoNow.heading")

        val paragraph1 = view.getElementsByClass("govuk-body").get(3)
        paragraph1.text mustBe messages("declaration.confirmation.body.2", messages("declaration.confirmation.declaration.details.link"))
        paragraph1.child(0) must haveHref(declarationDetailsRoute)

        val paragraph2 = view.getElementsByClass("govuk-body").get(4)
        paragraph2.text mustBe messages(
          "declaration.confirmation.whatYouCanDoNow.paragraph.2",
          messages("declaration.confirmation.whatYouCanDoNow.paragraph.2.link")
        )
        paragraph2.child(0) must haveHref(filedUploadRoute(notification.mrn))
      }

      "display the expected 'Tell us what you think' section" in {
        view.getElementsByTag("h2").get(2).text mustBe messages("declaration.exitSurvey.header")
      }
    }

    Seq(GOODS_ARRIVED, GOODS_ARRIVED_MESSAGE).foreach { enhancedStatus =>
      s"status of last received notification is '${enhancedStatus}'" should {

        val submission = createSubmission(statuses = Seq(enhancedStatus))
        val view = createView(Some(submission))

        "display the expected panel" in {
          val panels = view.getElementsByClass("govuk-panel")
          panels.size mustBe 1

          val children = panels.get(0).children

          And("which should include the expected title")
          children.get(0).tagName mustBe "h1"
          children.get(0).text mustBe messages("declaration.confirmation.accepted.title")

          And("the provided MRN")
          children.get(1).tagName mustBe "div"
          children.get(1).text mustBe messages("declaration.confirmation.mrn", mrn)
        }

        "display the expected first body paragraph when DUCR and LRN have been defined" in {
          val text = view.getElementsByClass("govuk-body").get(0).text
          text mustBe messages(
            "declaration.confirmation.body.1",
            s" ${messages("declaration.confirmation.body.1.ducr", ducr)}",
            s" ${messages("declaration.confirmation.body.1.lrn", lrn)}",
            mrn
          )
        }

        "display the expected first body paragraph when DUCR have NOT been defined" in {
          val view = createView(Some(submission.copy(ducr = None)))
          val text = view.getElementsByClass("govuk-body").get(0).text
          text mustBe messages("declaration.confirmation.body.1", "", s" ${messages("declaration.confirmation.body.1.lrn", lrn)}", mrn)
        }

        "display the expected second body paragraph" in {
          val paragraph = view.getElementsByClass("govuk-body").get(1)
          paragraph.text mustBe messages("declaration.confirmation.body.2", messages("declaration.confirmation.declaration.details.link"))
          paragraph.child(0) must haveHref(declarationDetailsRoute)
        }

        "display the expected 'What happens next' section" in {
          view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.what.happens.next")

          val paragraph1 = view.getElementsByClass("govuk-body").get(2)
          paragraph1.text must include("example@example.com")
          paragraph1.text must include(messages("declaration.confirmation.declaration.details.link"))
          paragraph1.child(1) must haveHref(declarationDetailsRoute)

          val paragraph2 = view.getElementsByClass("govuk-body").get(3)
          paragraph2.text mustBe messages(
            "declaration.confirmation.accepted.next.2",
            formatTimeDate(submission.enhancedStatusLastUpdated.getOrElse(ZonedDateTime.now(ZoneOffset.UTC))),
            messages("declaration.confirmation.next.2.link")
          )
          paragraph2.child(0) must haveHref(appConfig.nationalClearanceHub)
        }

        "display the expected 'Tell us what you think' section" in {
          view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
        }
      }
    }

    Seq(UNDERGOING_PHYSICAL_CHECK, ADDITIONAL_DOCUMENTS_REQUIRED).foreach { enhancedStatus =>
      s"status of last received notification is '${enhancedStatus}'" should {
        val submission = createSubmission(statuses = Seq(enhancedStatus))
        val view = createView(Some(submission))

        "display the expected title" in {
          view.getElementsByTag("h1").text mustBe messages("declaration.confirmation.needsDocument.title")
        }

        "display the expected warning paragraph" in {
          val expectedText = s"! ${messages("site.warning")} ${messages("declaration.confirmation.needsDocument.warning")}"
          view.getElementsByClass("govuk-warning-text").get(0).text mustBe expectedText
        }

        "display the expected first body paragraph when DUCR and LRN have been defined" in {
          val text = view.getElementsByClass("govuk-body").get(0).text
          text mustBe messages(
            "declaration.confirmation.body.1",
            s" ${messages("declaration.confirmation.body.1.ducr", ducr)}",
            s" ${messages("declaration.confirmation.body.1.lrn", lrn)}",
            mrn
          )
        }

        "display the expected first body paragraph when DUCR have NOT been defined" in {
          val view = createView(Some(submission.copy(ducr = None)))
          val text = view.getElementsByClass("govuk-body").get(0).text
          text mustBe messages("declaration.confirmation.body.1", "", s" ${messages("declaration.confirmation.body.1.lrn", lrn)}", mrn)
        }

        "display the expected second body paragraph" in {
          val paragraph = view.getElementsByClass("govuk-body").get(1)
          paragraph.text mustBe messages(
            "declaration.confirmation.needsDocument.body.2",
            messages("declaration.confirmation.declaration.details.link")
          )
          paragraph.child(0) must haveHref(declarationDetailsRoute)
        }
      }
    }

    "status of last received notification is 'CLEARED' and" when {
      List(STANDARD_FRONTIER, SIMPLIFIED_FRONTIER, OCCASIONAL_FRONTIER, CLEARANCE_FRONTIER).foreach { declarationType =>
        s"the additional declaration type is $declarationType" should {
          val submission = createSubmission(statuses = Seq(CLEARED))
          val view = createView(Some(submission), declarationType)

          "display the expected panel" in {
            val panels = view.getElementsByClass("govuk-panel")
            panels.size mustBe 1

            val children = panels.get(0).children

            And("which should include the expected title")
            children.get(0).tagName mustBe "h1"
            children.get(0).text mustBe messages("declaration.confirmation.cleared.title")

            And("the provided MRN")
            children.get(1).tagName mustBe "div"
            children.get(1).text mustBe messages("declaration.confirmation.mrn", mrn)
          }

          "display the expected first body paragraph when DUCR and LRN have been defined" in {
            val text = view.getElementsByClass("govuk-body").get(0).text
            text mustBe messages(
              "declaration.confirmation.cleared.body.1",
              s" ${messages("declaration.confirmation.body.1.ducr", ducr)}",
              s" ${messages("declaration.confirmation.body.1.lrn", lrn)}"
            )
          }

          "display the expected first body paragraph when DUCR have NOT been defined" in {
            val view = createView(Some(submission.copy(ducr = None)), declarationType)
            val text = view.getElementsByClass("govuk-body").get(0).text
            text mustBe messages("declaration.confirmation.cleared.body.1", "", s" ${messages("declaration.confirmation.body.1.lrn", lrn)}")
          }

          "display the expected 'What you can do now' section" in {
            view.getElementsByTag("h2").get(0).text mustBe messages("declaration.confirmation.cleared.heading")

            val paragraph1 = view.getElementsByClass("govuk-body").get(1)
            paragraph1.text must include(messages("declaration.confirmation.declaration.details.link"))
            paragraph1.child(0) must haveHref(declarationDetailsRoute)

            val paragraph2 = view.getElementsByClass("govuk-body").get(2)
            paragraph2.text mustBe messages("declaration.confirmation.cleared.body.3")
          }

          "display the expected 'Tell us what you think' section" in {
            view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
          }
        }
      }
    }

    "no notification has been received yet" should {
      val view = createView(Some(createSubmission(statuses = Seq.empty[EnhancedStatus])))

      "display the expected title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.confirmation.other.title")
      }

      "display the expected first body paragraph when DUCR and LRN have been defined" in {
        val paragraph = view.getElementsByClass("govuk-body").get(0)
        paragraph.text mustBe messages(
          "declaration.confirmation.other.body.1",
          s" ${messages("declaration.confirmation.body.1.ducr", ducr)}",
          s" ${messages("declaration.confirmation.body.1.lrn", lrn)}",
          messages("declaration.confirmation.other.body.1.link")
        )
        paragraph.child(2) must haveHref(SubmissionsController.displayListOfSubmissions().url)
      }

      "display the expected first body paragraph when DUCR have NOT been defined" in {
        val view = createView(Some(submission.copy(ducr = None)))
        val paragraph = view.getElementsByClass("govuk-body").get(0)
        paragraph.text mustBe messages(
          "declaration.confirmation.other.body.1",
          "",
          s" ${messages("declaration.confirmation.body.1.lrn", lrn)}",
          messages("declaration.confirmation.other.body.1.link")
        )
        paragraph.child(1) must haveHref(SubmissionsController.displayListOfSubmissions().url)
      }

      "display the expected second body paragraph" in {
        val text = view.getElementsByClass("govuk-body").get(1).text
        text mustBe messages("declaration.confirmation.other.body.2")
      }
    }
  }
}
