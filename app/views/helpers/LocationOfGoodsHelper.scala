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
import forms.section1.AdditionalDeclarationType._
import forms.section2.authorisationHolder.AuthorizationTypeCodes.{isAuthCode, CSE, EXRR, MIB}
import forms.section3.LocationOfGoods
import forms.section3.LocationOfGoods.{gvmsGoodsLocationsForArrivedDecls, radioGroupId, userChoice}
import models.requests.JourneyRequest
import play.api.data.{Field, Form, FormError}
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.twirl.api.HtmlFormat.Appendable
import play.twirl.api.{Html, HtmlFormat}
import services.view.GoodsLocationCodes
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukDetails, GovukHint, GovukInput, GovukRadios}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.details.Details
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.{RadioItem, Radios}
import views.helpers.ErrorMapper.radioGroupErrors
import views.html.components.fields.field_accessible_autocomplete
import views.html.components.gds._

import javax.inject.{Inject, Singleton}

@Singleton
class LocationOfGoodsHelper @Inject() (
  govukDetails: GovukDetails,
  govukHint: GovukHint,
  govukInput: GovukInput,
  govukRadios: GovukRadios,
  yesNoRadios: yesNoRadios,
  heading: heading,
  insetText: exportsInsetText,
  exportsInputText: exportsInputText,
  externalLink: externalLink,
  link: link,
  paragraph: paragraphBody,
  row: row,
  goodsLocationCodes: GoodsLocationCodes,
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

  private lazy val keysV1 = List(s"$prefix.body.v1.1", s"$prefix.body.v1.1.1", s"$prefix.body.v1.2", s"$prefix.body.v1.3")
  private lazy val keysV6 = List(s"$prefix.body.v6.1", s"$prefix.body.v6.2", s"$prefix.body.v6.3")

  def contentUnderHeading(version: Int)(implicit messages: Messages): Html =
    HtmlFormat.fill {
      version match {
        case 3 | 5 => List(paragraph(messages(s"$prefix.body.v$version.1")))

        case 6 => keysV6.map(key => paragraph(messages(key)))

        case 7 =>
          val linkAsParam = {
            val email = messages(s"$prefix.body.v7.2.email")
            val subject = messages(s"$prefix.body.v7.2.subject")
            link(email, Call("GET", s"mailto:$email?subject=$subject"))
          }

          List(messages(s"$prefix.body.v7.1"), messages(s"$prefix.body.v7.2", linkAsParam)).map(paragraph(_))

        // version 1
        case _ => keysV1.map(key => paragraph(messages(key)))
      }
    }

  def errors(form: Form[LocationOfGoods], version: Int)(implicit messages: Messages): Seq[FormError] =
    version match {
      case 3 | 5 => radioGroupErrors(radioGroupId, gvmsGoodsLocationsForArrivedDecls.head, form.errors)
      case _     => form.errors
    }

  def expander(version: Int)(implicit messages: Messages): Html = {
    val titleKey =
      if (version == 1) s"declaration.locationOfGoods.expander.v1.intro"
      else "declaration.locationOfGoods.expander.intro"

    govukDetails(
      Details(
        id = Some("location-of-goods-expander"),
        summary = Text(messages("declaration.locationOfGoods.expander.title")),
        content = HtmlContent(HtmlFormat.fill(paragraph(messages(titleKey)) :: expanderContent))
      )
    )
  }

  private def expanderContent(implicit messages: Messages): List[Html] = {
    val titleClasses = "govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-1"

    def text(suffix: String, args: Any*) = messages(s"declaration.locationOfGoods.expander.paragraph$suffix", args: _*)

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

  def mainContent(form: Form[LocationOfGoods], version: Int)(implicit request: Request[_], messages: Messages): Html =
    version match {
      case 3 | 5 => mainContentForV3AndV5(form)
      case _     => mainContentForOtherVersions(form, version)
    }

  private def mainContentForV3AndV5(form: Form[LocationOfGoods])(implicit messages: Messages): Html = {
    val message = messages(
      "declaration.locationOfGoods.inset.v3.body1",
      externalLink(text = messages("declaration.locationOfGoods.inset.v3.body1.link"), url = appConfig.getGoodsMovementReference)
    )
    HtmlFormat.fill(List(insetText(content = HtmlContent(paragraph(message))), radioButtons(form)))
  }

  private def radioButtons(form: Form[LocationOfGoods])(implicit messages: Messages): Appendable =
    govukRadios(
      Radios(
        name = radioGroupId,
        items = gvmsGoodsLocationsForArrivedDecls.zipWithIndex.map { case (code, ix) => radioButton(form, code, ix + 1) },
        errorMessage = form(radioGroupId).error.map(err => ErrorMessage(content = Text(messages(err.message, err.args: _*))))
      )
    )

  private def radioButton(form: Form[LocationOfGoods], goodsLocation: String, ix: Int)(implicit messages: Messages): RadioItem =
    RadioItem(
      id = Some(s"radio-$goodsLocation"),
      value = Some(goodsLocation),
      content = Text(messages(s"declaration.locationOfGoods.radio.$ix")),
      checked = form(radioGroupId).value.contains(goodsLocation),
      conditionalHtml = if (goodsLocation == userChoice) inputField(form) else None,
      hint =
        if (!messages.isDefinedAt(s"declaration.locationOfGoods.radio.$ix.hint")) None
        else Some(Hint(content = Text(messages(s"declaration.locationOfGoods.radio.$ix.hint"))))
    )

  private def inputField(form: Form[LocationOfGoods])(implicit messages: Messages): Option[Html] =
    Some(
      exportsInputText(
        field = form(userChoice),
        inputClasses = Some("govuk-input govuk-!-width-two-thirds"),
        labelKey = s"declaration.locationOfGoods.radio.$userChoice.input.label",
        hintKey = Some(s"declaration.locationOfGoods.radio.$userChoice.input.hint")
      )
    )

  private def mainContentForOtherVersions(form: Form[LocationOfGoods], version: Int)(implicit request: Request[_], messages: Messages): Html =
    yesNoRadios(
      form,
      heading = Some(Fieldset(legend = Some(Legend(Text(messages("declaration.locationOfGoods.yesNo.header")), "govuk-fieldset__legend--s")))),
      answerKey = radioGroupId,
      yesKey = Some("declaration.locationOfGoods.yesNo.yesKey"),
      noKey = Some("declaration.locationOfGoods.yesNo.noKey"),
      yesHint = None,
      noHint = None,
      yesConditionalHtml = Some(goodsLocationInputYes(form, version)),
      noConditionalHtml = Some(goodsLocationInputNo(form("code")))
    )

  private def goodsLocationInputYes(form: Form[LocationOfGoods], version: Int)(implicit request: Request[_], messages: Messages): Appendable =
    field_accessible_autocomplete(
      field = form(LocationOfGoods.locationId),
      label = messages("declaration.locationOfGoods.yesNo.yesHint"),
      labelClass = Some("govuk-label--s"),
      hintParagraphs = List(messages("declaration.locationOfGoods.yesNo.yes.hint")),
      emptySelectValue = messages("site.search.for.location"),
      items = goodsLocationCodes.asListOfAutoCompleteItems(version == 7)
    )

  private def goodsLocationInputNo(field: Field)(implicit messages: Messages): Appendable =
    govukInput(
      Input(
        id = field.id,
        name = field.name,
        value = field.value,
        label =
          Label(forAttr = Some(field.id), content = HtmlContent(messages("declaration.locationOfGoods.yesNo.noHint")), classes = "govuk-label--s"),
        hint = Some(Hint(content = Text(messages("declaration.locationOfGoods.yesNo.no.hint")))),
        classes = s"govuk-!-width-two-thirds ${if (field.hasErrors) "govuk-input--error"}",
        errorMessage = InputTextHelper.fieldErrorMessages(field)
      )
    )
}

object LocationOfGoodsHelper {

  def versionSelection(implicit request: JourneyRequest[_]): Int =
    request.cacheModel.additionalDeclarationType.fold(1) {
      case decType if ((arrivedTypes ++ Seq(SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED)) contains decType) && isAuthCode(CSE) => 7
      case decType if (preLodgedTypes ++ Seq(SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED)) contains decType                    => 6

      case STANDARD_FRONTIER | SIMPLIFIED_FRONTIER | OCCASIONAL_FRONTIER | CLEARANCE_FRONTIER =>
        if (isAuthCode(EXRR)) 3
        else if (isAuthCode(MIB)) 1
        else 5 // The 'isAuthCode(EXRR)' test is superfluous as the main content of versions 3 and 5 are pretty much equal.

      case _ => 1
    }
}
