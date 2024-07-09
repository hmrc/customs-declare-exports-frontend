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

import forms.section6.DepartureTransport.radioButtonGroupId
import forms.section6.InlandOrBorder.Border
import forms.section6.ModeOfTransportCode.Road
import forms.section6.{TransportCode, TransportCodes}
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import services.TransportCodeService
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukInsetText, GovukRadios}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.{RadioItem, Radios}
import views.html.components.gds.{exportsInputText, pageTitle, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class DepartureTransportHelper @Inject() (
  tcs: TransportCodeService,
  govukRadios: GovukRadios,
  govukInsetText: GovukInsetText,
  pageTitle: pageTitle,
  paragraphBody: paragraphBody,
  exportsInputText: exportsInputText
) {
  def dynamicContent(form: Form[_])(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val wrappedHeading: Html = Html(s"""
         |<legend class="govuk-fieldset__legend govuk-!-margin-bottom-0">
         |  $heading
         |</legend>
         |""".stripMargin)
    HtmlFormat.fill(List(wrappedHeading, bodyUnderHeading, insetText, radioButtons(form)))
  }

  def titleInHeadTag(hasErrors: Boolean)(implicit messages: Messages, request: JourneyRequest[_]): Title = {
    val (key, transportMode) = keyAndTransportModeForTitle
    Title(key, "declaration.section.6", transportMode, hasErrors = hasErrors)
  }

  private val prefix = "declaration.transportInformation.meansOfTransport"

  private def bodyUnderHeading(implicit messages: Messages, request: JourneyRequest[_]): Html =
    if (versionSelection == 2 && request.cacheModel.hasInlandModeOfTransportCode(Road))
      new Html(List(paragraph("body.v2"), paragraph("body")))
    else paragraph("body")

  private def hasPCsEqualTo0019(declaration: ExportsDeclaration): Boolean =
    declaration.items.exists(_.procedureCodes.exists(_.procedureCode.contains("0019")))

  private def heading(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val (key, transportMode) = keyAndTransportModeForTitle
    pageTitle(content = Some(Text(messages(key, transportMode))))
  }

  private def inputField(transportCode: TransportCode, form: Form[_])(implicit messages: Messages): Option[Html] =
    Some(
      exportsInputText(
        field = form(transportCode.id),
        inputClasses = Some("govuk-input govuk-!-width-two-thirds"),
        labelKey = s"$prefix.${transportCode.id}.label",
        hintKey = Some(s"$prefix.${transportCode.id}.hint")
      )
    )

  private def insetText(implicit messages: Messages, request: JourneyRequest[_]): Html =
    if (versionSelection == 3) govukInsetText(InsetText(content = Text(messages(s"$prefix.departure.inset.text.v3"))))
    else HtmlFormat.empty

  private def keyAndTransportModeForTitle(implicit messages: Messages, request: JourneyRequest[_]): (String, String) = {
    val version = versionSelection

    val transportCode =
      if (version == 2) request.cacheModel.inlandModeOfTransportCode
      else request.cacheModel.transportLeavingBorderCode

    (s"$prefix.departure.title.v$version", ModeOfTransportCodeHelper.transportMode(transportCode))
  }

  private def paragraph(key: String)(implicit messages: Messages): Html = paragraphBody(messages(s"$prefix.departure.$key"))

  private def radioButton(form: Form[_], transportCode: TransportCode, useAltRadioTextForV2: Boolean = false)(
    implicit messages: Messages
  ): RadioItem =
    RadioItem(
      id = Some(s"radio_${transportCode.id}"),
      value = Some(transportCode.value),
      content = Text(messages(s"$prefix.${transportCode.id}${if (useAltRadioTextForV2) ".v2" else ""}")),
      conditionalHtml = if (transportCode != tcs.NotApplicable) inputField(transportCode, form) else None,
      checked = form(radioButtonGroupId).value.contains(transportCode.value)
    )

  private def radioButtons(form: Form[_])(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val items = versionSelection match {
      case 1 => tcs.transportCodesForV1.asList.map(radioButton(form, _))
      case 2 => tcs.transportCodesForV2.asList.map(transportCode => radioButton(form, transportCode, transportCode.useAltRadioTextForV2))

      case 3 if hasPCsEqualTo0019(request.cacheModel) => tcs.transportCodesForV3WhenPC0019.asList.map(radioButton(form, _))

      case 3 => tcs.transportCodesForV3.asList.map(radioButton(form, _))
    }

    govukRadios(
      Radios(
        name = radioButtonGroupId,
        items = items,
        errorMessage = form(radioButtonGroupId).error.map(err => ErrorMessage(content = Text(messages(err.message, err.args: _*))))
      )
    )
  }

  def transportCodes(implicit request: JourneyRequest[_]): TransportCodes =
    versionSelection match {
      case 1                                          => tcs.transportCodesForV1
      case 2                                          => tcs.transportCodesForV2
      case 3 if hasPCsEqualTo0019(request.cacheModel) => tcs.transportCodesForV3WhenPC0019
      case 3                                          => tcs.transportCodesForV3
    }

  /*
   The version of the page's content is chosen under the assumption that the
   user has selected on /transport-leaving-the-border NEITHER 'Postal' or 'FTI'.

   This condition is tested in DepartureTransportController, as guarantee that if
   that is not the case the user won't be able to land on /departure-transport.
   */
  private def versionSelection(implicit request: JourneyRequest[_]): Int =
    if (request.isType(CLEARANCE)) 3
    else if (request.cacheModel.isInlandOrBorder(Border)) 1
    else 2
}
