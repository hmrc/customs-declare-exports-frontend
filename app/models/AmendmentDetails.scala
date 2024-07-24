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
  def getLeafPointersIfAny(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer]
}

trait Amendment extends AmendmentOp {
  def value: String
  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer): Seq[ExportsFieldPointer]
}

object AmendmentRow {
  def safeMessage(key: String, default: Any)(implicit messages: Messages): String =
    if (messages.isDefinedAt(key)) messages(key) else default.toString

  def convertToLeafPointer(head: ExportsFieldPointer, tail: ExportsFieldPointer): ExportsFieldPointer =
    if (head.endsWith(tail)) head else s"$head.$tail"
}
