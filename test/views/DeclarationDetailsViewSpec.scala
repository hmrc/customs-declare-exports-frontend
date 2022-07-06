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
import models.declaration.submissions.{Action, EnhancedStatus, NotificationSummary, Submission}
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.SubmissionRequest
import models.requests.VerifiedEmailRequest
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import org.scalatest.{Assertion, GivenWhenThen}
import play.api.inject.bind
import views.declaration.spec.UnitViewSpec
import views.helpers.{EnhancedStatusHelper, ViewDates}
import views.html.declaration_details

import java.time.ZonedDateTime
import java.util.UUID
import scala.collection.JavaConverters.asScalaIteratorConverter

class DeclarationDetailsViewSpec extends UnitViewSpec with GivenWhenThen with Injector {

  private val keyPrefix = "submission"
  private val msgKey = s"${keyPrefix}s.declarationDetails"
  private val statusKey = s"$keyPrefix.enhancedStatus"

  private val mrn = "mrn"
  private val now = ZonedDateTime.now

  private val user = ExportsTestData.newUser(ExportsTestData.eori, "Id")
  private val testEmail = "testEmail@mail.org"

  private val uuid = "uuid"
  private val submission = Submission(uuid, "eori", "lrn", Some(mrn), Some("ducr"), None, None, Seq.empty)

  private val dmsqry1Notification = NotificationSummary(UUID.randomUUID(), now, QUERY_NOTIFICATION_MESSAGE)
  private val dmsqry2Notification = NotificationSummary(UUID.randomUUID(), now.plusMinutes(1), QUERY_NOTIFICATION_MESSAGE)
  private val dmsdocNotification = NotificationSummary(UUID.randomUUID(), now.plusMinutes(2), ADDITIONAL_DOCUMENTS_REQUIRED)
  private val dmsctlNotification = NotificationSummary(UUID.randomUUID(), now.plusMinutes(3), UNDERGOING_PHYSICAL_CHECK)
  private val acceptedNotification = NotificationSummary(UUID.randomUUID(), now.plusMinutes(4), RECEIVED)

  private val dmsrejNotification = NotificationSummary(UUID.randomUUID(), now.plusMinutes(5), ERRORS)

  // Since the notification list is reverse-ordered (most to least recent) in TimelineEvents...

  // 1. order of dmsqry1Notification and dmsqry2Notification is relevant.
  //    Only the TimelineEvent instance generated from dmsqry2Notification will have an Html content,
  //    being placed on the timeline before the instance from dmsqry1Notification.

  // 2. order of dmsdocNotification and dmsctlNotification is relevant.
  //    Only the TimelineEvent instance generated from dmsctlNotification will have an Html content,
  //    being placed on the timeline before the instance from dmsdocNotification.

  // 3. button for dmsctlNotification will be a primary button, while button for dmsqry2Notification
  //    will be a secondary button being placed on the timeline after the former.

  private val notificationSummaries = List(dmsqry1Notification, dmsqry2Notification, dmsdocNotification, dmsctlNotification, acceptedNotification)

  private def verifiedEmailRequest(email: String = testEmail): VerifiedEmailRequest[_] =
    VerifiedEmailRequest(RequestBuilder.buildAuthenticatedRequest(request, user), email)

  private def createSubmissionWith(status: EnhancedStatus) = {
    val action = Action("id", SubmissionRequest, now, Some(Seq(NotificationSummary(UUID.randomUUID(), now, status))))
    Submission(uuid, "eori", "lrn", Some(mrn), Some("ducr"), Some(status), Some(now), Seq(action))
  }

  private def createSubmissionWith(notificationSummaries: Seq[NotificationSummary]) = {
    val action = Action("id", SubmissionRequest, now, Some(notificationSummaries))
    Submission(
      uuid,
      "eori",
      "lrn",
      Some(mrn),
      Some("ducr"),
      notificationSummaries.reverse.headOption.map(_.enhancedStatus),
      notificationSummaries.headOption.map(_ => now),
      Seq(action)
    )
  }

