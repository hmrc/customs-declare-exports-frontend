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

import base.Injector
import controllers.routes.DeclarationDetailsController
import models.dis.MrnStatusSpec
import models.requests.ExportsSessionKeys
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.ead.DeclarationType
import views.helpers.{CommonMessages, Title, ViewDates}
import views.tags.ViewTest

@ViewTest
class EadViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val page = instanceOf[views.html.ead]

  private val view: Document =
    page(MrnStatusSpec.completeMrnStatus.mrn, MrnStatusSpec.completeMrnStatus, "/img.jpg")(
      messages,
      FakeRequest().withSession((ExportsSessionKeys.submissionId, "submissionId"))
    )

  "CopyDeclaration page on empty page" should {

    "display 'Back' button that links to /submissions/:id/information" in {
      val backButton = view.getElementsByClass("govuk-back-link").first()
      backButton must containMessage(backCaption)
      backButton must haveHref(DeclarationDetailsController.displayPage("submissionId").url)
    }
    "display page title" in {
      view
        .getElementsByTag("title")
        .first()
        .text mustBe Title("ead.template.title").toString
    }
    "display the expected label, body and hint" when {
      "the DUCR field" in {
        view
          .getElementsByClass("ead-heading")
          .get(1)
          .text mustBe s"${messages("ead.template.ucr")}: ${MrnStatusSpec.completeMrnStatus.ucr.get}"
      }
      "the MRN field" in {
        view
          .getElementsByClass("ead-heading")
          .get(2)
          .text mustBe s"${messages("ead.template.mrn")}: ${MrnStatusSpec.completeMrnStatus.mrn}"
      }
      "the date/time fields" in {
        view
          .getElementsByClass("ead-body")
          .get(4)
          .text mustBe s"${messages("ead.template.releasedDateTime")}:"
        view
          .getElementsByClass("ead-body")
          .get(5)
          .text mustBe MrnStatusSpec.completeMrnStatus.releasedDateTime.map(ViewDates.formatDateAtTime).get
        view
          .getElementsByClass("ead-body")
          .get(6)
          .text mustBe s"${messages("ead.template.acceptanceDateTime")}:"
        view
          .getElementsByClass("ead-body")
          .get(7)
          .text mustBe MrnStatusSpec.completeMrnStatus.acceptanceDateTime.map(ViewDates.formatDateAtTime).get
        view
          .getElementsByClass("ead-body")
          .get(8)
          .text mustBe s"${messages("ead.template.receivedDateTime")}:"
        view
          .getElementsByClass("ead-body")
          .get(9)
          .text mustBe ViewDates.formatDateAtTime(MrnStatusSpec.completeMrnStatus.receivedDateTime)
      }
      "previous docs" in {
        view
          .getElementsByClass("ead-body")
          .get(10)
          .text mustBe s"${messages("ead.template.previousDocuments")}:"
        view
          .getElementsByClass("ead-body")
          .get(11)
          .text mustBe s"${MrnStatusSpec.completeMrnStatus.previousDocuments.head.typeCode} - ${MrnStatusSpec.completeMrnStatus.previousDocuments.head.id}"
      }
      "the quantities field" in {
        view
          .getElementsByClass("ead-quantity-font")
          .get(0)
          .text mustBe s"${messages("ead.template.totalPackageQuantity")}: ${MrnStatusSpec.completeMrnStatus.totalPackageQuantity}"
        view
          .getElementsByClass("ead-quantity-font")
          .get(1)
          .text mustBe s"${messages("ead.template.goodsItemQuantity")}: ${MrnStatusSpec.completeMrnStatus.goodsItemQuantity}"
      }
      "the declaration type field" in {
        view
          .getElementsByClass("ead-body")
          .get(2)
          .text mustBe s"${messages("ead.template.declarationType")}:"
        view
          .getElementsByClass("ead-body")
          .get(3)
          .text mustBe DeclarationType.translate(MrnStatusSpec.completeMrnStatus.declarationType)
      }
      "the EORI field" in {
        view
          .getElementsByClass("ead-body")
          .first()
          .text mustBe s"${messages("ead.template.eori")}:"
        view
          .getElementsByClass("ead-body")
          .get(1)
          .text mustBe DeclarationType.translate(MrnStatusSpec.completeMrnStatus.eori)
      }
      "the created field" in {
        view
          .getElementsByClass("ead-body")
          .get(view.getElementsByClass("ead-body").size() - 2)
          .text mustBe s"${messages("ead.template.createdDateTime")}: ${ViewDates.formatDateAtTime(MrnStatusSpec.completeMrnStatus.createdDateTime)}"
      }
      "the version field" in {
        view
          .getElementsByClass("ead-body")
          .last()
          .text mustBe s"${messages("ead.template.versionId")}: ${MrnStatusSpec.completeMrnStatus.versionId}"
      }
    }
    "display a 'Print' button" in {
      view.getElementsByClass("gem-c-print-link__button").size() mustBe 2
    }

  }

}
