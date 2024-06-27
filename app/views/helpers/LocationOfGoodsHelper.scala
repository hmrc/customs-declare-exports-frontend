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
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType._
import forms.section2.authorisationHolder.AuthorizationTypeCodes.{isAuthCode, CSE, EXRR, MIB}
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukDetails, GovukHint}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.details.Details
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import views.html.components.gds._

import javax.inject.{Inject, Singleton}

@Singleton
class LocationOfGoodsHelper @Inject() (
  govukDetails: GovukDetails,
  govukHint: GovukHint,
  bulletList: bulletList,
  heading: heading,
  externalLink: externalLink,
  link: link,
  body: paragraphBody,
  row: row,
  appConfig: AppConfig
) {
  private val prefix = "declaration.locationOfGoods"
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

  def bodyUnderHeading(version: Int)(implicit messages: Messages): Html =
    HtmlFormat.fill {
      version match {
        case 3 | 5 =>
          List(
            body(messages(s"$prefix.body.v$version.1")),
            bulletList {
              val (front, back) = (1 to 7)
                .map(ix => Html(messages(s"$prefix.body.v3.bullet$ix")))
                .splitAt(2)

              front ++ List(
                Html(messages(s"$prefix.body.v3.bullet8", govukHint(Hint(content = HtmlContent(messages(s"$prefix.body.v3.bullet8.hint"))))))
              ) ++ back
            }
          )

        case 6 =>
          List(s"$prefix.body.v6.1", s"$prefix.body.v6.2", s"$prefix.body.v6.3").map(key => body(messages(key)))

        case 7 =>
          val email = messages(s"$prefix.body.v7.2.email")
          val subject = messages(s"$prefix.body.v7.2.subject")

          List(messages(s"$prefix.body.v7.1"), messages(s"$prefix.body.v7.2", link(email, Call("GET", s"mailto:$email?subject=$subject"))))
            .map(body(_))

        // version 1
        case _ => List(s"$prefix.body.v1.1", s"$prefix.body.v1.1.1", s"$prefix.body.v1.2", s"$prefix.body.v1.3").map(key => body(messages(key)))
      }
    }

  def expander(version: Int)(implicit messages: Messages): Html = {

    val titleKey =
      if (version == 1) s"declaration.locationOfGoods.expander.v1.intro"
      else "declaration.locationOfGoods.expander.intro"

    govukDetails(
      Details(
        id = Some("location-of-goods-expander"),
        summary = Text(messages("declaration.locationOfGoods.expander.title")),
        content = HtmlContent(HtmlFormat.fill(body(messages(titleKey)) :: expanderContent))
      )
    )
  }

  private def expanderContent(implicit messages: Messages): List[Html] = {
    val titleClasses = "govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-1"
    (1 to 10).flatMap { ix =>
      val title = if (ix <= 9) Some(heading(text(s"$ix.title"), titleClasses, "h2")) else None
      val hint =
        if (ix <= 8) row(externalLink(text(s"$ix.link1"), expanderLinks(ix)._1), classes = "")
        else {
          val link1 = externalLink(text(s"$ix.link1"), expanderLinks(ix)._1)
          val link2 = externalLink(text(s"$ix.link2"), expanderLinks(ix)._2)
          govukHint(Hint(content = HtmlContent(text(s"$ix.text", link1, link2))))
        }

      List(title, Some(hint)).flatten
    }.toList
  }

  private def text(suffix: String, args: Any*)(implicit messages: Messages) =
    messages(s"declaration.locationOfGoods.expander.paragraph$suffix", args: _*)

  def versionSelection(implicit request: JourneyRequest[_]): Int =
    request.cacheModel.additionalDeclarationType.fold(1) {
      case decType if ((arrivedTypes ++ Seq(SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED)) contains decType) && isAuthCode(CSE) => 7
      case decType if (preLodgedTypes ++ Seq(SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED)) contains decType                    => 6

      case STANDARD_FRONTIER | SIMPLIFIED_FRONTIER | OCCASIONAL_FRONTIER | CLEARANCE_FRONTIER =>
        if (isAuthCode(EXRR)) 3
        else if (isAuthCode(MIB)) 1
        else 5 // contents of versions 3 and 5 are pretty much equal. They only differ in the body under the page title.

      case _ => 1
    }
}
