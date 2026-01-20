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

import forms.section6.BorderTransport.radioButtonGroupId
import forms.section6.{BorderTransport, TransportCode}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html
import services.TransportCodeService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import views.html.components.gds.exportsInputText

import javax.inject.{Inject, Singleton}

@Singleton
class BorderTransportHelper @Inject() (exportsInputText: exportsInputText, transportCodeService: TransportCodeService) {

  private val prefix = "declaration.transportInformation.meansOfTransport"

  def radioButtons(form: Form[BorderTransport])(implicit messages: Messages): List[RadioItem] = {
    val (h :: t) = transportCodeService.transportCodesOnBorderTransport.map(radioButton(form, _)).reverse
    List(h, RadioItem(divider = Some(messages("site.radio.divider")))).concat(t).reverse
  }


  //:+
//      RadioItem(divider = Some(messages("site.radio.divider"))) :+
//      RadioItem(
//      id = Some(s"radio_undeclared"),
//      value = Some(""),
//      content = Text(messages("bananafruitcake")),
//      checked = form(radioButtonGroupId).value.contains(""),
//    )

  def titleInHeadTag(hasErrors: Boolean)(implicit messages: Messages, request: JourneyRequest[_]): Title = {
    val transportMode = ModeOfTransportCodeHelper.transportMode(request.cacheModel.transportLeavingBorderCode)
    Title(s"$prefix.crossingTheBorder.title", "declaration.section.6", transportMode, hasErrors = hasErrors)
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

  private def radioButton(form: Form[BorderTransport], transportCode: TransportCode)(implicit messages: Messages): RadioItem = {
    //if transportcode == NotProvided handle case accordingly
    transportCode.id match {
      case "NotApplicable" =>
        RadioItem(
          id = Some(s"radios_undeclared"),
          value = Some(""),
          content = Text("bananafruitcake"),
          checked = form(radioButtonGroupId).value.contains(""),
        )
      case _ =>
        RadioItem(
          id = Some(s"radio_${transportCode.id}"),
          value = Some(transportCode.value),
          content = Text(messages(s"$prefix.${transportCode.id}${if (transportCode.useAltRadioTextForBorderTransport) ".vBT" else ""}")),
          conditionalHtml = if (transportCode != transportCodeService.NotApplicable) inputField(transportCode, form) else None,
          checked = form(radioButtonGroupId).value.contains(transportCode.value)
        )
    }
  }

  //  RadioItem(
//    id = Some(s"radio_undeclared"),
//    value = Some(""),
//    content = Text("bananafruitcake"),
//    checked = form(radioButtonGroupId).value.contains(""),
//    divider = messages("or")
//  )


}
