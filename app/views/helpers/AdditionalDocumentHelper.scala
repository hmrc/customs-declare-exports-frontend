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

package views.helpers

import config.AppConfig
import forms.declaration.CommodityDetails
import models.DeclarationType.CLEARANCE
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import services.view.{HolderOfAuthorisationCodes, TaggedAuthCodes}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukDetails, GovukInsetText}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.details.Details
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import views.helpers.CommodityCodeHelper.commodityCodeOfItem
import views.html.components.gds.{bulletList, externalLink, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AdditionalDocumentHelper @Inject() (
  taggedAuthCodes: TaggedAuthCodes,
  appConfig: AppConfig,
  authCodeHelper: HolderOfAuthorisationCodes,
  govukDetails: GovukDetails,
  insetText: GovukInsetText,
  bulletList: bulletList,
  externalLink: externalLink,
  paragraph: paragraphBody
) {
  def body(itemId: String)(implicit messages: Messages, request: JourneyRequest[_]): Html =
    versionSelection(itemId) match {
      case 1 => bodyV1
      case 2 => paragraph(messages(s"$prefix.v2.body"))
      case 3 => bodyV3
      case 4 => paragraph(messages(s"$prefix.v4.body"))
      case 5 => bodyV5
      case 6 => paragraph(messages(s"$prefix.v6.body"))
    }

  def topExpander(itemId: String)(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    def expander(content: Html): Html =
      govukDetails(Details(Some("top-expander"), summary = Text(messages(s"$prefix.expander.title")), content = HtmlContent(content)))

    versionSelection(itemId) match {
      case 1 | 2 => expander(new Html(topExpanderCommonParagraphs(itemId)))
      case 4     => HtmlFormat.empty
      case _     => expander(new Html(topExpanderCommonParagraphs(itemId) :+ topExpanderLastParagraph))
    }
  }

  def documentCodeBody(itemId: String)(implicit request: JourneyRequest[_]): String =
    s"$prefix.v${versionSelection(itemId)}.code.body"

  def documentCodeHint(itemId: String)(implicit request: JourneyRequest[_]): String =
    s"$prefix.v${versionSelection(itemId)}.code.hint"

  def documentCodeExpander(implicit messages: Messages): Html = {
    val link1 = externalLink(messages(s"$prefix.code.expander.body.1.link"), appConfig.additionalDocumentsUnionCodes)
    val link2 = externalLink(messages(s"$prefix.code.expander.body.2.link"), appConfig.additionalDocumentsReferenceCodes)

    val content = List(
      paragraph(messages(s"$prefix.code.expander.body.1", link1)),
      paragraph(messages(s"$prefix.code.expander.body.2", link2)),
      paragraph(messages(s"$prefix.code.expander.body.3"))
    )

    govukDetails(
      Details(id = Some("documentCode-expander"), summary = Text(messages(s"$prefix.code.expander.title")), content = HtmlContent(new Html(content)))
    )
  }

  def documentIdentifierBody(itemId: String)(implicit messages: Messages, request: JourneyRequest[_]): Html =
    versionSelection(itemId) match {
      case 3 => new Html(List(paragraph(messages(s"$prefix.v3.identifier.body.1")), paragraph(messages(s"$prefix.v3.identifier.body.2"))))
      case 4 => paragraph(messages(s"$prefix.v4.identifier.body"))
      case _ => paragraph(messages(s"$prefix.identifier.body"))
    }

  def documentIdentifierHint(implicit messages: Messages, request: JourneyRequest[_]): Option[String] = {
    val authorisationTypeCodes =
      request.cacheModel.parties.declarationHoldersData.fold(Seq.empty[String])(_.holders.flatMap(_.authorisationTypeCode))
    constructHintText(authorisationTypeCodes)
  }

  private def constructHintText(authorisationTypeCodes: Seq[String])(implicit messages: Messages): Option[String] = {
    val authCodesNeedingHintText = taggedAuthCodes.filterAuthCodesNeedingHintText(authorisationTypeCodes)

    authCodesNeedingHintText.headOption.map { _ =>
      val firstMatchingCodes = authCodesNeedingHintText
        .take(3)
        .map(code => messages(s"declaration.additionalDocument.${code}.hint"))

      (Seq(messages("declaration.additionalDocument.hint.prefix")) ++ firstMatchingCodes).mkString(", ")
    }
  }

  def documentIdentifierInsets(itemId: String)(implicit messages: Messages, request: JourneyRequest[_]): Option[Html] = {
    def insets(content: List[Html]): Option[Html] = Some(insetText(InsetText(content = HtmlContent(new Html(content)))))

    versionSelection(itemId) match {
      case 2 | 5 => insets(List(paragraph(messages(s"$prefix.identifier.inset.body.1")), paragraph(messages(s"$prefix.identifier.inset.body.2"))))

      case 3 => insets(List(paragraph(messages(s"$prefix.v3.identifier.inset.body"))))

      case _ => None
    }
  }

  def title(itemId: String)(implicit request: JourneyRequest[_]): String =
    s"$prefix.v${versionSelection(itemId)}.title"

  private def bodyV1(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val authCodes = taggedAuthCodes.authCodesRequiringAdditionalDocs(request.cacheModel)
    new Html(
      List(
        paragraph(messages(s"$prefix.v1.body.1")),
        paragraph(messages(s"$prefix.v1.body.2")),
        bulletList(authCodeHelper.codeDescriptions(messages.lang.toLocale, authCodes).map(Html(_)))
      )
    )
  }

  private def bodyV3(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val authCodes = taggedAuthCodes.authCodesRequiringAdditionalDocs(request.cacheModel)
    new Html(
      List(
        paragraph(messages(s"$prefix.v3.body.1")),
        bulletList(authCodeHelper.codeDescriptions(messages.lang.toLocale, authCodes).map(Html(_))),
        paragraph(messages(s"$prefix.v3.body.2"))
      )
    )
  }

  private def bodyV5(implicit messages: Messages): Html =
    new Html(List(paragraph(messages(s"$prefix.v5.body.1")), paragraph(messages(s"$prefix.v5.body.2"))))

  private def topExpanderCommonParagraphs(itemId: String)(implicit messages: Messages, request: JourneyRequest[_]): List[Html] =
    List(
      commodityCodeOfItem(itemId).fold {
        paragraph(
          messages(
            s"$prefix.expander.body.1.withoutCommodityCode",
            externalLink(text = messages(s"$prefix.expander.body.1.withoutCommodityCode.link"), url = appConfig.tradeTariffSections)
          )
        )
      } { commodityCode =>
        paragraph(
          messages(
            s"$prefix.expander.body.1.withCommodityCode",
            externalLink(
              text = messages(s"$prefix.expander.body.1.withCommodityCode.link", commodityCode.codeAsShown),
              url = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCode.codeAsRef)
            )
          )
        )
      },
      paragraph(messages(s"$prefix.expander.body.2")),
      paragraph(
        messages(
          s"$prefix.expander.body.3",
          externalLink(text = messages(s"$prefix.expander.body.3.link"), url = appConfig.additionalDocumentsLicenceTypes)
        )
      )
    )

  private def topExpanderLastParagraph(implicit messages: Messages): Html =
    paragraph(
      messages(s"$prefix.expander.body.4", externalLink(messages(s"$prefix.expander.body.4.link"), appConfig.guidance.commodityCode0306310010))
    )

  private val prefix = "declaration.additionalDocument"

  private def versionSelection(itemId: String)(implicit request: JourneyRequest[_]): Int = {
    val model = request.cacheModel
    val hasAuthCodeRequiringAdditionalDocs = taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(request.cacheModel)

    (model.`type`, hasAuthCodeRequiringAdditionalDocs, model.isLicenseRequired(itemId)) match {
      case (CLEARANCE, true, _)  => 5
      case (CLEARANCE, false, _) => 6
      case (_, true, true)       => 1
      case (_, false, true)      => 2
      case (_, true, false)      => 3
      case (_, false, false)     => 4
    }
  }
}
