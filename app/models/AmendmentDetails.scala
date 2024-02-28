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

package models

import models.ExportsFieldPointer.ExportsFieldPointer
import play.api.i18n.Messages

trait AmendmentOp {

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String
  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String
}

trait Amendment extends AmendmentOp {

  def value: String
  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String
}

object AmendmentRow {

  def forAddedValue(pointer: ExportsFieldPointer, fieldId: String, newValue: String): String =
    forAmendedValue(pointer, fieldId, "", newValue)

  def forAmendedValue(pointer: ExportsFieldPointer, fieldId: String, oldValue: String, newValue: String): String =
    s"""<tr class="govuk-table__row ${pointer.replaceAll("\\.#?", "-")}">
       |  <td class="govuk-table__cell govuk-table__cell_break-word">$fieldId</th>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">$oldValue</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">$newValue</td>
       |</tr>""".stripMargin

  def forRemovedValue(pointer: ExportsFieldPointer, fieldId: String, oldValue: String): String =
    forAmendedValue(pointer, fieldId, oldValue, "")

  def safeMessage(key: String, default: Any)(implicit messages: Messages): String =
    if (messages.isDefinedAt(key)) messages(key) else default.toString

  def pointerToSelector(head: ExportsFieldPointer, tail: ExportsFieldPointer): ExportsFieldPointer =
    if (head.endsWith(tail)) head else s"$head.$tail"
}
