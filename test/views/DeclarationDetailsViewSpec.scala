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

package views

import base.{ExportsTestData, Injector, OverridableInjector, RequestBuilder}
import config.featureFlags._
import controllers.routes
import models.declaration.notifications.Notification
import models.declaration.submissions.SubmissionStatus._
import models.declaration.submissions.{Submission, SubmissionStatus}
import models.requests.VerifiedEmailRequest
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import org.scalatest.{Assertion, GivenWhenThen}
import play.api.inject.bind
import views.declaration.spec.UnitViewSpec
import views.helpers.{StatusOfSubmission, ViewDates}
import views.html.declaration_details

import java.time.ZonedDateTime
import scala.collection.JavaConverters.asScalaIteratorConverter

class DeclarationDetailsViewSpec extends UnitViewSpec with GivenWhenThen with Injector {

  private val msgKey = "submissions.declarationDetails"

  private val mrn = "mrn"
  private val now = ZonedDateTime.now

  private val user = ExportsTestData.newUser(ExportsTestData.eori, "Id")
  private val testEmail = "testEmail@mail.org"

  private val uuid = "uuid"
  private val submission = Submission(uuid, "eori", "lrn", Some(mrn), Some("ducr"), Seq.empty)

  private val dmsqry1Notification = Notification("id1", mrn, now, QUERY_NOTIFICATION_MESSAGE, Seq.empty)
  private val dmsqry2Notification = Notification("id2", mrn, now, QUERY_NOTIFICATION_MESSAGE, Seq.empty)
  private val dmsdocNotification = Notification("id", mrn, now, ADDITIONAL_DOCUMENTS_REQUIRED, Seq.empty)
  private val dmsctlNotification = Notification("id", mrn, now, UNDERGOING_PHYSICAL_CHECK, Seq.empty)
  private val acceptedNotification = Notification("id", mrn, now, ACCEPTED, Seq.empty)

  private val dmsrejNotification = Notification("id", mrn, now, REJECTED, Seq.empty)

  // Since the notification list is reverse-ordered (most to least recent) in TimelineEvents...

  // 1. order of dmsqry1Notification and dmsqry2Notification is relevant.
  //    Only the TimelineEvent instance generated from dmsqry2Notification will have an Html content,
  //    being placed on the timeline before the instance from dmsqry1Notification.

  // 2. order of dmsdocNotification and dmsctlNotification is relevant.
  //    Only the TimelineEvent instance generated from dmsctlNotification will have an Html content,
  //    being placed on the timeline before the instance from dmsdocNotification.

  // 3. button for dmsctlNotification will be a primary button, while button for dmsqry2Notification
  //    will be a secondary button being placed on the timeline after the former.

  private val notifications = List(dmsqry1Notification, dmsqry2Notification, dmsdocNotification, dmsctlNotification, acceptedNotification)

  private def verifiedEmailRequest(email: String = testEmail): VerifiedEmailRequest[_] =
    VerifiedEmailRequest(RequestBuilder.buildAuthenticatedRequest(request, user), email)

