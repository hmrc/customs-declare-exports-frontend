/*
 * Copyright 2020 HM Revenue & Customs
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

package views.declaration.summary

import forms.common.{Address, Eori}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import views.components.gds.ActionItemBuilder._
import views.html.components.gds.linkContent

object EoriOrAddress {

  def rows(
    key: String,
    eori: Option[Eori],
    address: Option[Address],
    eoriLabel: String,
    eoriChangeLabel: String,
    addressLabel: String,
    addressChangeLabel: String,
    eoriChangeController: Call,
    addressChangeController: Call,
    isEoriDefault: Boolean = true,
    actionsEnabled: Boolean = true
  )(implicit messages: Messages): Seq[Option[SummaryListRow]] = {

    def emptyRow =
      if (eori.isEmpty && address.isEmpty) {
        if (isEoriDefault) {
          Some(rowForEori(key, eoriLabel, eoriChangeLabel, eoriChangeController, None, actionsEnabled))
        } else {
          Some(forForAddress(key, addressLabel, addressChangeLabel, addressChangeController, extractAddress, None, actionsEnabled))
        }
      } else None

    Seq(
      emptyRow,
      eori.map(eori => rowForEori(key, eoriLabel, eoriChangeLabel, eoriChangeController, Some(eori), actionsEnabled)),
      address.map(address => forForAddress(key, addressLabel, addressChangeLabel, addressChangeController, extractAddress _, Some(address), actionsEnabled))
    )
  }

  private def actionItems(actionsEnabled: Boolean, item: ActionItem) =
    if (actionsEnabled) Seq(item)
    else Seq.empty

  private def extractAddress(address: Address) =
    Seq(address.fullName, address.addressLine, address.townOrCity, address.postCode, address.country).mkString("<br>")

  private def rowForEori(
    key: String,
    eoriLabel: String,
    eoriChangeLabel: String,
    changeController: Call,
    maybeEori: Option[Eori],
    actionsEnabled: Boolean
  )(implicit messages: Messages) =
    SummaryListRow(
      classes = s"$key-eori-row",
      key = Key(content = Text(messages(eoriLabel))),
      value = Value(content = maybeEori.map(eori => Text(eori.value)).getOrElse(Empty)),
      actions = Some(
        Actions(
          items = actionItems(
            actionsEnabled,
            actionItem(
              href = changeController.url,
              content = HtmlContent(new linkContent()(messages("site.change"))),
              visuallyHiddenText = Some(messages(eoriChangeLabel))
            )
          )
        )
      )
    )

  private def forForAddress(
    key: String,
    addressLabel: String,
    addressChangeLabel: String,
    changeController: Call,
    extractAddress: Address => String,
    maybeAddress: Option[Address],
    actionsEnabled: Boolean
  )(implicit messages: Messages) =
    SummaryListRow(
      classes = s"$key-address-row",
      key = Key(content = Text(messages(addressLabel))),
      value = Value(content = maybeAddress.map(address => HtmlContent(extractAddress(address))).getOrElse(Empty)),
      actions = Some(
        Actions(
          items = actionItems(
            actionsEnabled,
            actionItem(
              href = changeController.url,
              content = HtmlContent(new linkContent()(messages("site.change"))),
              visuallyHiddenText = Some(messages(addressChangeLabel))
            )
          )
        )
      )
    )
}
