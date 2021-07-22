/*
 * Copyright 2021 HM Revenue & Customs
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

package views

import java.time.ZonedDateTime

import scala.collection.JavaConverters.asScalaIteratorConverter

import base.{Injector, OverridableInjector}
import config.featureFlags._
import controllers.routes
import models.declaration.notifications.Notification
import models.declaration.submissions.{Submission, SubmissionStatus}
import models.declaration.submissions.SubmissionStatus._
import org.mockito.Mockito.when
import org.scalatest.Assertion
import play.api.inject.bind
import views.declaration.spec.UnitViewSpec
import views.helpers.{StatusOfSubmission, ViewDates}
import views.html.declaration_details

class DeclarationDetailsViewSpec extends UnitViewSpec with Injector {

  private val submission = Submission("id", "eori", "lrn", Some("mrn"), Some("ducr"), Seq.empty)

  private val now = ZonedDateTime.now

  private val acceptedNotification = Notification("id", "mrn", now, ACCEPTED, Seq.empty)
  private val clearedNotification = Notification("id", "mrn", now, CLEARED, Seq.empty)
  private val rejectedNotification = Notification("id", "mrn", now, REJECTED, Seq.empty)
  private val unknownNotification = Notification("id", "mrn", now, UNKNOWN, Seq.empty)

  private val notifications = List(unknownNotification, clearedNotification, acceptedNotification, rejectedNotification)

  "Declaration details page" should {

    val secureMessagingConfig = mock[SecureMessagingConfig]
    val injector = new OverridableInjector(bind[SecureMessagingConfig].toInstance(secureMessagingConfig))
    val page = injector.instanceOf[declaration_details]

    "contain the navigation banner" when {
      "the Secure Messaging feature flag is enabled" in {
        when(secureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
        val view = page(submission, notifications)(request, messages)

        val banner = view.getElementById("navigation-banner")
        assert(Option(banner).isDefined && banner.childrenSize == 2)

        val elements = banner.children.iterator.asScala.toList
        assert(elements.forall(_.tagName.toLowerCase == "a"))
        elements.head must haveHref(routes.SubmissionsController.displayListOfSubmissions())
        elements.last must haveHref(routes.SecureMessagingController.displayInbox)
      }
    }

    "not contain the navigation banner" when {
      "the Secure Messaging feature flag is disabled" in {
        when(secureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)
        val view = page(submission, notifications)(request, messages)
        Option(view.getElementById("navigation-banner")) mustBe None
      }
    }
  }

  "Declaration details page" when {

    val eadConfig = mock[EadConfig]
    val injector = new OverridableInjector(bind[EadConfig].toInstance(eadConfig))
    val page = injector.instanceOf[declaration_details]

    "the EAD feature flag is enabled" should {
      when(eadConfig.isEadEnabled).thenReturn(true)

      "contain the PDF-for-EAD link for any accepted notification's status" in {
        SubmissionStatus.values
          .filter(eadAcceptableStatuses.contains)
          .foreach(status => verifyPdfForEadLink(unknownNotification.copy(status = status)))
      }

      "not contain the PDF-for-EAD link" when {

        "the notification's status is not an accepted status" in {
          SubmissionStatus.values
            .filterNot(eadAcceptableStatuses.contains)
            .foreach { status =>
              val view = page(submission, List(unknownNotification.copy(status = status)))(request, messages)
              Option(view.getElementById("generate-ead")) mustBe None
            }
        }

        "there is no mrn" in {
          val view = page(submission.copy(mrn = None), notifications)(request, messages)
          Option(view.getElementById("generate-ead")) mustBe None
        }
      }

      def verifyPdfForEadLink(notification: Notification): Assertion = {
        val view = page(submission, List(notification))(request, messages)

        val declarationLink = view.getElementById("generate-ead")
        declarationLink must containMessage("submissions.generateEAD")
        declarationLink must haveHref(controllers.pdf.routes.EADController.generatePdf(submission.mrn.get))
      }
    }

    "the EAD feature flag is disabled" should {
      "not contain the PDF-for-EAD link" in {
        when(eadConfig.isEadEnabled).thenReturn(false)
        val page = injector.instanceOf[declaration_details]
        val view = page(submission, notifications)(request, messages)
        Option(view.getElementById("generate-ead")) mustBe None
      }
    }
  }

  "Declaration details page" should {

    "have correct message keys" in {
      messages must haveTranslationFor("site.backToDeclarations")
      messages must haveTranslationFor("submissions.declarationDetails.title")
      messages must haveTranslationFor("submissions.declarationDetails.mrn")
      messages must haveTranslationFor("submissions.declarationDetails.references")
      messages must haveTranslationFor("submissions.declarationDetails.ducr")
      messages must haveTranslationFor("submissions.declarationDetails.lrn")
    }

    val page = instanceOf[declaration_details]
    val view = page(submission, notifications)(request, messages)

    "display 'Back' button to the 'Submission list' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.backToDeclarations")
      backButton must haveHref(routes.SubmissionsController.displayListOfSubmissions())
    }

    "display page title" in {
      view.getElementsByTag("h1").first must containMessage("submissions.declarationDetails.title")
    }

    "display the 'MRN' hint" in {
      val message = messages("submissions.declarationDetails.mrn")
      val expectedMrnHint = s"$message ${submission.mrn.get}"
      view.getElementsByClass("submission-mrn").first.text mustBe expectedMrnHint
    }

    "display the 'Declaration references' table" in {
      view.getElementsByClass("declaration-details-refs").text mustBe messages("submissions.declarationDetails.references")

      val ducr = view.getElementsByClass("submission-ducr").get(0)
      ducr.getElementsByClass("govuk-summary-list__key").text mustBe messages("submissions.declarationDetails.ducr")
      ducr.getElementsByClass("govuk-summary-list__value").text mustBe submission.ducr.get

      val lrn = view.getElementsByClass("submission-lrn").get(0)
      lrn.getElementsByClass("govuk-summary-list__key").text mustBe messages("submissions.declarationDetails.lrn")
      lrn.getElementsByClass("govuk-summary-list__value").text mustBe submission.lrn
    }

    "always contain the view-declaration link, regardless of the notification's status" when {
      def verifyDeclarationLink(notification: Notification): Assertion = {
        val view = page(submission, List(notification))(request, messages)

        val declarationLink = view.getElementById("view-declaration")
        declarationLink must containMessage("submissions.viewDeclaration")
        declarationLink must haveHref(routes.SubmissionsController.viewDeclaration(submission.uuid))
      }

      SubmissionStatus.values foreach (status => verifyDeclarationLink(unknownNotification.copy(status = status)))
    }

    "display the Declaration Timeline when there is at least one notification for the declaration" in {
      assert(view.getElementsByTag("ol").hasClass("hmrc-timeline"))

      val events = view.getElementsByClass("hmrc-timeline__event")
      events.size mustBe notifications.size

      def dateTimeAsShown(notification: Notification): String =
        ViewDates.formatDateAtTime(notification.dateTimeIssuedInUK)

      notifications.zipWithIndex.foreach {
        case (notification, ix) => {
          val title = events.get(ix).getElementsByTag("h2")
          assert(title.hasClass("hmrc-timeline__event-title"))
          title.text mustBe StatusOfSubmission.asText(notification)

          val datetime = events.get(ix).getElementsByTag("time")
          assert(datetime.hasClass("hmrc-timeline__event-meta"))
          datetime.text mustBe dateTimeAsShown(notification)
        }
      }
    }

    "omit the Declaration Timeline from the page when there are no notifications for the declaration" in {
      val view = page(submission, List.empty)(request, messages)
      val element = view.getElementsByTag("ol")
      assert(element.isEmpty || !element.hasClass("hmrc-timeline"))
    }
  }
}
