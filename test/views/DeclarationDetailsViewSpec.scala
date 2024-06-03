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

package views

import base.ExportsTestData.{eori, newUser}
import base.{Injector, OverridableInjector, RequestBuilder}
import config.ExternalServicesConfig
import config.featureFlags._
import controllers.declaration.amendments.routes.AmendDeclarationController
import controllers.declaration.routes.SubmissionController
import controllers.routes
import controllers.routes._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.{AmendmentRequest, ExternalAmendmentRequest, SubmissionRequest}
import models.declaration.submissions.{EnhancedStatus, _}
import models.requests.VerifiedEmailRequest
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import org.scalatest.{Assertion, GivenWhenThen}
import play.api.inject.bind
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable
import views.dashboard.DashboardHelper.toDashboard
import views.declaration.spec.UnitViewSpec
import views.helpers.{EnhancedStatusHelper, NotificationEvent, ViewDates}
import views.html.declaration_details

import java.time.ZonedDateTime
import java.util.UUID
import scala.jdk.CollectionConverters.IteratorHasAsScala

class DeclarationDetailsViewSpec extends UnitViewSpec with GivenWhenThen with Injector {

  private val externalServicesConfig = instanceOf[ExternalServicesConfig]

  private val msgKey = "declaration.details"
  private val statusKey = "submission.enhancedStatus"

  private val mrn = "mrn"
  private val now = ZonedDateTime.now

  private val user = newUser(eori, "Id")
  private val testEmail = "testEmail@mail.org"

  private val uuid = "uuid1"
  private val latestDecId = Some("uuid2")

  private val submission = Submission(uuid, "eori", "lrn", Some(mrn), Some("ducr"), None, None, Seq.empty, latestDecId = latestDecId)
  private val subWithoutLatestDecId = Submission(uuid, "eori", "lrn", Some(mrn), Some("ducr"), None, None, Seq.empty, latestDecId = None)

  private val dmsqry1Notification = NotificationSummary(UUID.randomUUID, now, QUERY_NOTIFICATION_MESSAGE)
  private val dmsqry2Notification = NotificationSummary(UUID.randomUUID, now.plusMinutes(1), QUERY_NOTIFICATION_MESSAGE)
  private val dmsdocNotification = NotificationSummary(UUID.randomUUID, now.plusMinutes(2), ADDITIONAL_DOCUMENTS_REQUIRED)
  private val dmsctlNotification = NotificationSummary(UUID.randomUUID, now.plusMinutes(3), UNDERGOING_PHYSICAL_CHECK)
  private val acceptedNotification = NotificationSummary(UUID.randomUUID, now.plusMinutes(4), RECEIVED)
  private val dmsrecNotification = NotificationSummary(UUID.randomUUID, now.plusMinutes(5), CUSTOMS_POSITION_DENIED)
  private val dmsrejNotification = NotificationSummary(UUID.randomUUID, now.plusMinutes(5), ERRORS)
  private val awaitExitResultsNotification = NotificationSummary(UUID.randomUUID, now.plusMinutes(6), AWAITING_EXIT_RESULTS)
  private val arrivedNotification = NotificationSummary(UUID.randomUUID, now.plusMinutes(6), GOODS_ARRIVED)

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

  private def submissionWithStatus(status: EnhancedStatus = ERRORS): Submission =
    Submission(uuid, "eori", "lrn", Some(mrn), Some("ducr"), latestEnhancedStatus = Some(status), None, Seq.empty, latestDecId = latestDecId)

  private def submissionWithAction(status: EnhancedStatus): Submission = {
    val action = Action("id", SubmissionRequest, now, Some(Seq(NotificationSummary(UUID.randomUUID, now, status))), Some(uuid), 1)
    Submission(uuid, "eori", "lrn", Some(mrn), Some("ducr"), Some(status), Some(now), Seq(action), latestDecId = latestDecId)
  }

  private def submissionWithNotifications(
    notificationSummaries: Seq[NotificationSummary],
    requestType: RequestType = SubmissionRequest
  ): Submission = {
    val action = Action("id", requestType, now, Some(notificationSummaries), Some(UUID.randomUUID().toString), 1)
    Submission(
      uuid,
      "eori",
      "lrn",
      Some(mrn),
      Some("ducr"),
      notificationSummaries.reverse.headOption.map(_.enhancedStatus),
      notificationSummaries.headOption.map(_ => now),
      Seq(action),
      latestDecId = latestDecId
    )
  }

