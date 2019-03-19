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

import forms.supplementary.AdditionalDeclarationType.AllowedAdditionalDeclarationTypes.Simplified
import forms.supplementary.AdditionalDeclarationTypeSpec._
import forms.supplementary.DispatchLocation.AllowedDispatchLocations.OutsideEU
import forms.supplementary.DispatchLocationSpec._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DeclarationTypeSpec extends WordSpec with MustMatchers {
  import DeclarationTypeSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val declarationType = correctDeclarationType
      val expectedMetadataProperties: Map[String, String] =
        Map(
          "declaration.typeCode" -> (declarationType.dispatchLocation.get.dispatchLocation +
            declarationType.additionalDeclarationType.get.additionalDeclarationType)
        )

      declarationType.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object DeclarationTypeSpec {
  val correctDeclarationType = DeclarationType(Some(correctDispatchLocation), Some(correctAdditionalDeclarationType))
  val emptyDeclarationType = DeclarationType(Some(correctDispatchLocation), Some(correctAdditionalDeclarationType))

  val correctDeclarationTypeJSON: JsValue = JsObject(
    Map("dispatchLocation" -> JsString(OutsideEU), "additionalDeclarationType" -> JsString(Simplified))
  )
  val emptyDeclarationTypeJSON: JsValue = JsObject(
    Map("dispatchLocation" -> JsString(""), "additionalDeclarationType" -> JsString(""))
  )
}
