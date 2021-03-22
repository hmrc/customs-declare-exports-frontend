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

import base.Injector
import com.typesafe.config.{Config, ConfigFactory}
import config.{EadConfig, FeatureSwitchConfig, SecureMessagingFeatureFlagConfig, SfusConfig}
import models.declaration.notifications.Notification
import models.declaration.submissions.{Submission, SubmissionStatus}
import play.api.Configuration
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, GovukTable}
import views.declaration.spec.UnitViewSpec
import views.helpers.{StatusOfSubmission, ViewDates}
import views.html.components.gds.{gdsMainTemplate, link}
import views.html.declaration_information

class DeclarationInformationViewSpec extends UnitViewSpec with Injector {

  private val gdsMainTemplate = instanceOf[gdsMainTemplate]
  private val govukSummaryList = instanceOf[GovukSummaryList]
  private val govukTable = instanceOf[GovukTable]
  private val link = instanceOf[link]

  private val configWithFeaturesEnabled: Config =
    ConfigFactory.parseString("""
        |microservice.services.features.ead=enabled
        |microservice.services.features.sfus=enabled
        |microservice.services.features.secureMessaging=sfus
        |urls.sfusUpload="http://localhost:6793/cds-file-upload-service/mrn-entry"
        |urls.sfusInbox="http://localhost:6793/cds-file-upload-service/exports-message-choice"
      """.stripMargin)
  private val configWithFeaturesDisabled: Config =
    ConfigFactory.parseString("""
        |microservice.services.features.ead=disabled
        |microservice.services.features.sfus=disabled
        |microservice.services.features.secureMessaging=disabled
        |urls.sfusUpload="http://localhost:6793/cds-file-upload-service/mrn-entry"
        |urls.sfusInbox="http://localhost:6793/cds-file-upload-service/exports-message-choice"
      """.stripMargin)

  private val featureSwitchConfigEnabled = new FeatureSwitchConfig(Configuration(configWithFeaturesEnabled))
  private val featureSwitchConfigDisabled = new FeatureSwitchConfig(Configuration(configWithFeaturesDisabled))

  private val eadConfigEnabled = new EadConfig(featureSwitchConfigEnabled)
  private val eadConfigDisabled = new EadConfig(featureSwitchConfigDisabled)

  private val sfusConfigEnabled = new SfusConfig(featureSwitchConfigEnabled, Configuration(configWithFeaturesEnabled))
  private val sfusConfigDisabled = new SfusConfig(featureSwitchConfigDisabled, Configuration(configWithFeaturesDisabled))

  private val secureMessagingFeatureFlagConfigSfus = new SecureMessagingFeatureFlagConfig(Configuration(configWithFeaturesEnabled))
  private val secureMessagingFeatureFlagConfigDisabled = new SecureMessagingFeatureFlagConfig(Configuration(configWithFeaturesDisabled))

  private def submission(mrn: Option[String] = Some("mrn")): Submission =
    Submission(uuid = "id", eori = "eori", lrn = "lrn", mrn = mrn, ducr = Some("ducr"), actions = Seq.empty)

  private val submission: Submission = submission()

  private val dateTimeIssued = ZonedDateTime.now
  private val dateTimeAsText = ViewDates.formatDateAtTime(dateTimeIssued)

  private val acceptedNotification = Notification(
    actionId = "action-id",
    mrn = "mrn",
    dateTimeIssued = dateTimeIssued,
    status = SubmissionStatus.ACCEPTED,
    errors = Seq.empty,
    payload = "payload"
  )

  private val rejectedNotification = Notification(
    actionId = "actionId",
    mrn = "mrn",
    dateTimeIssued = dateTimeIssued,
    status = SubmissionStatus.REJECTED,
    errors = Seq.empty,
    payload = ""
  )

  private val clearedNotification = Notification(
    actionId = "actionId",
    mrn = "mrn",
    dateTimeIssued = dateTimeIssued,
    status = SubmissionStatus.CLEARED,
    errors = Seq.empty,
    payload = ""
  )

  private val additionalDocumentsNotification = Notification(
    actionId = "actionId",
    mrn = "mrn",
    dateTimeIssued = dateTimeIssued,
    status = SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED,
    errors = Seq.empty,
    payload = ""
  )

  private val notifications = Seq(acceptedNotification, rejectedNotification, additionalDocumentsNotification)

  private val declarationInformationPageWithFeatures =
    new declaration_information(
      gdsMainTemplate,
      govukSummaryList,
      govukTable,
      link,
      eadConfigEnabled,
      sfusConfigEnabled,
      secureMessagingFeatureFlagConfigSfus
    )

