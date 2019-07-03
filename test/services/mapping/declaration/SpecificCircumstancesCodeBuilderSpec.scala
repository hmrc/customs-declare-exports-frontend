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

package services.mapping.declaration
import forms.ChoiceSpec
import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitStandard}
import forms.declaration.{OfficeOfExitStandardSpec, OfficeOfExitSupplementarySpec}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap

class SpecificCircumstancesCodeBuilderSpec extends WordSpec with Matchers {

  "SpecificCircumstancesCodeBuilder" should {
    "correctly map to the WCO-DEC CircumstancesCode to null for a supplementary journey" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(OfficeOfExitForms.formId -> OfficeOfExitSupplementarySpec.correctOfficeOfExitJSON))
      val circumstancesCode = SpecificCircumstancesCodeBuilder.build(cacheMap, ChoiceSpec.supplementaryChoice)
      circumstancesCode should be(null)
    }

    "correctly map to the WCO-DEC CircumstancesCode instance for a standard journey" when {
      "circumstancesCode is 'Yes'" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(OfficeOfExitForms.formId -> OfficeOfExitStandardSpec.correctOfficeOfExitJSON))
        val circumstancesCode = SpecificCircumstancesCodeBuilder.build(cacheMap, ChoiceSpec.standardChoice)
        circumstancesCode.getValue should be("A20")
      }

      "circumstancesCode is 'No'" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(OfficeOfExitForms.formId -> Json.toJson(OfficeOfExitStandard("123qwe12", "123", "No"))))
        val circumstancesCode = SpecificCircumstancesCodeBuilder.build(cacheMap, ChoiceSpec.standardChoice)
        circumstancesCode should be(null)
      }
    }
  }
}
