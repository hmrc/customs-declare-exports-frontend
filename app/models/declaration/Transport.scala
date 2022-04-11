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

package models.declaration

import forms.common.YesNoAnswer
import forms.declaration.{TransportCountry, TransportLeavingTheBorder, TransportPayment}
import play.api.libs.json.{Format, Json}

case class Transport(
  expressConsignment: Option[YesNoAnswer] = None,
  transportPayment: Option[TransportPayment] = None,
  containers: Option[Seq[Container]] = None,
  borderModeOfTransportCode: Option[TransportLeavingTheBorder] = None,
  meansOfTransportOnDepartureType: Option[String] = None,
  meansOfTransportOnDepartureIDNumber: Option[String] = None,
  transportCrossingTheBorderNationality: Option[TransportCountry] = None,
  meansOfTransportCrossingTheBorderType: Option[String] = None,
  meansOfTransportCrossingTheBorderIDNumber: Option[String] = None
) {

  def addOrUpdateContainer(updatedContainer: Container): Transport = {

    def containerSequence: Seq[Container] = containers.getOrElse(Seq.empty)

    def hasContainer(id: String) = containers.exists(_.exists(_.id == id))

    val updatedContainers =
      if (containers.isEmpty) Seq(updatedContainer)
      else if (hasContainer(updatedContainer.id)) {
        containerSequence.map {
          case container if updatedContainer.id == container.id => updatedContainer
          case otherContainer                                   => otherContainer
        }
      } else containerSequence :+ updatedContainer

    copy(containers = Some(updatedContainers))
  }
}

object Transport {
  implicit val format: Format[Transport] = Json.format[Transport]
}
