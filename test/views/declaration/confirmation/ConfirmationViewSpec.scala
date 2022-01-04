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

import java.time.ZonedDateTime
import base.{Injector, MockAuthAction}
import controllers.routes.{DeclarationDetailsController, SubmissionsController}
import models.declaration.notifications.Notification
import models.declaration.submissions.SubmissionStatus.{ACCEPTED, ADDITIONAL_DOCUMENTS_REQUIRED, QUERY_NOTIFICATION_MESSAGE, RECEIVED}
import org.jsoup.nodes.Document
import org.scalatest.GivenWhenThen
import views.declaration.spec.UnitViewSpec
import views.helpers.Confirmation
import views.helpers.ViewDates.formatTimeDate
import views.html.declaration.confirmation.confirmation_page
import views.tags.ViewTest

@ViewTest
class ConfirmationViewSpec extends UnitViewSpec with GivenWhenThen with Injector with MockAuthAction {

  private val page = instanceOf[confirmation_page]
  private val defaultDucr = "ducr"
  private val defaultLrn = "lrn"
  private val submissionId = "submissionId"

  private val declarationDetailsRoute = DeclarationDetailsController.displayPage(submissionId).url

  private def createView(
    notification: Option[Notification] = None,
    ducr: Option[String] = Some(defaultDucr),
    lrn: Option[String] = Some(defaultLrn)
  ): Document = {
    val req = buildVerifiedEmailRequest(request, exampleUser)
    val confirmation = Confirmation(req.email, submissionId, ducr, lrn, notification)
    page(confirmation)(req, messages)
  }

  "Confirmation View" when {

    "status of last received notification is 'DMSRCV'" should {
      val notification = Notification("actionId", "mrn", ZonedDateTime.now, RECEIVED, List.empty)
      val view = createView(Some(notification))

      "display the expected panel" in {
        val panels = view.getElementsByClass("govuk-panel")
        panels.size mustBe 1

        val children = panels.get(0).children

        And("which should include the expected title")
        children.get(0).tagName mustBe "h1"
        children.get(0).text mustBe messages("declaration.confirmation.received.title")

        And("the provided MRN")
        children.get(1).tagName mustBe "div"
        children.get(1).text mustBe messages("declaration.confirmation.mrn", notification.mrn)
      }

      "display the expected first body paragraph when DUCR and LRN have been defined" in {
        val text = view.getElementsByClass("govuk-body").get(0).text
        text mustBe messages(
          "declaration.confirmation.body.1",
          s" ${messages("declaration.confirmation.body.1.ducr", defaultDucr)}",
          s" ${messages("declaration.confirmation.body.1.lrn", defaultLrn)}",
          notification.mrn
        )
      }

      "display the expected first body paragraph when DUCR and LRN have NOT been defined" in {
        val view = createView(Some(notification), None, None)
        val text = view.getElementsByClass("govuk-body").get(0).text
        text mustBe messages("declaration.confirmation.body.1", "", "", notification.mrn)
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
        paragraph2.text mustBe messages("declaration.confirmation.received.next.2", messages("declaration.confirmation.next.2.link"))
        paragraph2.child(0) must haveHref(appConfig.nationalClearanceHub)
      }

      "display the expected 'Tell us what you think' section" in {
        view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
      }
    }

    "status of last received notification is 'DMSACC'" should {
      val notification = Notification("actionId", "mrn", ZonedDateTime.now, ACCEPTED, List.empty)
      val view = createView(Some(notification))

      "display the expected panel" in {
        val panels = view.getElementsByClass("govuk-panel")
        panels.size mustBe 1

        val children = panels.get(0).children

        And("which should include the expected title")
        children.get(0).tagName mustBe "h1"
        children.get(0).text mustBe messages("declaration.confirmation.accepted.title")

        And("the provided MRN")
        children.get(1).tagName mustBe "div"
        children.get(1).text mustBe messages("declaration.confirmation.mrn", notification.mrn)
      }

      "display the expected first body paragraph when DUCR and LRN have been defined" in {
        val text = view.getElementsByClass("govuk-body").get(0).text
        text mustBe messages(
          "declaration.confirmation.body.1",
          s" ${messages("declaration.confirmation.body.1.ducr", defaultDucr)}",
          s" ${messages("declaration.confirmation.body.1.lrn", defaultLrn)}",
          notification.mrn
        )
      }

      "display the expected first body paragraph when DUCR and LRN have NOT been defined" in {
        val view = createView(Some(notification), None, None)
        val text = view.getElementsByClass("govuk-body").get(0).text
        text mustBe messages("declaration.confirmation.body.1", "", "", notification.mrn)
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
          formatTimeDate(notification.dateTimeIssued),
          messages("declaration.confirmation.next.2.link")
        )
        paragraph2.child(0) must haveHref(appConfig.nationalClearanceHub)
      }

      "display the expected 'Tell us what you think' section" in {
        view.getElementsByTag("h2").get(1).text mustBe messages("declaration.exitSurvey.header")
      }
    }

