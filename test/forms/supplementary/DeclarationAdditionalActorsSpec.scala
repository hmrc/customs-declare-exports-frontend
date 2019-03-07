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

import forms.supplementary.DeclarationAdditionalActors.PartyType.Consolidator
import forms.supplementary.DocumentsProducedSpec.correctDocumentsProducedJSON
import models.declaration.supplementary.DeclarationAdditionalActorsData
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

class DeclarationAdditionalActorsSpec extends WordSpec with MustMatchers {
  import DeclarationAdditionalActorsSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val additionalActorsData = correctAdditionalActorsData
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.aeoMutualRecognitionParties[0].id" -> correctAdditionalActors.eori.get,
        "declaration.goodsShipment.aeoMutualRecognitionParties[0].roleCode" -> correctAdditionalActors.partyType.get
      )

      additionalActorsData.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object DeclarationAdditionalActorsSpec {
  val correctAdditionalActors = DeclarationAdditionalActors(eori = Some("eori1"), partyType = Some(Consolidator))
  val correctAdditionalActorsData = DeclarationAdditionalActorsData(Seq(correctAdditionalActors))
  val emptyAdditionalActors = DeclarationAdditionalActors(eori = None, partyType = None)
  val incorrectAdditionalActors =
    DeclarationAdditionalActors(eori = Some("123456789123456789"), partyType = Some("Incorrect"))

  val correctAdditionalActorsJSON: JsValue = JsObject(
    Map("eori" -> JsString("eori1"), "partyType" -> JsString(Consolidator))
  )

  val correctAdditionalActorsDataJSON: JsValue =
    JsObject(Map("actors" -> JsArray(Seq(correctAdditionalActorsJSON))))

  val emptyAdditionalActorsJSON: JsValue = JsObject(Map("eori" -> JsString(""), "partyType" -> JsString("")))
  val incorrectAdditionalActorsJSON: JsValue = JsObject(
    Map("eori" -> JsString("123456789123456789"), "partyType" -> JsString("Incorrect"))
  )

}