  private val declarationInformationPageWithoutFeatures =
    new declaration_information(
      gdsMainTemplate,
      govukSummaryList,
      govukTable,
      link,
      eadConfigDisabled,
      sfusConfigDisabled,
      secureMessagingFeatureFlagConfigDisabled
    )

  private val viewWithFeatures = declarationInformationPageWithFeatures(submission, notifications)(request, messages)

  private val viewWithFeaturesNotAccepted =
    declarationInformationPageWithFeatures(submission, Seq(rejectedNotification))(request, messages)

  "Declaration information" should {

    "have proper messages for labels" in {

      val messages = realMessagesApi.preferred(request)

      messages must haveTranslationFor("submissions.viewErrors")
      messages must haveTranslationFor("submissions.declarationInformation")
      messages must haveTranslationFor("site.backToDeclarations")
      messages must haveTranslationFor("submissions.references")
      messages must haveTranslationFor("submissions.ucr")
      messages must haveTranslationFor("submissions.lrn")
      messages must haveTranslationFor("submissions.mrn")
      messages must haveTranslationFor("submissions.timeline")
    }

    "contains page header" in {

      viewWithFeatures.getElementsByTag("h1") must containMessageForElements("submissions.declarationInformation")
    }

    "contains references table with correct labels" in {

      viewWithFeatures.getElementsByTag("h2").first() must containMessage("submissions.references")
      viewWithFeatures.select(".submission__ucr .govuk-summary-list__key").first() must containMessage("submissions.ucr")
      viewWithFeatures.select(".submission__ucr .govuk-summary-list__value").first().text() mustBe submission.ducr.get
      viewWithFeatures.select(".submission__lrn .govuk-summary-list__key").first() must containMessage("submissions.lrn")
      viewWithFeatures.select(".submission__lrn .govuk-summary-list__value").first().text() mustBe submission.lrn
      viewWithFeatures.select(".submission__mrn .govuk-summary-list__key").first() must containMessage("submissions.mrn")
      viewWithFeatures.select(".submission__mrn .govuk-summary-list__value").first().text() mustBe submission.mrn.get
    }

    "contains create EAD link" when {

      "feature flag is enabled" in {

        val generateEADLink = viewWithFeatures.getElementById("generate-ead")

        generateEADLink must containMessage("submissions.generateEAD")
        generateEADLink must haveHref(controllers.pdf.routes.EADController.generatePdf(submission.mrn.get))
      }
    }

    "doesn't contain EAD link " when {

      "there is no mrn" in {

        val view = declarationInformationPageWithoutFeatures(submission(None), notifications)(request, messages)

        Option(view.getElementById("generate-ead")) mustBe None
      }

      "feature flag is disabled" in {

        val view = declarationInformationPageWithoutFeatures(submission, notifications)(request, messages)

        Option(view.getElementById("generate-ead")) mustBe None
      }

      "declaration is not accepted" in {

        val view = viewWithFeaturesNotAccepted

        Option(view.getElementById("generate-ead")) mustBe None
      }
    }

    "contains view declaration link" when {

      "declaration is accepted" in {

        val viewDeclarationLink =
          declarationInformationPageWithFeatures(submission, Seq(acceptedNotification))(request, messages).getElementById("view-declaration")

        viewDeclarationLink must containMessage("submissions.viewDeclaration")
        viewDeclarationLink must haveHref(controllers.routes.SubmissionsController.viewDeclaration(submission.uuid))
      }

      "declaration is cleared" in {

        val viewDeclarationLink =
          declarationInformationPageWithFeatures(submission, Seq(clearedNotification))(request, messages).getElementById("view-declaration")

        viewDeclarationLink must containMessage("submissions.viewDeclaration")
        viewDeclarationLink must haveHref(controllers.routes.SubmissionsController.viewDeclaration(submission.uuid))
      }

      "declaration is not accepted" in {

        val viewDeclarationLink = viewWithFeaturesNotAccepted.getElementById("view-declaration")

        viewDeclarationLink must containMessage("submissions.viewDeclaration")
        viewDeclarationLink must haveHref(controllers.routes.SubmissionsController.viewDeclaration(submission.uuid))
      }
    }

    "contains SFUS link" when {

      "feature flag is enabled, status is ADDITIONAL_DOCUMENTS_REQUIRED and mrn is present" in {

        val sfusLink = viewWithFeatures.getElementById("notification_action_0")

        sfusLink must containMessage("submissions.sfus.upload.files")
        sfusLink.child(0) must haveHref("http://localhost:6793/cds-file-upload-service/mrn-entry/mrn")
      }

      "feature flag is enabled, status is ADDITIONAL_DOCUMENTS_REQUIRED and mrn is not present" in {
        val view = declarationInformationPageWithFeatures(submission(None), notifications)(request, messages)
        val sfusLink = view.getElementById("notification_action_0")

        sfusLink must containMessage("submissions.sfus.upload.files")
        sfusLink.child(0) must haveHref("http://localhost:6793/cds-file-upload-service/mrn-entry/")
      }
    }

    "not contain SFUS link" when {

      "feature flag is disabled" in {

        val view = declarationInformationPageWithoutFeatures(submission, notifications)(request, messages)

        val documentsRequired = StatusOfSubmission.asText(SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED)
        view.getElementById("notification_status_0").text() mustBe documentsRequired

        view.getElementById("notification_date_time_0").text() mustBe dateTimeAsText
        view.getElementById("notification_action_0").text() mustBe ""
      }

      "status is not ADDITIONAL_DOCUMENTS_REQUIRED" in {
        val view = viewWithFeaturesNotAccepted

        val documentsRequired = StatusOfSubmission.asText(SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED)
        view.getElementById("notification_status_0").text() mustNot equal(documentsRequired)

        view.getElementById("notification_date_time_0").text() mustBe dateTimeAsText
        view.getElementById("notification_action_0").text() mustBe "View errors"

        Option(view.getElementById("notification_status_1")) mustBe None
      }
    }

    "not display the paragraph below Timeline" when {
      "additional documents are not required" in {
        Option(viewWithFeaturesNotAccepted.getElementById("content-on-dmsdoc")) mustBe None
      }
    }

    "display the paragraph below Timeline, including the link to the SFUS Messaging Inbox" when {
      "additional documents are required and the 'SFUS Secure Messaging' flag is enabled" in {
        val link = messages("submissions.sfus.inbox.link")
        val paragraph = s"${messages("submissions.content.on.dmsdoc")} ${messages("submissions.content.on.dmsdoc.sfus", link)}."
        viewWithFeatures.getElementById("content-on-dmsdoc").text mustBe paragraph

        val element = viewWithFeatures.getElementById("has-dmsdoc-notification")
        element.tagName.toLowerCase mustBe "a"
        element.attr("href") mustBe secureMessagingFeatureFlagConfigSfus.sfusInboxLink
      }
    }

    "display the paragraph below Timeline, excluding the link to the SFUS Messaging Inbox" when {
      "additional documents are required but the 'SFUS Secure Messaging' flag is disabled" in {
        val view = declarationInformationPageWithoutFeatures(submission, notifications)(request, messages)
        view.getElementById("content-on-dmsdoc").text mustBe messages("submissions.content.on.dmsdoc")
      }
    }

    "contains additional documents acceptedNotification with redirect to SFUS link" in {

      val documentsRequired = StatusOfSubmission.asText(SubmissionStatus.ADDITIONAL_DOCUMENTS_REQUIRED)
      viewWithFeatures.getElementById("notification_status_0").text() mustBe documentsRequired
      viewWithFeatures.getElementById("notification_date_time_0").text() mustBe dateTimeAsText
      viewWithFeatures.getElementById("notification_action_0") must containMessage("submissions.sfus.upload.files")
      viewWithFeatures
        .getElementById("notification_action_0")
        .child(0) must haveHref("http://localhost:6793/cds-file-upload-service/mrn-entry/mrn")
    }

    "contains rejected acceptedNotification with correct data and view errors link" in {

      viewWithFeatures.getElementById("notification_status_1").text() mustBe StatusOfSubmission.asText(SubmissionStatus.REJECTED)
      viewWithFeatures.getElementById("notification_date_time_1").text() mustBe dateTimeAsText
      viewWithFeatures.getElementById("notification_action_1") must containMessage("submissions.viewErrors")
      viewWithFeatures.getElementById("notification_action_1").child(0) must haveHref(
        controllers.routes.RejectedNotificationsController.displayPage(submission.uuid)
      )
    }

    "contains accepted acceptedNotification with correct data" in {

      viewWithFeatures.getElementById("notification_status_2").text() mustBe StatusOfSubmission.asText(SubmissionStatus.ACCEPTED)
      viewWithFeatures.getElementById("notification_date_time_2").text() mustBe dateTimeAsText
      viewWithFeatures.getElementById("notification_action_2").text() mustBe empty
    }

    "contains back link which links to the submission list" in {

      val backButton = viewWithFeatures.getElementById("back-link")

      backButton must containMessage("site.backToDeclarations")
      backButton must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
    }
  }
}
