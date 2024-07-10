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

package views.timeline

import base.{Injector, MrnStatusTestData}
import controllers.timeline.routes.DeclarationDetailsController
import models.requests.SessionHelper
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import tools.Stubs
import views.common.UnitViewSpec
import views.helpers.{CommonMessages, EadHelper, Title, ViewDates}
import views.html.timeline.ead
import views.tags.ViewTest

@ViewTest
class EadViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with MrnStatusTestData {

  private val page = instanceOf[ead]

  private val view: Document =
    page(mrnStatus.mrn, mrnStatus, "/img.jpg")(messages, FakeRequest().withSession((SessionHelper.submissionUuid, "submissionUuid")))

  "CopyDeclaration page on empty page" should {

    "display 'Back' button that links to /submissions/:id/information" in {
      val backButton = view.getElementsByClass("govuk-back-link").first
      backButton must containMessage(backCaption)
      backButton must haveHref(DeclarationDetailsController.displayPage("submissionUuid").url)
    }

    "display page title" in {
      view.getElementsByTag("title").first.text mustBe Title("ead.template.title").toString
    }

    "display the expected label, body and hint" when {

      "the DUCR field" in {
        val paragraph = view.getElementsByClass("ead-heading")
        paragraph.get(1).text mustBe s"${messages("ead.template.ucr")}: ${mrnStatus.ucr.get}"
      }

      "the MRN field" in {
        val paragraph = view.getElementsByClass("ead-heading")
        paragraph.get(2).text mustBe s"${messages("ead.template.mrn")}: ${mrnStatus.mrn}"
      }

      "the date/time fields" in {
        val paragraph = view.getElementsByClass("ead-body")
        paragraph.get(6).text mustBe s"${messages("ead.template.releasedDateTime")}:"
        paragraph.get(7).text mustBe mrnStatus.releasedDateTime.map(ViewDates.formatDateAtTime).get
        paragraph.get(8).text mustBe s"${messages("ead.template.acceptanceDateTime")}:"
        paragraph.get(9).text mustBe mrnStatus.acceptanceDateTime.map(ViewDates.formatDateAtTime).get
        paragraph.get(10).text mustBe s"${messages("ead.template.receivedDateTime")}:"
        paragraph.get(11).text mustBe ViewDates.formatDateAtTime(mrnStatus.receivedDateTime)
      }

      "previous docs" in {
        val paragraph = view.getElementsByClass("ead-body")
        paragraph.get(12).text mustBe s"${messages("ead.template.previousDocuments")}:"
        paragraph.get(13).text mustBe s"${mrnStatus.previousDocuments.head.typeCode} - ${mrnStatus.previousDocuments.head.id}"
      }

      "the quantities field" in {
        val paragraph = view.getElementsByClass("ead-quantity-font")
        paragraph.get(0).text mustBe s"${messages("ead.template.totalPackageQuantity")}: ${mrnStatus.totalPackageQuantity}"
        paragraph.get(1).text mustBe s"${messages("ead.template.goodsItemQuantity")}: ${mrnStatus.goodsItemQuantity}"
      }

      "the declaration type field" in {
        val paragraph = view.getElementsByClass("ead-body")
        paragraph.get(2).text mustBe s"${messages("ead.template.declarationType")}:"
        paragraph.get(3).text mustBe EadHelper.translate(mrnStatus.declarationType)
      }

      "the EORI field" in {
        val paragraph = view.getElementsByClass("ead-body")
        paragraph.first.text mustBe s"${messages("ead.template.eori")}:"
        paragraph.get(1).text mustBe EadHelper.translate(mrnStatus.eori)
      }

      "the created field" in {
        val expectedText = s"${messages("ead.template.createdDateTime")}: ${ViewDates.formatDateAtTime(mrnStatus.createdDateTime)}"
        val paragraph = view.getElementsByClass("ead-body")
        paragraph.get(paragraph.size() - 2).text mustBe expectedText
      }

      "the version field" in {
        view.getElementsByClass("ead-body").last.text mustBe s"${messages("ead.template.versionId")}: ${mrnStatus.versionId}"
      }
    }

    "display a 'Print' button" in {
      view.getElementsByClass("ceds-print-link").size() mustBe 2
    }
  }
}
