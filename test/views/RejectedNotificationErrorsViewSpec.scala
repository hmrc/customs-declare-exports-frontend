/*
 * Copyright 2019 HM Revenue & Customs
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

import base.Injector
import com.typesafe.config.ConfigFactory
import config.AppConfig
import controllers.routes
import models.Pointer
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission}
import org.jsoup.nodes.Document
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import services.model.RejectionReason
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.rejected_notification_errors

class RejectedNotificationErrorsViewSpec extends UnitViewSpec with Stubs with Injector {

  private val page = new rejected_notification_errors(mainTemplate, minimalAppConfig)
  private val ducr = Some("DUCR")
  private val submission =
    Submission("submissionId", "eori", "lrn", ducr = ducr, actions = Seq(Action("convId", SubmissionRequest)))
  private def view(reasons: Seq[RejectionReason] = Seq.empty) = page(submission, reasons)(request, messages)
  val defaultView = view()

  "Rejected notification errors page" should {

    "have proper messages for labels" in {

      val messages = instanceOf[MessagesApi].preferred(request)
      messages must haveTranslationFor("rejected.notification.ucr")
      messages must haveTranslationFor("rejected.notification.title")
      messages must haveTranslationFor("rejected.notification.header.fieldName")
      messages must haveTranslationFor("rejected.notification.header.errorCode")
      messages must haveTranslationFor("rejected.notification.header.errorDescription")
      messages must haveTranslationFor("rejected.notification.information")
      messages must haveTranslationFor("rejected.notification.continue")
    }

    "have correct title" in {

      defaultView.getElementById("title").text() mustBe messages("rejected.notification.title")
    }

    "have correct back link" in {

      val backLink = defaultView.getElementById("link-back")

      backLink.text() mustBe messages("site.back")
      backLink.attr("href") mustBe routes.SubmissionsController.displayListOfSubmissions().url
    }

    "must contain information" in {

      defaultView.getElementById("information").text() mustBe messages("rejected.notification.information")
    }

    "must contain table headers" in {

      contentAsString(defaultView) must include(messages("rejected.notification.header.fieldName"))
      contentAsString(defaultView) must include(messages("rejected.notification.header.errorCode"))
      contentAsString(defaultView) must include(messages("rejected.notification.header.errorDescription"))
    }

    "must contain notifications" when {
      val reason =
        RejectionReason(
          "rejectionCode",
          "cdsRejectionDescription",
          "exportsRejectionDescription",
          Some(Pointer("declaration.consignmentReferences.lrn"))
        )

      "fully populated and we are using the exports error descriptions" in {
        val doc: Document = view(Seq(reason))

        doc must containElementWithID("rejected_notifications-row-0")
        doc.getElementById("rejected_notifications-row-0-name").text() mustBe messages("field.declaration.consignmentReferences.lrn")
        doc.getElementById("rejected_notifications-row-0-code").text() mustBe "rejectionCode"
        doc.getElementById("rejected_notifications-row-0-description").text() mustBe "exportsRejectionDescription"
      }

      "pointer " in {
        val reason = RejectionReason(
          "rejectionCode",
          "cdsRejectionDescription",
          "exportsRejectionDescription",
          Some(Pointer("declaration.goodsShipment.governmentAgencyGoodsItem.#0.additionalDocument.#1.id"))
        )

        val doc: Document = view(Seq(reason))

        doc must containElementWithID("rejected_notifications-row-0")
        doc.getElementById("rejected_notifications-row-0-name").text() mustBe messages(
          "field.declaration.goodsShipment.governmentAgencyGoodsItem.$.additionalDocument.$.id",
          "0",
          "1"
        )
      }
    }

    "must contain continue link" in {

      val continueLink = defaultView.getElementById("continue")

      continueLink.text() mustBe messages("rejected.notification.continue")
      continueLink.attr("href") mustBe routes.SubmissionsController.amend(submission.uuid).url
    }
  }
}