  "Declaration details page" should {

    val injector = new OverridableInjector(bind[SecureMessagingConfig].toInstance(mockSecureMessagingConfig))
    val page = injector.instanceOf[declaration_details]

    "contain the navigation banner" when {
      "the Secure Messaging feature flag is enabled" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
        val view = page(submission)(verifiedEmailRequest(), messages)

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
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)
        val view = page(submission)(verifiedEmailRequest(), messages)
        Option(view.getElementById("navigation-banner")) mustBe None
      }
    }
  }

  "Declaration details page" when {

    val injector = new OverridableInjector(bind[EadConfig].toInstance(mockEadConfig))
    val page = injector.instanceOf[declaration_details]

    "the EAD feature flag is enabled" should {
      when(mockEadConfig.isEadEnabled).thenReturn(true)

      "contain the PDF-for-EAD link for any accepted notification's status" in {
        EnhancedStatus.values
          .filter(eadAcceptableStatuses.contains)
          .foreach(status => verifyPdfForEadLink(status))
      }

      "not contain the PDF-for-EAD link" when {
        "the notification's status is not an accepted status" in {
          EnhancedStatus.values
            .filterNot(eadAcceptableStatuses.contains)
            .foreach { status =>
              val view = page(createSubmissionWith(status))(verifiedEmailRequest(), messages)
              Option(view.getElementById("generate-ead")) mustBe None
            }
        }

        "there is no mrn" in {
          val view = page(submission.copy(mrn = None))(verifiedEmailRequest(), messages)
          Option(view.getElementById("generate-ead")) mustBe None
        }
      }

      def verifyPdfForEadLink(status: EnhancedStatus): Assertion = {
        val view = page(createSubmissionWith(status))(verifiedEmailRequest(), messages)

        val declarationLink = view.getElementById("generate-ead")
        declarationLink must containMessage("submissions.generateEAD")
        declarationLink must haveHref(controllers.pdf.routes.EADController.generatePdf(submission.mrn.get))
      }
    }

    "the EAD feature flag is disabled" should {
      "not contain the PDF-for-EAD link" in {
        when(mockEadConfig.isEadEnabled).thenReturn(false)
        val page = injector.instanceOf[declaration_details]
        val view = page(submission)(verifiedEmailRequest(), messages)
        Option(view.getElementById("generate-ead")) mustBe None
      }
    }
  }

  "Declaration details page" should {

    "have correct message keys" in {
      messages must haveTranslationFor("site.backToDeclarations")
      messages must haveTranslationFor(s"$msgKey.title")
      messages must haveTranslationFor(s"$msgKey.mrn")
      messages must haveTranslationFor(s"$msgKey.references")
      messages must haveTranslationFor(s"$msgKey.ducr")
      messages must haveTranslationFor(s"$msgKey.lrn")
      messages must haveTranslationFor(s"$msgKey.fix.resubmit.button")
      messages must haveTranslationFor(s"$msgKey.upload.files.button")
      messages must haveTranslationFor(s"$msgKey.upload.files.title")
      messages must haveTranslationFor(s"$msgKey.upload.files.hint")
      messages must haveTranslationFor(s"$msgKey.view.queries.button")
    }

    "have correct message keys for 'read more' section" in {
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.header")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.paragraph")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.submitted.header")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.submitted.paragraph")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.decHasError.header")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.decHasError.paragraph")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.accepted.header")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.accepted.paragraph")

      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.documentsRequired.header")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.documentsRequired.paragraph.1")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.documentsRequired.paragraph.2")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.documentsRequired.bullet.1")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.documentsRequired.bullet.2")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.documentsRequired.bullet.3")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.documentsRequired.paragraph.3")

      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.queryRaised.header")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.queryRaised.paragraph.1")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.queryRaised.paragraph.2")

      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.goodsExamined.header")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.goodsExamined.paragraph.1")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.goodsExamined.paragraph.2")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.goodsExamined.bullet.1")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.goodsExamined.bullet.2")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.goodsExamined.bullet.3")
      messages must haveTranslationFor(s"$msgKey.readMoreAboutDecStatus.goodsExamined.paragraph.3")
    }

    "have correct message keys for enhanced statuses" in {
      messages must haveTranslationFor(s"$statusKey.ADDITIONAL_DOCUMENTS_REQUIRED")
      messages must haveTranslationFor(s"$statusKey.AMENDED")
      messages must haveTranslationFor(s"$statusKey.AWAITING_EXIT_RESULTS")
      messages must haveTranslationFor(s"$statusKey.CANCELLED")
      messages must haveTranslationFor(s"$statusKey.CLEARED")
      messages must haveTranslationFor(s"$statusKey.CUSTOMS_POSITION_DENIED")
      messages must haveTranslationFor(s"$statusKey.CUSTOMS_POSITION_GRANTED")
      messages must haveTranslationFor(s"$statusKey.DECLARATION_HANDLED_EXTERNALLY")
      messages must haveTranslationFor(s"$statusKey.EXPIRED_NO_ARRIVAL")
      messages must haveTranslationFor(s"$statusKey.EXPIRED_NO_DEPARTURE")
      messages must haveTranslationFor(s"$statusKey.GOODS_ARRIVED")
      messages must haveTranslationFor(s"$statusKey.GOODS_ARRIVED_MESSAGE")
      messages must haveTranslationFor(s"$statusKey.GOODS_HAVE_EXITED")
      messages must haveTranslationFor(s"$statusKey.QUERY_NOTIFICATION_MESSAGE")
      messages must haveTranslationFor(s"$statusKey.RECEIVED")
      messages must haveTranslationFor(s"$statusKey.RELEASED")
      messages must haveTranslationFor(s"$statusKey.UNDERGOING_PHYSICAL_CHECK")
      messages must haveTranslationFor(s"$statusKey.WITHDRAWN")
      messages must haveTranslationFor(s"$statusKey.REQUESTED_CANCELLATION")
      messages must haveTranslationFor(s"$statusKey.UNKNOWN")

      messages must haveTranslationFor(s"$statusKey.CANCELLED.body")
      messages must haveTranslationFor(s"$statusKey.WITHDRAWN.body")
      messages must haveTranslationFor(s"$statusKey.EXPIRED_NO_ARRIVAL.body")
      messages must haveTranslationFor(s"$statusKey.EXPIRED_NO_DEPARTURE.body")
      messages must haveTranslationFor(s"$statusKey.CLEARED.body")
      messages must haveTranslationFor(s"$statusKey.RECEIVED.body")
      messages must haveTranslationFor(s"$statusKey.GOODS_ARRIVED_MESSAGE.body")
    }

    val dummyInboxLink = "dummyInboxLink"

    when(mockSecureMessagingInboxConfig.sfusInboxLink).thenReturn(dummyInboxLink)

    val dummySfusLink = "dummyInboxLink"

    when(mockSfusConfig.isSfusUploadEnabled).thenReturn(true)
    when(mockSfusConfig.sfusUploadLink).thenReturn(dummySfusLink)

    val injector =
      new OverridableInjector(
        bind[SecureMessagingInboxConfig].toInstance(mockSecureMessagingInboxConfig),
        bind[SfusConfig].toInstance(mockSfusConfig)
      )

    val page = injector.instanceOf[declaration_details]
    val view = page(createSubmissionWith(notificationSummaries))(verifiedEmailRequest(), messages)

    "display 'Back' button to the 'Submission list' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.backToDeclarations")
      backButton must haveHref(routes.SubmissionsController.displayListOfSubmissions())
    }

    "display page title" in {
      view.getElementsByTag("h1").first must containMessage(s"$msgKey.title")
    }

    "display the 'MRN' hint" in {
      val message = messages(s"$msgKey.mrn")
      val expectedMrnHint = s"$message ${submission.mrn.get}"
      view.getElementsByClass("submission-mrn").first.text mustBe expectedMrnHint
    }

    "display the 'Declaration references' table" in {
      view.getElementsByClass("declaration-details-refs").get(0).text mustBe messages(s"$msgKey.references")

      val ducr = view.getElementsByClass("submission-ducr").get(0)
      ducr.getElementsByClass("govuk-summary-list__key").text mustBe messages(s"$msgKey.ducr")
      ducr.getElementsByClass("govuk-summary-list__value").text mustBe submission.ducr.get

      val lrn = view.getElementsByClass("submission-lrn").get(0)
      lrn.getElementsByClass("govuk-summary-list__key").text mustBe messages(s"$msgKey.lrn")
      lrn.getElementsByClass("govuk-summary-list__value").text mustBe submission.lrn
    }

    s"contain the uploading-documents link" when {
      List(GOODS_ARRIVED, RECEIVED).foreach { status =>
        s"enhanced status is $status" in {
          val view = page(createSubmissionWith(status))(verifiedEmailRequest(), messages)

          val uploadingDocumentsLink = view.getElementById("uploading-documents-link")
          uploadingDocumentsLink must containMessage("submissions.uploading.documents")
          uploadingDocumentsLink must haveHref(routes.FileUploadController.startFileUpload(mrn))
        }
      }
    }

    s"not contain the uploading-documents link" when {
      (EnhancedStatus.values &~ Set(GOODS_ARRIVED, GOODS_ARRIVED_MESSAGE, RECEIVED)).foreach { status =>
        s"notification's status is $status" in {
          val view = page(createSubmissionWith(status))(verifiedEmailRequest(), messages)
          Option(view.getElementById("uploading-documents-link")) mustBe None
        }
      }
    }

    EnhancedStatus.values.filter(_ != ERRORS) foreach { status =>
      s"contain the view-declaration link when notification's status is ${status}" in {
        val view = page(createSubmissionWith(status))(verifiedEmailRequest(), messages)

        val declarationLink = view.getElementById("view-declaration")
        declarationLink must containMessage("submissions.viewDeclaration")
        declarationLink must haveHref(routes.SubmissionsController.viewDeclaration(submission.uuid))
      }
    }

    s"not contain the view-declaration link when notification's status is ERRORS" in {
      val view = page(createSubmissionWith(ERRORS))(verifiedEmailRequest(), messages)
      Option(view.getElementById("view-declaration")) mustBe None
    }

    "display the Declaration Timeline when there is at least one notification for the declaration" in {
      assert(view.getElementsByTag("ol").hasClass("hmrc-timeline"))

      And("the Timeline should display an event for each notification")
      val events = view.getElementsByClass("hmrc-timeline__event")
      events.size() mustBe notificationSummaries.size

      def dateTimeAsShown(notification: NotificationSummary): String =
        ViewDates.formatDateAtTime(notification.dateTimeIssued)

      notificationSummaries.reverse.zipWithIndex.foreach { case (notification, ix) =>
        And("each Timeline event should always include a title")
        val title = events.get(ix).getElementsByTag("h2")
        assert(title.hasClass("hmrc-timeline__event-title"))
        title.text mustBe EnhancedStatusHelper.asText(notification)

        And("a date and time")
        val datetime = events.get(ix).getElementsByTag("time")
        assert(datetime.hasClass("hmrc-timeline__event-meta"))
        datetime.text mustBe dateTimeAsShown(notification)
      }
    }

    "display on the Declaration Timeline events with content" which {

      "must include one primary button and one secondary button" when {

        "a UNDERGOING_PHYSICAL_CHECK notification is more recent than a QUERY_NOTIFICATION_MESSAGE notification" in {
          val events = eventsOnTimeline(notificationSummaries)

          verifyUploadFilesContent(content(events.get(1)), false)
          verifyViewQueriesContent(content(events.get(3)), true)
        }

        "a ADDITIONAL_DOCUMENTS_REQUIRED notification is more recent than a QUERY_NOTIFICATION_MESSAGE notification" in {
          val notifications = List(dmsdocNotification, dmsqry2Notification)
          val events = eventsOnTimeline(notifications)

          verifyUploadFilesContent(content(events.get(0)), false)
          verifyViewQueriesContent(content(events.get(1)), true)
        }

        "a QUERY_NOTIFICATION_MESSAGE notification is more recent than a UNDERGOING_PHYSICAL_CHECK notification" in {
          val notifications = List(
            NotificationSummary(UUID.randomUUID(), now, UNDERGOING_PHYSICAL_CHECK),
            NotificationSummary(UUID.randomUUID(), now.plusMinutes(1), QUERY_NOTIFICATION_MESSAGE)
          )
          val events = eventsOnTimeline(notifications)

          verifyViewQueriesContent(content(events.get(0)), false)
          verifyUploadFilesContent(content(events.get(1)), true)
        }

        "a QUERY_NOTIFICATION_MESSAGE notification is more recent than a ADDITIONAL_DOCUMENTS_REQUIRED notification" in {
          val notifications = List(
            NotificationSummary(UUID.randomUUID(), now, ADDITIONAL_DOCUMENTS_REQUIRED),
            NotificationSummary(UUID.randomUUID(), now.plusMinutes(1), QUERY_NOTIFICATION_MESSAGE)
          )
          val events = eventsOnTimeline(notifications)

          verifyViewQueriesContent(content(events.get(0)), false)
          verifyUploadFilesContent(content(events.get(1)), true)
        }

        "one notification at least is a ERRORS notification in addition to a ADDITIONAL_DOCUMENTS_REQUIRED notification" in {
          val notifications = List(dmsqry2Notification, dmsdocNotification, dmsrejNotification)
          val events = eventsOnTimeline(notifications)
          verifyRejectedContent(content(events.get(0)))
          content(events.get(1)).size mustBe 0
          verifyViewQueriesContent(content(events.get(2)), true)
        }
      }

      "must only include one primary button and no secondary buttons" when {

        "one notification at least is a ERRORS notification in addition to only UNDERGOING_PHYSICAL_CHECK and/or ADDITIONAL_DOCUMENTS_REQUIRED notifications" in {
          val notifications = List(dmsdocNotification, dmsctlNotification, dmsrejNotification)
          val events = eventsOnTimeline(notifications)
          verifyRejectedContent(content(events.get(0)))
          content(events.get(1)).size mustBe 0
          content(events.get(2)).size mustBe 0
        }

        "there is one only ERRORS notification" in {
          val events = eventsOnTimeline(List(dmsrejNotification))
          verifyRejectedContent(content(events.get(0)))
        }

        "there is one only ADDITIONAL_DOCUMENTS_REQUIRED notification" in {
          val events = eventsOnTimeline(List(dmsdocNotification))
          verifyUploadFilesContent(content(events.get(0)), false)
        }

        "there is one only UNDERGOING_PHYSICAL_CHECK notification" in {
          val events = eventsOnTimeline(List(dmsctlNotification))
          verifyUploadFilesContent(content(events.get(0)), false)
        }

        "there is one only QUERY_NOTIFICATION_MESSAGE notification" in {
          val events = eventsOnTimeline(List(dmsqry2Notification))
          verifyViewQueriesContent(content(events.get(0)), false)
        }
      }

      "must include additional body text" when {
        val statusesWithBodyText = Seq(CANCELLED, WITHDRAWN, EXPIRED_NO_DEPARTURE, EXPIRED_NO_ARRIVAL, CLEARED, RECEIVED, GOODS_ARRIVED_MESSAGE)

        statusesWithBodyText.foreach { status =>
          s"displaying a ${status} notification" in {
            verifyBodyText(status)
          }
        }
      }

      def verifyBodyText(status: EnhancedStatus) = {
        val notifications = List(NotificationSummary(UUID.randomUUID(), now, status))
        val events = eventsOnTimeline(notifications)
        val elements = content(events.get(0))

        val bodyElements = elements.get(0).children
        bodyElements.size mustBe 1
        bodyElements.get(0).hasClass("govuk-body") mustBe true
        bodyElements.get(0).text mustBe messages(s"$statusKey.$status.body")
      }

      def eventsOnTimeline(notifications: List[NotificationSummary]): Elements = {
        val view = page(createSubmissionWith(notifications))(verifiedEmailRequest(), messages)
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
        details.getElementsByTag("summary").text mustBe messages(s"$msgKey.upload.files.title")
        details.getElementsByClass("govuk-details__text").text mustBe messages(s"$msgKey.upload.files.hint")
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

    "display the expected section headers" in {
      val headers = view.getElementsByClass("declaration-details-refs")
      headers.get(1).text mustBe messages(s"$msgKey.uploadDocuments.header")
      headers.get(2).text mustBe messages(s"$msgKey.readMoreAboutDecStatus.header")
    }

    "display 'Uploading documents' expander" in {
      val element = view.getElementById("uploading-documents-details")
      element must containMessage(s"$msgKey.uploadDocuments.details")
      element must containMessage(s"$msgKey.uploadDocuments.details.paragraph.1")
      element must containMessage(s"$msgKey.uploadDocuments.details.paragraph.2")
      element must containMessage(s"$msgKey.uploadDocuments.details.paragraph.3")

      val bulletPoints = view.getElementsByClass("govuk-list--bullet").text
      bulletPoints must include(messages(s"$msgKey.uploadDocuments.details.paragraph.1.bullet.1"))
      bulletPoints must include(messages(s"$msgKey.uploadDocuments.details.paragraph.1.bullet.2"))
      bulletPoints must include(messages(s"$msgKey.uploadDocuments.details.paragraph.1.bullet.3"))
    }

    "display 'read more' section with submitted declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-submitted")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.submitted.header")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.submitted.paragraph")
    }

    "display 'read more' section with declaration has error declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-declaration-has-error")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.decHasError.header")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.decHasError.paragraph")
    }

    "display 'read more' section with accepted declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-accepted")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.accepted.header")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.accepted.paragraph")
    }

    "display 'read more' section with documents required declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-documents-required")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.documentsRequired.header")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.documentsRequired.paragraph.1")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.documentsRequired.paragraph.2")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.documentsRequired.paragraph.3")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.documentsRequired.paragraph.4")

      val bulletPoints = view.getElementsByClass("govuk-list--bullet").text
      bulletPoints must include(messages(s"$msgKey.readMoreAboutDecStatus.documentsRequired.bullet.1"))
      bulletPoints must include(messages(s"$msgKey.readMoreAboutDecStatus.documentsRequired.bullet.2"))
      bulletPoints must include(messages(s"$msgKey.readMoreAboutDecStatus.documentsRequired.bullet.3"))
    }

    "display 'read more' section with query raised declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-query-raised")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.queryRaised.header")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.queryRaised.paragraph.1")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.queryRaised.paragraph.2", testEmail)
    }

    "display 'read more' section with 'Goods being examined' declaration status expander" in {
      val element = view.getElementById("read-more-about-declaration-status-goods-being-examined")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.goodsExamined.header")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.goodsExamined.paragraph.1")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.goodsExamined.paragraph.2")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.goodsExamined.paragraph.3")
      element must containMessage(s"$msgKey.readMoreAboutDecStatus.goodsExamined.paragraph.4")

      val bulletPoints = view.getElementsByClass("govuk-list--bullet").text
      bulletPoints must include(messages(s"$msgKey.readMoreAboutDecStatus.goodsExamined.bullet.1"))
      bulletPoints must include(messages(s"$msgKey.readMoreAboutDecStatus.goodsExamined.bullet.2"))
      bulletPoints must include(messages(s"$msgKey.readMoreAboutDecStatus.goodsExamined.bullet.3"))
    }

    "omit the Declaration Timeline from the page when there are no notifications for the declaration" in {
      val view = page(submission)(verifiedEmailRequest(), messages)
      val element = view.getElementsByTag("ol")
      assert(element.isEmpty || !element.hasClass("hmrc-timeline"))
    }
  }
}
