/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.declaration.exporter

import base.JourneyTypeTestRunner
import forms.LightFormMatchers
import forms.common.DeclarationPageBaseSpec
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.EntityDetailsSpec
import forms.declaration.EntityDetailsSpec.emptyEntityDetailsJSON
import models.ExportsDeclaration
import models.requests.JourneyRequest
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsValue}

class ExporterDetailsSpec extends WordSpec with MustMatchers with LightFormMatchers with JourneyTypeTestRunner with DeclarationPageBaseSpec {

  val emptyExporterDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))

  private def form(model: Option[ExportsDeclaration])(implicit request: JourneyRequest[_]): Form[ExporterDetails] =
    ExporterDetails.form(request.declarationType, model)

  onClearance { implicit request =>
    s"Exporter Details form for ${request.declarationType}" when {

      "it is EIDR" should {
        "validate is eori and address is non empty" in {

          val cachedModel: ExportsDeclaration = aDeclaration(withEntryIntoDeclarantsRecords(YesNoAnswers.yes))

          val errors = form(Some(cachedModel)).bind(emptyExporterDetailsJSON).errors
          EntityDetailsSpec.assertEmptyDetails(errors)
        }
      }

      "it is not EIDR" should {
        "allow an empty eori and empty address" in {

          val cachedModel = aDeclaration(withEntryIntoDeclarantsRecords(YesNoAnswers.no))

          val result = form(Some(cachedModel)).bind(emptyExporterDetailsJSON)
          result mustBe errorless
        }
      }
    }
  }

  "ExporterDetails" when {
    testTariffContentKeys(ExporterDetails, "tariff.declaration.exporterAddress")
  }
}
