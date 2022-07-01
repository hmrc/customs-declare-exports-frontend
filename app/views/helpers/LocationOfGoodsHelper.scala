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

package views.helpers

import config.AppConfig
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1007, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.declarationHolder.AuthorizationTypeCodes.isAuthCode
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukDetails, GovukHint}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.details.Details
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import views.html.components.gds.{bulletList, heading, link, paragraphBody, row}

import javax.inject.{Inject, Singleton}

@Singleton
class LocationOfGoodsHelper @Inject() (
  govukDetails: GovukDetails,
  govukHint: GovukHint,
  bulletList: bulletList,
  heading: heading,
  link: link,
  body: paragraphBody,
  row: row,
  appConfig: AppConfig
) {
  private val prefix = "declaration.locationOfGoods"

  def bodyUnderHeading(version: Int)(implicit messages: Messages): Html = {
    val sections = version match {
      case 2 =>
        val linkText2 = messages(s"$prefix.body.v2.2.link")
        val email = messages(s"$prefix.body.v2.3.email")
        val subject = messages(s"$prefix.body.v2.3.subject")

        List(
          messages(s"$prefix.body.v2.1"),
          messages(s"$prefix.body.v2.2", link(linkText2, Call("GET", appConfig.locationCodesForCsePremises), "_blank")),
          messages(s"$prefix.body.v2.3", link(email, Call("GET", s"mailto:$email?subject=$subject")))
        ).map(body(_))

      case 3 =>
        List(body(messages(s"$prefix.body.v3.1")), bulletList((1 to 8).map(ix => Html(messages(s"$prefix.body.v3.bullet$ix")))))

      case 4 =>
        val linkText2 = messages(s"$prefix.body.v4.2.link")
        val linkText3 = messages(s"$prefix.body.v4.3.link")

        List(
          body(messages(s"$prefix.body.v4.1")),
          body(messages(s"$prefix.body.v4.1.1")),
          body(messages(s"$prefix.body.v4.2", link(linkText2, Call("GET", appConfig.previousProcedureCodes), "_blank"))),
          body(messages(s"$prefix.body.v4.3.label"), "govuk-heading-s"),
          body(messages(s"$prefix.body.v4.3", link(linkText3, Call("GET", appConfig.locationCodesForPortsUsingGVMS), "_blank")))
        )

      // version 1
      case _ => List(s"$prefix.body.v1.1", s"$prefix.body.v1.1.1", s"$prefix.body.v1.2", s"$prefix.body.v1.3").map(key => body(messages(key)))
    }

    HtmlFormat.fill(sections)
  }

  def expander(version: Int)(implicit messages: Messages): Html =
    govukDetails(
      Details(
        id = Some("location-of-goods-expander"),
        summary = Text(messages("declaration.locationOfGoods.expander.title")),
        content = HtmlContent(HtmlFormat.fill(body(messages(s"declaration.locationOfGoods.expander.v$version.intro")) :: expanderContent))
      )
    )

  def versionSelection(implicit request: JourneyRequest[_]): Int =
    request.cacheModel.additionalDeclarationType.fold(1) {
      case STANDARD_PRE_LODGED | SIMPLIFIED_PRE_LODGED | OCCASIONAL_PRE_LODGED | CLEARANCE_PRE_LODGED if isAuthProcedureCodeForV4 => 4

      case STANDARD_FRONTIER | SIMPLIFIED_FRONTIER | OCCASIONAL_FRONTIER | CLEARANCE_FRONTIER =>
        if (isAuthCode("CSE")) 2 else if (isAuthCode("EXRR")) 3 else 1

      case _ => 1
    }

  private def isAuthProcedureCodeForV4(implicit request: JourneyRequest[_]): Boolean = {
    val authProcedureCode = request.cacheModel.parties.authorisationProcedureCodeChoice
    authProcedureCode == Choice1007 || authProcedureCode == ChoiceOthers
  }

  private val expanderLinks = Array(
    ("", ""),
    /*  1 */ (appConfig.locationCodesForPortsUsingGVMS, ""),
    /*  2 */ (appConfig.rollOnRollOffPorts, ""),
    /*  3 */ (appConfig.railLocationCodes, ""),
    /*  4 */ (appConfig.locationCodeForAirports, ""),
    /*  5 */ (appConfig.certificateOfAgreementAirports, ""),
    /*  6 */ (appConfig.locationCodeForMaritimePorts, ""),
    /*  7 */ (appConfig.locationCodeForTempStorage, ""),
    /*  8 */ (appConfig.designatedExportPlaceCodes, ""),
    /*  9 */ (appConfig.locationCodesForCsePremises, appConfig.previousProcedureCodes),
    /* 10 */ (appConfig.goodsLocationCodesForDataElement, appConfig.tariffCdsChiefSupplement)
  )

  private def expanderContent(implicit messages: Messages): List[Html] = {
    val titleClasses = "govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-1"
    (1 to 10).flatMap { ix =>
      val title = if (ix <= 9) Some(heading(text(s"$ix.title"), titleClasses, "h2")) else None
      val hint =
        if (ix <= 8) row(link(text(s"$ix.link1"), Call("GET", expanderLinks(ix)._1), "_blank"), classes = "")
        else {
          val link1 = link(text(s"$ix.link1"), Call("GET", expanderLinks(ix)._1), "_blank")
          val link2 = link(text(s"$ix.link2"), Call("GET", expanderLinks(ix)._2), "_blank")
          govukHint(Hint(content = HtmlContent(text(s"$ix.text", link1, link2))))
        }

      List(title, Some(hint)).flatten
    }.toList
  }

  private def text(suffix: String, args: Any*)(implicit messages: Messages) =
    messages(s"declaration.locationOfGoods.expander.paragraph$suffix", args: _*)
}
