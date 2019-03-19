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

import forms.supplementary.DeclarationAdditionalActorsSpec._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsValue}

class DeclarationAdditionalActorsDataSpec extends WordSpec with MustMatchers {
  import DeclarationAdditionalActorsDataSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val additionalActorsData = correctAdditionalActorsData
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.aeoMutualRecognitionParties[0].id" -> correctAdditionalActors1.eori.get,
        "declaration.goodsShipment.aeoMutualRecognitionParties[0].roleCode" -> correctAdditionalActors1.partyType.get,
        "declaration.goodsShipment.aeoMutualRecognitionParties[1].id" -> correctAdditionalActors2.eori.get,
        "declaration.goodsShipment.aeoMutualRecognitionParties[1].roleCode" -> correctAdditionalActors2.partyType.get
      )

      additionalActorsData.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }
}

object DeclarationAdditionalActorsDataSpec {
  val correctAdditionalActorsData = DeclarationAdditionalActorsData(
    Seq(correctAdditionalActors1, correctAdditionalActors2)
  )

  val correctAdditionalActorsDataJSON: JsValue = JsObject(Map("actors" -> JsArray(Seq(correctAdditionalActorsJSON))))
  val emptyAdditionalActorsDataJSON = JsObject(Map("actors" -> JsArray(Seq(emptyAdditionalActorsJSON))))
}
