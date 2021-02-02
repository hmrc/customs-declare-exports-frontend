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

import forms.common.DeclarationPageBaseSpec
import forms.declaration.EntityDetailsSpec
import play.api.libs.json.{JsObject, JsValue}
import unit.base.JourneyTypeTestRunner

class ExporterDetailsSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner {

  "ExporterDetails" when {
    testTariffContentKeys(ExporterDetails, "tariff.declaration.exporterAddress")
  }
}

object ExporterDetailsSpec {
  import forms.declaration.EntityDetailsSpec._

  val correctExporterDetails = ExporterDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctExporterDetailsEORIOnly = ExporterDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctExporterDetailsAddressOnly = ExporterDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)

  val correctExporterDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctExporterDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctExporterDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val incorrectExporterDetailsJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val emptyExporterDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}
