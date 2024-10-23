/*
 * Copyright 2024 HM Revenue & Customs
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

package views.helpers

import config.AppConfig
import forms.common.YesNoAnswer.Yes
import forms.section2.AuthorisationProcedureCodeChoice.{Choice1007, Choice1040, ChoiceOthers}
import forms.section4.Document
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukHint, GovukInsetText}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import views.html.components.gds.{bulletList, exportsInputText, externalLink, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class PreviousDocumentsHelper @Inject() (
  bulletList: bulletList,
  govukHint: GovukHint,
  govukInsetText: GovukInsetText,
  exportsInputText: exportsInputText,
  externalLink: externalLink,
  paragraphBody: paragraphBody
) {
  private val prefix = "declaration.previousDocuments"

  def titleInHead(documents: Seq[Document], hasErrors: Boolean): Title = {
    val documentsSize = documents.size
    if (documentsSize == 1) Title("declaration.previousDocuments.summary.header.singular", "declaration.section.4", hasErrors = hasErrors)
    else Title("declaration.previousDocuments.summary.header.plural", "declaration.section.4", documentsSize.toString, hasErrors = hasErrors)
  }

  def bodyUnderTitle(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val bodyBulletList = bulletList(List(Html(messages(s"$prefix.body.bullet.1")), Html(messages(s"$prefix.body.bullet.2"))))

    versionSelection match {
      case 1 => new Html(List(paragraph("v1.body"), bodyBulletList))
      case 2 => new Html(List(paragraph("v2.body.1"), bodyBulletList, paragraph("v2.body.2")))
      case 3 => new Html(List(paragraph("v3.body.1"), bodyBulletList, paragraph("v3.body.2")))
      case 4 => new Html(List(paragraph("v4.body"), bodyBulletList))
      case 5 => new Html(List(paragraph("v5.body"), bodyBulletList))
      case 6 => new Html(List(paragraph("v6.body"), bodyBulletList))
    }
  }

  def helpForDocumentCode(implicit messages: Messages, request: JourneyRequest[_]): Html =
    versionSelection match {
      case 1 => new Html(List(paragraph("v1.documentCode.body"), hint("v1.documentCode.hint")))
      case 2 => new Html(List(paragraph("v2.documentCode.body"), hint("v2.documentCode.hint")))
      case 3 => new Html(List(paragraph("v3.documentCode.body"), hint("v3.documentCode.hint")))
      case 4 => paragraph("v4.documentCode.body")
      case 5 => paragraph("v5.documentCode.body")
      case 6 => paragraph("v6.documentCode.body")
    }

  def helpForDocumentReference(implicit messages: Messages, request: JourneyRequest[_]): Html =
    new Html(List(paragraph(s"v$versionSelection.documentReference.body"), hint("documentReference.hint")))

  def insetText(appConfig: AppConfig)(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val commonParagraphs = List(paragraph("inset.text.1"), paragraph("inset.text.2"))

    val paragraphsForInsetText =
      if (versionSelection == 2) {
        val linkForV2 = externalLink(messages(s"$prefix.inset.text.3.link"), appConfig.simplifiedDeclPreviousDoc)
        commonParagraphs :+ paragraph("inset.text.3", linkForV2)
      } else commonParagraphs

    govukInsetText(InsetText(content = HtmlContent(new Html(paragraphsForInsetText))))
  }

  def itemNumber(form: Form[Document])(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val version = versionSelection
    if (version == 1) HtmlFormat.empty
    else {
      exportsInputText(
        field = form("goodsItemIdentifier"),
        labelKey = "declaration.previousDocuments.goodsItemIdentifier",
        labelClasses = "govuk-label govuk-label--m",
        bodyHtml = Some(paragraph(s"v$version.goodsItemIdentifier.body")),
        inputClasses = Some("govuk-input--width-2 govuk-!-margin-bottom-4")
      )
    }
  }

  def versionSelection(implicit request: JourneyRequest[_]): Int = {
    val model = request.cacheModel
    (model.`type`, model.parties.authorisationProcedureCodeChoice, model.parties.isEntryIntoDeclarantsRecords) match {
      case (STANDARD | SIMPLIFIED | SUPPLEMENTARY, Choice1040, _) => 1
      case (CLEARANCE, Choice1040, Yes)                           => 1

      case (STANDARD | SIMPLIFIED | SUPPLEMENTARY, Choice1007, _) => 2
      case (CLEARANCE, Choice1007, Yes)                           => 2

      case (STANDARD | SIMPLIFIED | SUPPLEMENTARY, ChoiceOthers, _) => 3
      case (CLEARANCE, ChoiceOthers, Yes)                           => 3

      case (STANDARD | SIMPLIFIED | SUPPLEMENTARY, _, _) => 4
      case (CLEARANCE, _, Yes)                           => 4

      case (CLEARANCE, _, _) => 5

      case (OCCASIONAL, _, _) => 6

      // Avoid a compiler warning. It should never get here
      case _ => 1
    }
  }

  def title(implicit request: JourneyRequest[_]): String = s"$prefix.v$versionSelection.title"

  private def hint(key: String)(implicit messages: Messages): Html = govukHint(Hint(content = Text(messages(s"$prefix.$key"))))

  private def paragraph(key: String)(implicit messages: Messages): Html = paragraphBody(messages(s"$prefix.$key"))

  private def paragraph(key: String, link: Html)(implicit messages: Messages): Html = paragraphBody(messages(s"$prefix.$key", link))
}
