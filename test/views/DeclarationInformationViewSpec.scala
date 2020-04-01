/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import base.Injector
import com.typesafe.config.{Config, ConfigFactory}
import config.{EadConfig, FeatureSwitchConfig}
import models.declaration.notifications.Notification
import models.declaration.submissions.{Submission, SubmissionStatus}
import play.api.Configuration
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, GovukTable}
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.gdsMainTemplate
import views.html.declaration_information

class DeclarationInformationViewSpec extends UnitViewSpec with Injector {

  private val gdsMainTemplate = instanceOf[gdsMainTemplate]
  private val govukSummaryList = instanceOf[GovukSummaryList]
  private val govukTable = instanceOf[GovukTable]

  private val configWithEnabledEAD: Config =
    ConfigFactory.parseString("microservice.services.features.ead=enabled")
  private val configWithDisabledEAD: Config =
    ConfigFactory.parseString("microservice.services.features.ead=disabled")

  private val featureSwitchConfigEnabledEAD = new FeatureSwitchConfig(Configuration(configWithEnabledEAD))
  private val featureSwitchConfigDisabledEAD = new FeatureSwitchConfig(Configuration(configWithDisabledEAD))

  private val eadConfigEnabled = new EadConfig(featureSwitchConfigEnabledEAD)
  private val eadConfigDisabled = new EadConfig(featureSwitchConfigDisabledEAD)

  private def submission(mrn: Option[String] = Some("mrn")): Submission =
    Submission(uuid = "id", eori = "eori", lrn = "lrn", mrn = mrn, ducr = Some("ducr"), actions = Seq.empty)

  private val submission: Submission = submission()

  private val notification = Notification(
    actionId = "action-id",
    mrn = "mrn",
    dateTimeIssued = LocalDateTime.of(2020, 1, 1, 0, 0, 0),
    status = SubmissionStatus.ACCEPTED,
    errors = Seq.empty,
    payload = "payload"
  )

  private val rejectedNotification = Notification(
    actionId = "actionId",
    mrn = "mrn",
    dateTimeIssued = LocalDateTime.of(2020, 2, 2, 10, 0, 0),
    status = SubmissionStatus.REJECTED,
    errors = Seq.empty,
    payload = ""
  )

  private val notifications = Seq(notification, rejectedNotification)

  private val declarationInformationPageWithEad = new declaration_information(gdsMainTemplate, govukSummaryList, govukTable, eadConfigEnabled)

  private val declarationInformationPageWithoutEad =
    new declaration_information(gdsMainTemplate, govukSummaryList, govukTable, eadConfigDisabled)

  private val viewWithEad = declarationInformationPageWithEad(submission, notifications)(request, messages)

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
      messages must haveTranslationFor("submissions.history")
    }

    "contains page header" in {

      viewWithEad.getElementsByTag("h1").first().text() mustBe "submissions.declarationInformation"
    }

    "contains references table with correct labels" in {

      viewWithEad.getElementsByTag("h2").first().text() mustBe "submissions.references"
      viewWithEad.select(".submission__ucr .govuk-summary-list__key").first().text() mustBe "submissions.ucr"
      viewWithEad.select(".submission__ucr .govuk-summary-list__value").first().text() mustBe submission.ducr.get
      viewWithEad.select(".submission__lrn .govuk-summary-list__key").first().text() mustBe "submissions.lrn"
      viewWithEad.select(".submission__lrn .govuk-summary-list__value").first().text() mustBe submission.lrn
      viewWithEad.select(".submission__mrn .govuk-summary-list__key").first().text() mustBe "submissions.mrn"
      viewWithEad.select(".submission__mrn .govuk-summary-list__value").first().text() mustBe submission.mrn.get
    }

    "contains create EAD link" when {

      "feature flag is enabled" in {

        val generateEADLink = viewWithEad.getElementById("generate-ead")

        generateEADLink.text() mustBe "submissions.generateEAD"
        generateEADLink must haveHref(controllers.pdf.routes.EADController.generatePdf(submission.mrn.get))
      }
    }

    "doesn't contain EAD link " when {

      "there is no mrn" in {

        val view = declarationInformationPageWithoutEad(submission(None), notifications)(request, messages)

        view.getElementById("generate-ead") mustBe null
      }

      "feature flag is disabled" in {

        val view = declarationInformationPageWithoutEad(submission, notifications)(request, messages)

        view.getElementById("generate-ead") mustBe null
      }
    }

    "contains rejected notification with correct data and view errors link" in {

      viewWithEad.getElementById("notification_status_0").text() mustBe SubmissionStatus.format(SubmissionStatus.REJECTED)
      viewWithEad.getElementById("notification_date_time_0").text() mustBe "2 February 2020 at 10:00"
      viewWithEad.getElementById("notification_errors_0").text() mustBe "submissions.viewErrors"
      viewWithEad.getElementById("notification_errors_0").child(0) must haveHref(
        controllers.routes.RejectedNotificationsController.displayPage(submission.uuid)
      )
    }

    "contains accepted notification with correct data" in {

      viewWithEad.getElementById("notification_status_1").text() mustBe SubmissionStatus.format(SubmissionStatus.ACCEPTED)
      viewWithEad.getElementById("notification_date_time_1").text() mustBe "1 January 2020 at 00:00"
      viewWithEad.getElementById("notification_errors_1").text() mustBe empty
    }

    "contains back link which links to the submission list" in {

      val backButton = viewWithEad.getElementById("back-link")

      backButton.text() mustBe "site.backToDeclarations"
      backButton must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
    }
  }
}