    "status of last received notification is 'DMSCTL' or 'DMSDOC'" should {
      val notification = Notification("actionId", "mrn", ZonedDateTime.now, ADDITIONAL_DOCUMENTS_REQUIRED, List.empty)
      val view = createView(Some(notification))

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
          s" ${messages("declaration.confirmation.body.1.ducr", defaultDucr)}",
          s" ${messages("declaration.confirmation.body.1.lrn", defaultLrn)}",
          notification.mrn
        )
      }

      "display the expected first body paragraph when DUCR and LRN have NOT been defined" in {
        val view = createView(Some(notification), None, None)
        val text = view.getElementsByClass("govuk-body").get(0).text
        text mustBe messages("declaration.confirmation.body.1", "", "", notification.mrn)
      }

      "display the expected second body paragraph" in {
        val paragraph = view.getElementsByClass("govuk-body").get(1)
        paragraph.text mustBe messages("declaration.confirmation.needsDocument.body.2", messages("declaration.confirmation.declaration.details.link"))
        paragraph.child(0) must haveHref(declarationDetailsRoute)
      }
    }

    "status of last received notification is not one of 'DMSRCV', 'DMSACC', 'DMSCTL' or 'DMSDOC'" should {
      val notification = Notification("actionId", "mrn", ZonedDateTime.now, QUERY_NOTIFICATION_MESSAGE, List.empty)
      val view = createView(Some(notification))

      "display the expected title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.confirmation.other.title")
      }

      "display the expected first body paragraph when DUCR and LRN have been defined" in {
        val paragraph = view.getElementsByClass("govuk-body").get(0)
        paragraph.text mustBe messages(
          "declaration.confirmation.other.body.1",
          s" ${messages("declaration.confirmation.body.1.ducr", defaultDucr)}",
          s" ${messages("declaration.confirmation.body.1.lrn", defaultLrn)}",
          messages("declaration.confirmation.other.body.1.link")
        )
        paragraph.child(0) must haveHref(SubmissionsController.displayListOfSubmissions().url)
      }

      "display the expected first body paragraph when DUCR and LRN have NOT been defined" in {
        val view = createView(Some(notification), None, None)
        val paragraph = view.getElementsByClass("govuk-body").get(0)
        paragraph.text mustBe messages("declaration.confirmation.other.body.1", "", "", messages("declaration.confirmation.other.body.1.link"))
        paragraph.child(0) must haveHref(SubmissionsController.displayListOfSubmissions().url)
      }

      "display the expected second body paragraph" in {
        val text = view.getElementsByClass("govuk-body").get(1).text
        text mustBe messages("declaration.confirmation.other.body.2")
      }
    }

    "no notification has been received yet" should {
      val view = createView()

      "display the expected title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.confirmation.other.title")
      }

      "display the expected first body paragraph when DUCR and LRN have been defined" in {
        val paragraph = view.getElementsByClass("govuk-body").get(0)
        paragraph.text mustBe messages(
          "declaration.confirmation.other.body.1",
          s" ${messages("declaration.confirmation.body.1.ducr", defaultDucr)}",
          s" ${messages("declaration.confirmation.body.1.lrn", defaultLrn)}",
          messages("declaration.confirmation.other.body.1.link")
        )
        paragraph.child(0) must haveHref(SubmissionsController.displayListOfSubmissions().url)
      }

      "display the expected first body paragraph when DUCR and LRN have NOT been defined" in {
        val view = createView(None, None, None)
        val paragraph = view.getElementsByClass("govuk-body").get(0)
        paragraph.text mustBe messages("declaration.confirmation.other.body.1", "", "", messages("declaration.confirmation.other.body.1.link"))
        paragraph.child(0) must haveHref(SubmissionsController.displayListOfSubmissions().url)
      }

      "display the expected second body paragraph" in {
        val text = view.getElementsByClass("govuk-body").get(1).text
        text mustBe messages("declaration.confirmation.other.body.2")
      }
    }
  }
}
