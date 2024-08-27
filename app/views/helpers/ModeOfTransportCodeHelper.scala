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

import forms.section6.ModeOfTransportCode._
import forms.section6.ModeOfTransportCode
import play.api.i18n.Messages

object ModeOfTransportCodeHelper {

  private val prefix = "declaration.transport.leavingTheBorder.transportMode"

  def transportMode(code: Option[ModeOfTransportCode], decapitalize: Boolean = true)(implicit messages: Messages): String = {
    val key = code match {
      case Some(Maritime)                    => s"$prefix.sea"
      case Some(Rail)                        => s"$prefix.rail"
      case Some(Road)                        => s"$prefix.road"
      case Some(Air)                         => s"$prefix.air"
      case Some(PostalConsignment)           => s"$prefix.postalOrMail"
      case Some(RoRo)                        => s"$prefix.ferry"
      case Some(FixedTransportInstallations) => s"$prefix.fixedTransportInstallations"
      case Some(InlandWaterway)              => s"$prefix.inlandWaterway"
      case _                                 => s"$prefix.unknown"
    }

    if (code == Some(RoRo) || !decapitalize) messages(key)
    else {
      val s = messages(key)
      s"${s.head.toLower}${s.tail}"
    }
  }
}
