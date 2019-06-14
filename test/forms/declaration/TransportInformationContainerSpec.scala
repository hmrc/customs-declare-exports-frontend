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

import base.CustomExportsBaseSpec
import forms.FormMatchers
import forms.declaration.TransportInformationContainer.{mapping, maxContainerIdLength}
import generators.Generators
import models.declaration.TransportInformationContainerData
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{alphaNumStr, asciiStr}
import org.scalatest.MustMatchers
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.libs.json._
import services.mapping.governmentagencygoodsitem.GovernmentAgencyGoodsItemData
import utils.validators.forms.FieldValidator.isAlphanumeric
class TransportInformationContainerSpec
    extends CustomExportsBaseSpec with MustMatchers with PropertyChecks with Generators with FormMatchers
    with GovernmentAgencyGoodsItemData {

  "Transport Information Object object" should {
    "contains correct limit value" in {
      TransportInformationContainerData.maxNumberOfItems must be(9999)
    }
  }

  "TransportInformationContainerSpec" should {
    "fail" when {
      "id is empty" in {
        Form(mapping)
          .fillAndValidate(TransportInformationContainer(""))
          .fold(_ must haveErrorMessage("Container ID cannot be empty"), _ => fail("form should not succeed"))
      }

      "try to add a longer id" in {
        implicit val arbitraryTransportInformationContainer: Arbitrary[TransportInformationContainer] = Arbitrary {
          for {
            id <- alphaNumStr.suchThat(_.length > maxContainerIdLength)
          } yield TransportInformationContainer(id.toString)
        }

        forAll(arbitrary[TransportInformationContainer]) { container =>
          Form(mapping)
            .fillAndValidate(container)
            .fold(
              _ must haveErrorMessage("Only 17 alphanumeric characters are allowed"),
              _ => fail("form should not succeed")
            )
        }
      }

      "try to add special characters" in {
        implicit val arbitraryTransportInformationContainer: Arbitrary[TransportInformationContainer] = Arbitrary {
          for {
            id <- asciiStr.suchThat(str => str.length > 0 && !isAlphanumeric(str))
          } yield TransportInformationContainer(id.toString)
        }

        forAll(arbitrary[TransportInformationContainer]) { container =>
          Form(mapping)
            .fillAndValidate(container)
            .fold(_ must haveErrorMessage("Only alphanumeric characters allowed"), _ => fail("form should not succeed"))
        }
      }
    }
  }
}

object TransportInformationContainerSpec {
  val correctTransportInformationContainerData =
    TransportInformationContainerData(Seq(TransportInformationContainer(id = "M1l3s")))
  val emptyTransportInformationContainerData = TransportInformationContainer("")
  val correctTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("container-M1l3s")))
  val incorrectTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("123456789012345678")))
  val emptyTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("")))
  val correctTransportInformationContainerDataJSON: JsValue = Json.toJson(correctTransportInformationContainerData)
  private val containerId = "id"
}