  private def createView(submission: Submission, additionalDecType: AdditionalDeclarationType = STANDARD_PRE_LODGED)(
    implicit injector: OverridableInjector
  ): Appendable = {
    val page = injector.instanceOf[declaration_details]
    page(submission, additionalDecType)(verifiedEmailRequest(), messages)
  }

  "Declaration details page" should {

    implicit val injector = new OverridableInjector(
      bind[DeclarationAmendmentsConfig].toInstance(mockDeclarationAmendmentsConfig),
      bind[SecureMessagingConfig].toInstance(mockSecureMessagingConfig)
    )

    "contain the navigation banner" when {
      "the Secure Messaging feature flag is enabled" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
        val view = createView(submission)

        val banner = view.getElementById("navigation-banner")
        assert(Option(banner).isDefined && banner.childrenSize == 2)

        val elements = banner.children.iterator.asScala.toList
        assert(elements.forall(_.tagName.toLowerCase == "a"))
        elements.head must haveHref(toDashboard)
        elements.last must haveHref(routes.SecureMessagingController.displayInbox)
      }
    }

    "not contain the navigation banner" when {
      "the Secure Messaging feature flag is disabled" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)
        val view = createView(submissionWithStatus())
        Option(view.getElementById("amend-declaration")) mustBe None
      }
    }

    "contain the 'Amend declaration' link" when {
      "the 'declarationAmendments' feature flag is enabled and" when {
        "the declaration has NOT been externally amended" in {
          when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)
          val submission = submissionWithStatus(RECEIVED)
          val view = createView(submission)

          val amendDeclarationLink = view.getElementById("amend-declaration")
          amendDeclarationLink must containMessage("declaration.details.amend.declaration")

          val parentId = submission.latestDecId.value
          amendDeclarationLink must haveHref(AmendDeclarationController.initAmendment(parentId, RECEIVED.toString))
        }
      }
    }

    "contain the 'Amend declaration' link pointing to '/unavailable-actions'" when {
      "the 'declarationAmendments' feature flag is enabled and" when {
        "the declaration has been externally amended" in {
          when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)

          val action = Action("id", ExternalAmendmentRequest, now, None, None, 2)
          val submission = submissionWithStatus(RECEIVED).copy(actions = Seq(action))
          val view = createView(submission)

          val amendDeclarationLink = view.getElementById("amend-declaration")
          amendDeclarationLink must containMessage("declaration.details.amend.declaration")
          amendDeclarationLink must haveHref(DeclarationDetailsController.unavailableActions(submission.uuid))
        }
      }
    }

    "NOT contain the 'Amend declaration' link" when {
      "the 'declarationAmendments' feature flag is disabled" in {
        when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(false)
        val view = createView(submission)
        Option(view.getElementById("amend-declaration")) mustBe None
      }

      "submission has no latestDecId" in {
        when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)
        val view = createView(subWithoutLatestDecId)
        Option(view.getElementById("amend-declaration")) mustBe None
      }

      amendmentBlockingStatuses.foreach { status =>
        s"submission has one of the enhanced status of $status" in {
          when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)
          val view = createView(submissionWithStatus(status))
          Option(view.getElementById("amend-declaration")) mustBe None
        }
      }
    }
  }

  "Declaration details page" when {
    implicit val injector = new OverridableInjector()

    "contain the EAD link for any accepted notification's status" in {
      EnhancedStatus.values
        .filter(_ in eadAcceptableStatuses)
        .foreach(status => verifyEadLink(status))
    }

    "not contain the EAD link" when {
      "the notification's status is not an accepted status" in {
        EnhancedStatus.values
          .filterNot(_ in eadAcceptableStatuses)
          .foreach { status =>
            val view = createView(submissionWithAction(status))
            Option(view.getElementById("generate-ead")) mustBe None
          }
      }

      "there is no mrn" in {
        val view = createView(submissionWithStatus().copy(mrn = None))
        Option(view.getElementById("generate-ead")) mustBe None
      }
    }

    def verifyEadLink(status: EnhancedStatus): Assertion = {
      val view = createView(submissionWithAction(status))

      val declarationLink = view.getElementById("generate-ead")
      declarationLink must containMessage("declaration.details.generateEAD")
      declarationLink must haveHref(EADController.generateDocument(submission.mrn.get))
    }
  }

  "Declaration details page" should {

    "have correct message keys" in {
      messages must haveTranslationFor("site.backToDeclarations")
      messages must haveTranslationFor(s"$msgKey.title")
      messages must haveTranslationFor("mrn.heading")
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

      messages must haveTranslationFor(s"$statusKey.timeline.content.CANCELLED")
      messages must haveTranslationFor(s"$statusKey.timeline.content.WITHDRAWN")
      messages must haveTranslationFor(s"$statusKey.timeline.content.EXPIRED_NO_ARRIVAL")
      messages must haveTranslationFor(s"$statusKey.timeline.content.EXPIRED_NO_DEPARTURE")
      messages must haveTranslationFor(s"$statusKey.timeline.content.CLEARED")
      messages must haveTranslationFor(s"$statusKey.timeline.content.RECEIVED")
      messages must haveTranslationFor(s"$statusKey.timeline.content.GOODS_ARRIVED_MESSAGE")
    }

    val dummyInboxLink = "dummyInboxLink"

    when(mockSecureMessagingInboxConfig.sfusInboxLink).thenReturn(dummyInboxLink)

    val dummySfusLink = "dummyInboxLink"

    when(mockSfusConfig.isSfusUploadEnabled).thenReturn(true)
    when(mockSfusConfig.sfusUploadLink).thenReturn(dummySfusLink)

    implicit val injector =
      new OverridableInjector(
        bind[SecureMessagingInboxConfig].toInstance(mockSecureMessagingInboxConfig),
        bind[SfusConfig].toInstance(mockSfusConfig)
      )

    val view = createView(submissionWithNotifications(notificationSummaries))

    "display 'Back' button to the 'Submission list' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.backToDeclarations")
      backButton must haveHref(toDashboard)
    }

    "not have View declaration summary link" in {
      val view = createView(submissionWithNotifications(notificationSummaries))
      Option(view.getElementById("view_declaration_summary")) mustBe None
    }

    "display page title" in {
      EnhancedStatus.values.foreach { status =>
        val view = createView(submissionWithAction(status))
        val expected = messages(s"$statusKey.$status")
        view.getElementsByTag("h1").first.text mustBe messages(s"$msgKey.title", expected)
      }
    }

    "display the 'MRN' hint" in {
      view.getElementsByClass("submission-mrn").first.text mustBe messages("mrn.heading", submission.mrn.get)
    }

    "display the 'Declaration references' table" in {
      view.getElementsByClass("declaration-details-refs").get(0).text mustBe messages(s"$msgKey.references")

      val ducr = view.getElementsByClass("submission-ducr").get(0)
      ducr.getElementsByClass(summaryKeyClassName).text mustBe messages(s"$msgKey.ducr")
      ducr.getElementsByClass(summaryValueClassName).text mustBe submission.ducr.get

      val lrn = view.getElementsByClass("submission-lrn").get(0)
      lrn.getElementsByClass(summaryKeyClassName).text mustBe messages(s"$msgKey.lrn")
      lrn.getElementsByClass(summaryValueClassName).text mustBe submission.lrn
    }

    "contain the uploading-documents link" when {
      List(GOODS_ARRIVED, RECEIVED).foreach { status =>
        s"enhanced status is $status" in {
          val view = createView(submissionWithAction(status))

          val uploadingDocumentsLink = view.getElementById("uploading-documents-link")
          uploadingDocumentsLink must containMessage("declaration.details.uploading.documents")
          uploadingDocumentsLink must haveHref(FileUploadController.startFileUpload(mrn))
        }
      }
    }

    "not contain the uploading-documents link" when {
      (EnhancedStatus.values &~ Set(GOODS_ARRIVED, GOODS_ARRIVED_MESSAGE, RECEIVED)).foreach { status =>
        s"notification's status is $status" in {
          val view = createView(submissionWithAction(status))
          Option(view.getElementById("uploading-documents-link")) mustBe None
        }
      }
    }

    EnhancedStatus.values.filter(_ != ERRORS) foreach { status =>
      s"contain the view-declaration link when notification's status is ${status}" in {
        val view = createView(submissionWithAction(status))

        val viewDeclarationLink = view.getElementById("view-declaration")
        viewDeclarationLink must containMessage(s"${msgKey}.view.declaration")
        viewDeclarationLink must haveHref(SubmissionsController.viewDeclaration(latestDecId.value))
      }
    }

    "contain the view-declaration link pointing to the declaration to show via Submission.uuid" when {
      "Submission.latestDecId is None" in {
        val view = createView(subWithoutLatestDecId)

        val viewDeclarationLink = view.getElementById("view-declaration")
        viewDeclarationLink must containMessage(s"${msgKey}.view.declaration")
        viewDeclarationLink must haveHref(SubmissionsController.viewDeclaration(subWithoutLatestDecId.uuid))
      }
    }

    "not contain the view-declaration link when notification's status is ERRORS" in {
      val view = createView(submissionWithAction(ERRORS))
      Option(view.getElementById("view-declaration")) mustBe None
    }

    EnhancedStatus.values.diff(rejectedStatuses) foreach { status =>
      s"contain the copy-declaration link when notification's status is ${status}" in {
        val view = createView(submissionWithAction(status))

        val copyDeclarationLink = view.getElementById("copy-declaration")
        copyDeclarationLink must containMessage("declaration.details.copy.declaration")
        copyDeclarationLink must haveHref(CopyDeclarationController.redirectToReceiveJourneyRequest(submission.uuid))
      }
    }

    EnhancedStatus.values.intersect(rejectedStatuses) foreach { status =>
      s"not contain the copy-declaration link when notification's status is ${status}" in {
        val view = createView(submissionWithAction(status))
        Option(view.getElementById("copy-declaration")) mustBe None
      }
    }

    "contain the cancel-declaration link" in {
      val view = createView(submissionWithStatus())

      val cancelDeclarationLink = view.getElementById("cancel-declaration")
      cancelDeclarationLink must containMessage("declaration.details.cancel.declaration")
      cancelDeclarationLink must haveHref(CancelDeclarationController.displayPage)
    }

    "NOT contain the cancel-declaration link" when {
      cancelledStatuses.foreach { status =>
        s"when the declaration has status $status" in {
          val view = createView(submissionWithStatus(status))

          Option(view.getElementById("cancel-declaration")) mustBe None
        }
      }
    }

    "display the link to redirect the user to the 'Movements' service" in {
      val redirectionLink = view.getElementById("movements-redirection")
      redirectionLink.text mustBe messages("declaration.details.movements.redirection")
      redirectionLink must haveHref(Call("GET", externalServicesConfig.customsMovementsFrontendUrl))
    }

    "display the Declaration Timeline when there is at least one notification for the declaration" in {
      assert(view.getElementsByTag("ol").hasClass("hmrc-timeline"))

      And("the Timeline should display an event for each notification")
      val events = view.getElementsByClass("hmrc-timeline__event")
      events.size mustBe notificationSummaries.size

      def dateTimeAsShown(notification: NotificationSummary): String =
        ViewDates.formatDateAtTime(notification.dateTimeIssued)

      notificationSummaries.reverse.zipWithIndex.foreach { case (notification, ix) =>
        And("each Timeline event should always include a title")
        val title = events.get(ix).getElementsByTag("h2")
        assert(title.hasClass("hmrc-timeline__event-title"))
        title.text mustBe EnhancedStatusHelper.asTimelineEvent(NotificationEvent(uuid, SubmissionRequest, notification))

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
            NotificationSummary(UUID.randomUUID, now, UNDERGOING_PHYSICAL_CHECK),
            NotificationSummary(UUID.randomUUID, now.plusMinutes(1), QUERY_NOTIFICATION_MESSAGE)
          )
          val events = eventsOnTimeline(notifications)

          verifyViewQueriesContent(content(events.get(0)), false)
          verifyUploadFilesContent(content(events.get(1)), true)
        }

        "a QUERY_NOTIFICATION_MESSAGE notification is more recent than a ADDITIONAL_DOCUMENTS_REQUIRED notification" in {
          val notifications = List(
            NotificationSummary(UUID.randomUUID, now, ADDITIONAL_DOCUMENTS_REQUIRED),
            NotificationSummary(UUID.randomUUID, now.plusMinutes(1), QUERY_NOTIFICATION_MESSAGE)
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

        "in addition to only UNDERGOING_PHYSICAL_CHECK and/or ADDITIONAL_DOCUMENTS_REQUIRED notifications" when {
          "one notification at least is a ERRORS notification" in {
            val notifications = List(dmsdocNotification, dmsctlNotification, dmsrejNotification)
            val events = eventsOnTimeline(notifications)
            verifyRejectedContent(content(events.get(0)))
            content(events.get(1)).size mustBe 0
            content(events.get(2)).size mustBe 0
          }
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

      "must NOT include an additional body text for a 'CLEARED' notification" when {
        "the declaration has type 'Y' (SUPPLEMENTARY_SIMPLIFIED)" in {
          val notifications = List(NotificationSummary(UUID.randomUUID, now, CLEARED))
          val view = createView(submissionWithNotifications(notifications), SUPPLEMENTARY_SIMPLIFIED)
          val events = view.getElementsByClass("hmrc-timeline__event")
          events.get(0).getElementsByClass("hmrc-timeline__event-content").size() mustBe 0
        }
      }

      "must display buttons under the appropriate notification" when {
        "'Fix' button and 'Cancel' link when an amendment rejected notification is not latest" in {
          val notifications = List(dmsrejNotification, awaitExitResultsNotification)
          val events = eventsOnTimeline(notifications)
          verifyRejectedContent(content(events.get(1)))
          content(events.get(0)).size mustBe 0
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

      def verifyBodyText(status: EnhancedStatus): Assertion = {
        val notifications = List(NotificationSummary(UUID.randomUUID, now, status))
        val events = eventsOnTimeline(notifications)
        val elements = content(events.get(0))

        val bodyElements = elements.get(0).children
        bodyElements.size mustBe 1
        bodyElements.get(0).hasClass("govuk-body") mustBe true
        bodyElements.get(0).text mustBe messages(s"$statusKey.timeline.content.$status")
      }

      def eventsOnTimeline(notifications: List[NotificationSummary]): Elements = {
        val view = createView(submissionWithNotifications(notifications))
        view.getElementsByClass("hmrc-timeline__event")
      }

      def content(element: Element): Elements = element.getElementsByClass("hmrc-timeline__event-content")

      def verifyRejectedContent(content: Elements): Assertion = {
        val rejectedElements = content.get(0).children
        rejectedElements.size mustBe 1

        And("the 'Fix and resubmit' content, should include a primary link-button to the RejectedNotificationsController")
        val call = RejectedNotificationsController.displayPage(uuid).url
        verifyButton(rejectedElements.get(0), false, "fix.resubmit", call)
      }

      def verifyUploadFilesContent(content: Elements, buttonIsSecondary: Boolean): Assertion = {
        val uploadFilesElements = content.get(0).getElementById("upload-files-section").children
        uploadFilesElements.size mustBe 2

        And("the 'Documents required' content, when defined, should include a link-button to SFUS")
        verifyButton(uploadFilesElements.get(0), buttonIsSecondary, "upload.files", FileUploadController.startFileUpload(mrn).url)

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

    "display 'Fix and resubmit' button and 'Cancel' link when latest notification is a rejected Amendment" in {
      val notificationsWithAmendmentRejected = List(dmsrejNotification, dmsdocNotification, dmsctlNotification, acceptedNotification)
      val submission = submissionWithNotifications(notificationsWithAmendmentRejected, AmendmentRequest)
      val view = createView(submission)
      checkLatestAmendment(Some(submission), view, "declaration.details.fix.resubmit.button")
    }

    "display 'Resubmit' button and 'Cancel' link when latest notification is a failed Amendment" in {
      val notificationsWithAmendmentFailed = List(dmsrecNotification, dmsdocNotification, dmsctlNotification, acceptedNotification)
      val submission = submissionWithNotifications(notificationsWithAmendmentFailed, AmendmentRequest)
      val view = createView(submission)
      checkLatestAmendment(None, view, "declaration.details.resubmit.button")
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

    "display 'read more' section with re-arrivals status expander based on arrival status" when {
      "Arrival has occurred on the declaration" in {
        val arrivedNotificationSummaries =
          List(acceptedNotification, arrivedNotification)
        val element =
          createView(submissionWithNotifications(arrivedNotificationSummaries)).getElementById("read-more-about-declaration-status-rearrivals")
        element must containMessage(s"$msgKey.readMoreAboutDecStatus.rearrivals.header")
        element must containMessage(s"$msgKey.readMoreAboutDecStatus.rearrivals.paragraph")
      }

      "Arrival has not occurred on the declaration" in {
        val element = view.getElementById("read-more-about-declaration-status-rearrivals")
        element mustBe null
      }
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
      val view = createView(submissionWithStatus())
      val element = view.getElementsByTag("ol")
      assert(element.isEmpty || !element.hasClass("hmrc-timeline"))
    }
  }

  private def checkLatestAmendment(maybeSubmission: Option[Submission], view: Appendable, messageKeyOfButton: String): Unit = {
    val buttonGroup = view.getElementsByClass("govuk-button-group")
    val button = buttonGroup.get(0).child(0)
    val link = buttonGroup.get(0).child(1)

    button.text() mustBe messages(messageKeyOfButton)
    button.hasClass("govuk-button")

    val href = maybeSubmission.fold(SubmissionController.displayResubmitAmendmentPage) { submission =>
      RejectedNotificationsController.displayPageOnUnacceptedAmendment(submission.actions.head.id)
    }
    button must haveHref(href)

    link.tagName() mustBe "a"
    link.hasClass("govuk-link")
    link.text() mustBe messages("declaration.details.cancel.amendment")
    link must haveHref(SubmissionController.cancelAmendment)
  }
}
