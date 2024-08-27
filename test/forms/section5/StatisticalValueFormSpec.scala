/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section5

import forms.common.DeclarationPageBaseSpec
import forms.section5.StatisticalValue._
import forms.section5.StatisticalValueFormSpec.correctStatisticalValueMap
import models.viewmodels.TariffContentKey
import play.api.data.FormError

class StatisticalValueFormSpec extends DeclarationPageBaseSpec {

  "Item Type form" should {

    "return form without errors" when {
      "provided with valid values" in {
        val form = StatisticalValue.form.bind(correctStatisticalValueMap)
        form.hasErrors must be(false)
      }
    }

    "return form with errors" when {
      "statisticalValue is missing" in {
        val form = StatisticalValue.form.bind(correctStatisticalValueMap - "statisticalValue")

        form.errors mustBe Seq(FormError("statisticalValue", "error.required"))
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"))

  "StatisticalValue" when {
    testTariffContentKeysNoSpecialisation(StatisticalValue, "tariff.declaration.item.statisticalValue")
  }
}

object StatisticalValueFormSpec {
  private val statisticalValue = "1234567890123.45"

  val correctStatisticalValueMap: Map[String, String] =
    Map(statisticalValueKey -> statisticalValue)
}
