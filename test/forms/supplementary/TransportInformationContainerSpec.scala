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

package forms.supplementary
import forms.supplementary.TransportInformationContainerSpec._
import models.declaration.supplementary.TransportInformationContainerData
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
class TransportInformationContainerSpec extends WordSpec with MustMatchers {

  "Method toMetadataProperties" should {

    "return proper Metadata Properties" in {

      val transportInformationContainerData = correctTransportInformationContainerData

      val expectedProperties: Map[String, String] = Map(
        "declaration.goodsShipment.consignment.transportEquipment[0].id" -> transportInformationContainerData.containers.head.id
      )

      transportInformationContainerData.toMetadataProperties() must equal(expectedProperties)
    }
  }

  "Transport Information Object object" should {
    "contains correct limit value" in {
      TransportInformationContainerData.maxNumberOfItems must be(9999)
    }
  }

}

object TransportInformationContainerSpec {
  private val containerId = "containerId"

  val correctTransportInformationContainerData =
    TransportInformationContainerData(Seq(TransportInformationContainer(id = "M1l3s")))

  val emptyTransportInformationContainerData = TransportInformationContainer("")

  val correctTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("M1l3s")))

  val incorrectTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("123456789012345678")))

  val emptyTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("")))

  val correctTransportInformationContainerDataJSON: JsValue = JsObject(
    Map("containers" -> JsArray(Seq(correctTransportInformationContainerJSON)))
  )
}
