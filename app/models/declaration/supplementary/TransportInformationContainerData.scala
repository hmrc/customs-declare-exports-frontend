/*
 * Copyright 2019 HM Revenue & Customs
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

package models.declaration.supplementary

import forms.MetadataPropertiesConvertable
import forms.supplementary.TransportInformationContainer
import play.api.libs.json.Json

case class TransportInformationContainerData(containers: Seq[TransportInformationContainer])
    extends MetadataPropertiesConvertable {
  override def toMetadataProperties(): Map[String, String] =
    containers.zipWithIndex.map { container =>
      Map(
        "declaration.goodsShipment.consignment.transportEquipments[" + container._2 + "].id" -> container._1.id
      )
    }.fold(Map.empty)(_ ++ _)

  def containsItem(container: TransportInformationContainer): Boolean = containers.contains(container)
}

object TransportInformationContainerData {
  implicit val format = Json.format[TransportInformationContainerData]

  val id = "TransportInformationContainerData"

  val maxNumberOfItems = 9999
}
