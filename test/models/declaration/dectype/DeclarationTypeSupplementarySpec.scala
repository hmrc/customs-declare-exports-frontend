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

package models.declaration.dectype

import forms.declaration.DispatchLocation.AllowedDispatchLocations.OutsideEU
import forms.declaration.DispatchLocationSpec._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes.Simplified
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDecSpec._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}
import uk.gov.hmrc.wco.dec.MetaData

class DeclarationTypeSupplementarySpec extends WordSpec with MustMatchers {
  import DeclarationTypeSupplementarySpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val declarationType = correctDeclarationType
      val expectedTypeCode = declarationType.dispatchLocation.get.dispatchLocation + declarationType.additionalDeclarationType.get.additionalDeclarationType

      val metadata = MetaData.fromProperties(declarationType.toMetadataProperties())

      metadata.declaration must be(defined)
      metadata.declaration.get.typeCode must be(defined)
      metadata.declaration.get.typeCode.get must equal(expectedTypeCode)
    }
  }

}

object DeclarationTypeSupplementarySpec {
  val correctDeclarationType = DeclarationTypeSupplementary(Some(correctDispatchLocation), Some(correctAdditionalDeclarationTypeSupplementaryDec))
  val emptyDeclarationType = DeclarationTypeSupplementary(Some(correctDispatchLocation), Some(correctAdditionalDeclarationTypeSupplementaryDec))

  val correctDeclarationTypeJSON: JsValue = JsObject(
    Map("dispatchLocation" -> JsString(OutsideEU), "additionalDeclarationType" -> JsString(Simplified))
  )
  val emptyDeclarationTypeJSON: JsValue = JsObject(
    Map("dispatchLocation" -> JsString(""), "additionalDeclarationType" -> JsString(""))
  )
}