  "Declaration details page" should {

    val secureMessagingConfig = mock[SecureMessagingConfig]
    val injector = new OverridableInjector(bind[SecureMessagingConfig].toInstance(secureMessagingConfig))
    val page = injector.instanceOf[declaration_details]

    "contain the navigation banner" when {
      "the Secure Messaging feature flag is enabled" in {
        when(secureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
        val view = page(submission, notifications)(verifiedEmailRequest(), messages)

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
        val view = page(submission, notifications)(verifiedEmailRequest(), messages)
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
          .foreach(status => verifyPdfForEadLink(dmsdocNotification.copy(status = status)))
      }

      "not contain the PDF-for-EAD link" when {

        "the notification's status is not an accepted status" in {
          SubmissionStatus.values
            .filterNot(eadAcceptableStatuses.contains)
            .foreach { status =>
              val view = page(submission, List(dmsdocNotification.copy(status = status)))(verifiedEmailRequest(), messages)
              Option(view.getElementById("generate-ead")) mustBe None
            }
        }

        "there is no mrn" in {
          val view = page(submission.copy(mrn = None), notifications)(verifiedEmailRequest(), messages)
          Option(view.getElementById("generate-ead")) mustBe None
        }
      }

      def verifyPdfForEadLink(notification: Notification): Assertion = {
        val view = page(submission, List(notification))(verifiedEmailRequest(), messages)

        val declarationLink = view.getElementById("generate-ead")
        declarationLink must containMessage("submissions.generateEAD")
        declarationLink must haveHref(controllers.pdf.routes.EADController.generatePdf(submission.mrn.get))
      }
    }

    "the EAD feature flag is disabled" should {
      "not contain the PDF-for-EAD link" in {
        when(eadConfig.isEadEnabled).thenReturn(false)
        val page = injector.instanceOf[declaration_details]
        val view = page(submission, notifications)(verifiedEmailRequest(), messages)
        Option(view.getElementById("generate-ead")) mustBe None
      }
    }
  }

  "Declaration details page" should {

    "have correct message keys" in {
      messages must haveTranslationFor("site.backToDeclarations")
      messages must haveTranslationFor(s"${msgKey}.title")
      messages must haveTranslationFor(s"${msgKey}.mrn")
      messages must haveTranslationFor(s"${msgKey}.references")
      messages must haveTranslationFor(s"${msgKey}.ducr")
      messages must haveTranslationFor(s"${msgKey}.lrn")
      messages must haveTranslationFor(s"${msgKey}.fix.resubmit.button")
      messages must haveTranslationFor(s"${msgKey}.upload.files.button")
      messages must haveTranslationFor(s"${msgKey}.upload.files.title")
      messages must haveTranslationFor(s"${msgKey}.upload.files.hint")
      messages must haveTranslationFor(s"${msgKey}.view.queries.button")
    }

    "have correct message keys for 'read more' section" in {
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.header")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.paragraph")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.submitted.header")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.submitted.paragraph")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.decHasError.header")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.decHasError.paragraph")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.accepted.header")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.accepted.paragraph")

      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.header")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.paragraph.1")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.paragraph.2")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.bulletPoint.1")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.bulletPoint.2")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.bulletPoint.3")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.paragraph.3")

      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.queryRaised.header")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.queryRaised.paragraph.1")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.queryRaised.paragraph.2")

      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.header")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.paragraph.1")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.paragraph.2")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.bulletPoint.1")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.bulletPoint.2")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.bulletPoint.3")
      messages must haveTranslationFor(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.paragraph.3")
    }

    val dummyInboxLink = "dummyInboxLink"

    val secureMessagingInboxConfig = mock[SecureMessagingInboxConfig]
    when(secureMessagingInboxConfig.sfusInboxLink).thenReturn(dummyInboxLink)

    val dummySfusLink = "dummyInboxLink"

    val sfusConfig = mock[SfusConfig]
    when(sfusConfig.isSfusUploadEnabled).thenReturn(true)
    when(sfusConfig.sfusUploadLink).thenReturn(dummySfusLink)

    val injector =
      new OverridableInjector(bind[SecureMessagingInboxConfig].toInstance(secureMessagingInboxConfig), bind[SfusConfig].toInstance(sfusConfig))

    val page = injector.instanceOf[declaration_details]
    val view = page(submission, notifications)(verifiedEmailRequest(), messages)

    "display 'Back' button to the 'Submission list' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.backToDeclarations")
      backButton must haveHref(routes.SubmissionsController.displayListOfSubmissions())
    }

    "display page title" in {
      view.getElementsByTag("h1").first must containMessage(s"${msgKey}.title")
    }

    "display the 'MRN' hint" in {
      val message = messages(s"${msgKey}.mrn")
      val expectedMrnHint = s"$message ${submission.mrn.get}"
      view.getElementsByClass("submission-mrn").first.text mustBe expectedMrnHint
    }

    "display the 'Declaration references' table" in {
      view.getElementsByClass("declaration-details-refs").get(0).text mustBe messages(s"${msgKey}.references")

      val ducr = view.getElementsByClass("submission-ducr").get(0)
      ducr.getElementsByClass("govuk-summary-list__key").text mustBe messages(s"${msgKey}.ducr")
      ducr.getElementsByClass("govuk-summary-list__value").text mustBe submission.ducr.get

      val lrn = view.getElementsByClass("submission-lrn").get(0)
      lrn.getElementsByClass("govuk-summary-list__key").text mustBe messages(s"${msgKey}.lrn")
      lrn.getElementsByClass("govuk-summary-list__value").text mustBe submission.lrn
    }

    SubmissionStatus.values.filter(_ != REJECTED) foreach { status =>
      s"contain the view-declaration link when notification's status is ${status}" in {
        val view = page(submission, List(dmsdocNotification.copy(status = status)))(verifiedEmailRequest(), messages)

        val declarationLink = view.getElementById("view-declaration")
        declarationLink must containMessage("submissions.viewDeclaration")
        declarationLink must haveHref(routes.SubmissionsController.viewDeclaration(submission.uuid))
      }
    }

    s"not contain the view-declaration link when notification's status is REJECTED" in {
      val view = page(submission, List(dmsdocNotification.copy(status = REJECTED)))(verifiedEmailRequest(), messages)

      Option(view.getElementById("view-declaration")) mustBe None
    }

    "display the Declaration Timeline when there is at least one notification for the declaration" in {
      assert(view.getElementsByTag("ol").hasClass("hmrc-timeline"))

      And("the Timeline should display an event for each notification")
      val events = view.getElementsByClass("hmrc-timeline__event")
      events.size mustBe notifications.size

      def dateTimeAsShown(notification: Notification): String =
        ViewDates.formatDateAtTime(notification.dateTimeIssuedInUK)

      notifications.reverse.zipWithIndex.foreach {
        case (notification, ix) =>
          And("each Timeline event should always include a title")
          val title = events.get(ix).getElementsByTag("h2")
          assert(title.hasClass("hmrc-timeline__event-title"))
          title.text mustBe StatusOfSubmission.asText(notification)

          And("a date and time")
          val datetime = events.get(ix).getElementsByTag("time")
          assert(datetime.hasClass("hmrc-timeline__event-meta"))
          datetime.text mustBe dateTimeAsShown(notification)
      }
    }

    "display on the Declaration Timeline events with content" which {

      "must include one primary button and one secondary button" when {

        "a DMSCTL notification is more recent than a DMSQRY notification" in {
          val events = eventsOnTimeline(notifications)
          content(events.get(0)).size mustBe 0
          verifyUploadFilesContent(content(events.get(1)), false)
          content(events.get(2)).size mustBe 0
          verifyViewQueriesContent(content(events.get(3)), true)
          content(events.get(4)).size mustBe 0
        }

        "a DMSDOC notification is more recent than a DMSQRY notification" in {
          val notifications = List(dmsqry2Notification, dmsdocNotification)
          val events = eventsOnTimeline(notifications)
          verifyUploadFilesContent(content(events.get(0)), false)
          verifyViewQueriesContent(content(events.get(1)), true)
        }

        "a DMSQRY notification is more recent than a DMSCTL notification" in {
          val notifications = List(dmsctlNotification, dmsqry2Notification)
          val events = eventsOnTimeline(notifications)
          verifyViewQueriesContent(content(events.get(0)), false)
          verifyUploadFilesContent(content(events.get(1)), true)
        }

        "a DMSQRY notification is more recent than a DMSDOC notification" in {
          val notifications = List(dmsdocNotification, dmsqry2Notification)
          val events = eventsOnTimeline(notifications)
          verifyViewQueriesContent(content(events.get(0)), false)
          verifyUploadFilesContent(content(events.get(1)), true)
        }

        "one notification at least is a DMSREJ notification in addition to a DMSQRY notification" in {
          val notifications = List(dmsqry2Notification, dmsdocNotification, dmsrejNotification)
          val events = eventsOnTimeline(notifications)
          verifyRejectedContent(content(events.get(0)))
          content(events.get(1)).size mustBe 0
          verifyViewQueriesContent(content(events.get(2)), true)
        }
      }

      "must only include one primary button and no secondary buttons" when {

        "one notification at least is a DMSREJ notification in addition to only DMSCTL and/or DMSDOC notifications" in {
          val notifications = List(dmsdocNotification, dmsctlNotification, dmsrejNotification)
          val events = eventsOnTimeline(notifications)
          verifyRejectedContent(content(events.get(0)))
          content(events.get(1)).size mustBe 0
          content(events.get(2)).size mustBe 0
        }

        "there is one only DMSREJ notification" in {
          val events = eventsOnTimeline(List(dmsrejNotification))
          verifyRejectedContent(content(events.get(0)))
        }

        "there is one only DMSDOC notification" in {
          val events = eventsOnTimeline(List(dmsdocNotification))
          verifyUploadFilesContent(content(events.get(0)), false)
        }

        "there is one only DMSCTL notification" in {
          val events = eventsOnTimeline(List(dmsctlNotification))
          verifyUploadFilesContent(content(events.get(0)), false)
        }

        "there is one only DMSQRY notification" in {
          val events = eventsOnTimeline(List(dmsqry2Notification))
          verifyViewQueriesContent(content(events.get(0)), false)
        }
      }

      def eventsOnTimeline(notifications: List[Notification]): Elements = {
        val view = page(submission, notifications)(verifiedEmailRequest(), messages)
        view.getElementsByClass("hmrc-timeline__event")
      }

      def content(element: Element): Elements = element.getElementsByClass("hmrc-timeline__event-content")

      def verifyRejectedContent(content: Elements): Assertion = {
        val rejectedElements = content.get(0).children
        rejectedElements.size mustBe 1

        And("the 'Fix and resubmit' content, should include a primary link-button to the RejectedNotificationsController")
        val call = controllers.routes.RejectedNotificationsController.displayPage(uuid).url
        verifyButton(rejectedElements.get(0), false, "fix.resubmit", call)
      }

      def verifyUploadFilesContent(content: Elements, buttonIsSecondary: Boolean): Assertion = {
        val uploadFilesElements = content.get(0).getElementById("upload-files-section").children
        uploadFilesElements.size mustBe 2

        And("the 'Documents required' content, when defined, should include a link-button to SFUS")
        verifyButton(uploadFilesElements.get(0), buttonIsSecondary, "upload.files", controllers.routes.FileUploadController.startFileUpload(mrn).url)

        And("and an expander")
        val details = uploadFilesElements.get(1)
        details.getElementsByTag("summary").text mustBe messages(s"${msgKey}.upload.files.title")
        details.getElementsByClass("govuk-details__text").text mustBe messages(s"${msgKey}.upload.files.hint")
      }

      def verifyViewQueriesContent(content: Elements, buttonIsSecondary: Boolean): Assertion = {
        val viewQueriesElements = content.get(0).children
        viewQueriesElements.size mustBe 1

        And("the 'View queries' content, when defined, should include a link-button to the Secure-Messaging Inbox")
        verifyButton(viewQueriesElements.get(0), buttonIsSecondary, "view.queries", dummyInboxLink)
      }

      def verifyButton(button: Element, buttonIsSecondary: Boolean, msgId: String, href: String): Assertion = {
        button.hasClass("govuk-button") mustBe true
        And(s"the link-button should be a ${if (buttonIsSecondary) "secondary" else "primary"} one")
        button.hasClass("govuk-button--secondary") mustBe buttonIsSecondary
        button.text mustBe messages(s"$msgKey.$msgId.button")
        button.attr("href") mustBe href
      }
    }

    "display 'read more' section header" in {
      view.getElementsByClass("declaration-details-refs").get(1).text mustBe messages(s"${msgKey}.readMoreAboutDecStatus.header")
    }

    "display 'read more' section with submitted declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-submitted")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.submitted.header")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.submitted.paragraph")
    }

    "display 'read more' section with declaration has error declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-declaration-has-error")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.decHasError.header")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.decHasError.paragraph")
    }

    "display 'read more' section with accepted declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-accepted")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.accepted.header")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.accepted.paragraph")
    }

    "display 'read more' section with documents required declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-documents-required")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.header")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.paragraph.1")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.paragraph.2")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.paragraph.3", testEmail)

      val bulletPoints = view.getElementsByClass("govuk-list--bullet").text
      bulletPoints must include(messages(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.bulletPoint.1"))
      bulletPoints must include(messages(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.bulletPoint.2"))
      bulletPoints must include(messages(s"${msgKey}.readMoreAboutDecStatus.documentsRequired.bulletPoint.3"))
    }

    "display 'read more' section with query raised declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-query-raised")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.queryRaised.header")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.queryRaised.paragraph.1")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.queryRaised.paragraph.2", testEmail)
    }

    "display 'read more' section with 'Goods being examined' declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-goods-being-examined")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.header")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.paragraph.1")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.paragraph.2")
      element must containMessage(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.paragraph.3", testEmail)

      val bulletPoints = view.getElementsByClass("govuk-list--bullet").text
      bulletPoints must include(messages(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.bulletPoint.1"))
      bulletPoints must include(messages(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.bulletPoint.2"))
      bulletPoints must include(messages(s"${msgKey}.readMoreAboutDecStatus.goodsExamined.bulletPoint.3"))
    }

    "omit the Declaration Timeline from the page when there are no notifications for the declaration" in {
      val view = page(submission, List.empty)(verifiedEmailRequest(), messages)
      val element = view.getElementsByTag("ol")
      assert(element.isEmpty || !element.hasClass("hmrc-timeline"))
    }
  }
}
