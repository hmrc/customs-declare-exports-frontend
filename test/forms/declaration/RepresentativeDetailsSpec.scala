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

package forms.declaration

import forms.declaration.EntityDetailsSpec._
import forms.declaration.RepresentativeDetails.StatusCodes.DirectRepresentative
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class RepresentativeDetailsSpec extends WordSpec with MustMatchers {
  import RepresentativeDetailsSpec._

  "RepresentativeDetails" should {
    "convert itself to representative details properties" in {
      val representativeDetails = correctRepresentativeDetails
      val countryCode = "PL"
      val expectedRepresentativeAddressProperties: Map[String, String] = Map(
        "declaration.agent.id" -> representativeDetails.details.flatMap(_.eori).get,
        "declaration.agent.name" -> representativeDetails.details.flatMap(_.address).get.fullName,
        "declaration.agent.address.line" -> representativeDetails.details.flatMap(_.address).get.addressLine,
        "declaration.agent.address.cityName" -> representativeDetails.details.flatMap(_.address).get.townOrCity,
        "declaration.agent.address.postcodeId" -> representativeDetails.details.flatMap(_.address).get.postCode,
        "declaration.agent.address.countryCode" -> countryCode,
        "declaration.agent.functionCode" -> representativeDetails.statusCode.get
      )

      representativeDetails.toMetadataProperties() mustEqual expectedRepresentativeAddressProperties
    }
  }

  "RepresentativeDetails mapping used for binding data" should {

    "return form with errors" when {
      "provided with empty value for status code" in {
        val representativeDetailsWithoutStatusCode = JsObject(Map("details" -> correctEntityDetailsJSON))
        val form = RepresentativeDetails.form().bind(representativeDetailsWithoutStatusCode)

        form.hasErrors mustBe true
        form.errors.length mustEqual 1
        form.errors.head.message mustEqual "supplementary.representative.representationType.error.empty"
      }

      "provided with unrecognized status code" in {
        val representativeDetailsWithoutStatusCode =
          JsObject(Map("details" -> correctEntityDetailsJSON, "statusCode" -> JsString("Invalid")))
        val form = RepresentativeDetails.form().bind(representativeDetailsWithoutStatusCode)

        form.hasErrors mustBe true
        form.errors.length mustEqual 1
        form.errors.head.message mustEqual "supplementary.representative.representationType.error.wrongValue"
      }
    }

    "return form without errors" when {
      "provided with valid value for status code" in {
        val form = RepresentativeDetails.form().bind(correctRepresentativeDetailsJSON)

        form.hasErrors mustBe false
      }
    }

  }

}

object RepresentativeDetailsSpec {
  val correctRepresentativeDetails =
    RepresentativeDetails(
      details = Some(EntityDetailsSpec.correctEntityDetails),
      statusCode = Some(DirectRepresentative)
    )
  val correctRepresentativeDetailsEORIOnly =
    RepresentativeDetails(
      details = Some(EntityDetailsSpec.correctEntityDetailsEORIOnly),
      statusCode = Some(DirectRepresentative)
    )
  val correctRepresentativeDetailsAddressOnly = RepresentativeDetails(
    details = Some(EntityDetailsSpec.correctEntityDetailsAddressOnly),
    statusCode = Some(DirectRepresentative)
  )
  val emptyRepresentativeDetails =
    RepresentativeDetails(details = None, statusCode = None)

  val correctRepresentativeDetailsJSON: JsValue = JsObject(
    Map("details" -> correctEntityDetailsJSON, "statusCode" -> JsString(DirectRepresentative))
  )

  val representativeDetailsWithEmptyFullNameJSON: JsValue = JsObject(
    Map(
      "details" -> EntityDetailsSpec.entityDetailsWithEmptyFullNameJSON,
      "statusCode" -> JsString(DirectRepresentative)
    )
  )
  val correctRepresentativeDetailsEORIOnlyJSON: JsValue = JsObject(
    Map("details" -> correctEntityDetailsEORIOnlyJSON, "statusCode" -> JsString(DirectRepresentative))
  )
  val correctRepresentativeDetailsAddressOnlyJSON: JsValue = JsObject(
    Map("details" -> correctEntityDetailsAddressOnlyJSON, "statusCode" -> JsString(DirectRepresentative))
  )
  val emptyRepresentativeDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}
