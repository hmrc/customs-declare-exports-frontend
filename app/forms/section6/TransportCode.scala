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

package forms.section6

import connectors.CodeLinkConnector
import connectors.Tag.Tag

case class TransportCode(id: String, value: String, useAltRadioTextForV2: Boolean, useAltRadioTextForBorderTransport: Boolean)

object TransportCode {
  def apply(tag: Tag, useAltRadioTextForV2: Boolean = false, useAltRadioTextForBorderTransport: Boolean = false)(
    implicit codeLinkConnector: CodeLinkConnector
  ): TransportCode = {
    val idAndValue = codeLinkConnector.getTransportCodeForTag(tag)
    new TransportCode(idAndValue._1, idAndValue._2, useAltRadioTextForV2, useAltRadioTextForBorderTransport)
  }
}

case class TransportCodes(
  code1: TransportCode,
  code2: TransportCode,
  code3: TransportCode,
  code4: TransportCode,
  code5: TransportCode,
  code6: TransportCode,
  code7: TransportCode,
  code8: TransportCode,
  notProvided: TransportCode,
  maybeNotAvailable: Option[TransportCode] = None
) {
  lazy val asList =
    List(
      Some(code1),
      Some(code2),
      Some(code3),
      Some(code4),
      Some(code5),
      Some(code6),
      Some(code7),
      Some(code8),
      Some(notProvided),
      maybeNotAvailable
    ).flatten
      .map(identity)
}
