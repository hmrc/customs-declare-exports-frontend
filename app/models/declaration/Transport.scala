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

package models.declaration

import forms.declaration.TransportPayment
import play.api.libs.json.{Format, Json}

case class Transport(
  transportPayment: Option[TransportPayment] = None,
  containers: Option[Seq[Container]] = None,
  borderModeOfTransportCode: Option[String] = None,
  meansOfTransportOnDepartureType: Option[String] = None,
  meansOfTransportOnDepartureIDNumber: Option[String] = None,
  meansOfTransportCrossingTheBorderNationality: Option[String] = None,
  meansOfTransportCrossingTheBorderType: Option[String] = None,
  meansOfTransportCrossingTheBorderIDNumber: Option[String] = None
) {

  def addOrUpdateContainer(updatedContainer: Container): Transport = {
    val updatedContainers = {
      if (containers.isEmpty) {
        Seq(updatedContainer)
      } else if (hasContainer(updatedContainer.id)) {
        containers.get.map {
          case container if updatedContainer.id == container.id => updatedContainer
          case otherContainer                                   => otherContainer
        }
      } else {
        containers.get :+ updatedContainer
      }
    }
    copy(containers = Some(updatedContainers))
  }

  private def hasContainer(id: String) = containers.exists(_.exists(_.id == id))

  def hasContainers: Boolean = containers.exists(_.nonEmpty)

}

object Transport {
  implicit val format: Format[Transport] = Json.format[Transport]
}
