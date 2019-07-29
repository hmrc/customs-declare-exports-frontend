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
import forms.Choice.AllowedChoiceValues
import forms.ChoiceSpec
import forms.declaration.officeOfExit.OfficeOfExitForms
import forms.declaration.{OfficeOfExitStandardSpec, OfficeOfExitSupplementarySpec}
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

class ExitOfficeBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {

  "ExitOfficeBuilder" should {
    "correctly map to the WCO-DEC ExitOffice instance for a supplementary journey" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(OfficeOfExitForms.formId -> OfficeOfExitSupplementarySpec.correctOfficeOfExitJSON))
      val exitOffice = ExitOfficeBuilder.build(cacheMap, ChoiceSpec.supplementaryChoice)
      exitOffice.getID.getValue should be("123qwe12")
    }

    "correctly map to the WCO-DEC ExitOffice instance for a standard journey" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(OfficeOfExitForms.formId -> OfficeOfExitStandardSpec.correctOfficeOfExitJSON))
      val exitOffice = ExitOfficeBuilder.build(cacheMap, ChoiceSpec.standardChoice)
      exitOffice.getID.getValue should be("123qwe12")
    }

    "build then add" when {
      "standard journey with no data" in {
        val model = aCacheModel(withChoice(AllowedChoiceValues.StandardDec), withoutOfficeOfExit())
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getExitOffice should be(null)
      }

      "supplementary journey with no data" in {
        val model = aCacheModel(withChoice(AllowedChoiceValues.SupplementaryDec), withoutOfficeOfExit())
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getExitOffice should be(null)
      }

      "standard journey with populated data" in {
        val model = aCacheModel(withChoice(AllowedChoiceValues.StandardDec), withOfficeOfExit(officeId = "office-id"))
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getExitOffice.getID.getValue should be("office-id")
      }

      "supplementary journey with populated data" in {
        val model =
          aCacheModel(withChoice(AllowedChoiceValues.SupplementaryDec), withOfficeOfExit(officeId = "office-id"))
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getExitOffice.getID.getValue should be("office-id")
      }
    }
  }

  private def builder = new ExitOfficeBuilder()
}
