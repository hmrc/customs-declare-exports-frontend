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
import models.declaration.notifications.Notification
import models.declaration.submissions.{Submission, SubmissionStatus}
import views.declaration.spec.UnitViewSpec
import views.html.declaration_information

class DeclarationInformationViewSpec extends UnitViewSpec with Injector {

  private val declarationInformationPage = instanceOf[declaration_information]

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

  private val view = declarationInformationPage(submission, notifications)(request, messages)

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

      view.getElementsByTag("h1").first().text() mustBe "submissions.declarationInformation"
    }

    "contains references table with correct labels" in {

      view.getElementsByTag("h2").first().text() mustBe "submissions.references"
      view.select(".submission__ucr .govuk-summary-list__key").first().text() mustBe "submissions.ucr"
      view.select(".submission__ucr .govuk-summary-list__value").first().text() mustBe submission.ducr.get
      view.select(".submission__lrn .govuk-summary-list__key").first().text() mustBe "submissions.lrn"
      view.select(".submission__lrn .govuk-summary-list__value").first().text() mustBe submission.lrn
      view.select(".submission__mrn .govuk-summary-list__key").first().text() mustBe "submissions.mrn"
      view.select(".submission__mrn .govuk-summary-list__value").first().text() mustBe submission.mrn.get
    }

    "contains create EAD link" in {

      val generateEADLink = view.getElementById("generate-ead")

      generateEADLink.text() mustBe "submissions.generateEAD"
      generateEADLink must haveHref(controllers.pdf.routes.EADController.generatePdf(submission.mrn.get))
    }

    "doesn't contain EAD link if there is no MRN" in {

      val view = declarationInformationPage(submission(None), notifications)(request, messages)

      view.getElementById("generate-ead") mustBe null
    }

    "contains rejected notification with correct data and view errors link" in {

      view.getElementById("notification_status_0").text() mustBe SubmissionStatus.format(SubmissionStatus.REJECTED)
      view.getElementById("notification_date_time_0").text() mustBe "2 February 2020 at 10:00"
      view.getElementById("notification_errors_0").text() mustBe "submissions.viewErrors"
      view.getElementById("notification_errors_0").child(0) must haveHref(
        controllers.routes.RejectedNotificationsController.displayPage(submission.uuid)
      )
    }

    "contains accepted notification with correct data" in {

      view.getElementById("notification_status_1").text() mustBe SubmissionStatus.format(SubmissionStatus.ACCEPTED)
      view.getElementById("notification_date_time_1").text() mustBe "1 January 2020 at 00:00"
      view.getElementById("notification_errors_1").text() mustBe empty
    }

    "contains back link which links to the submission list" in {

      val backButton = view.getElementById("back-link")

      backButton.text() mustBe "site.backToDeclarations"
      backButton must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
    }
  }
}
