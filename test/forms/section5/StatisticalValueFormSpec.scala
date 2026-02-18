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
import forms.section5.StatisticalValue.statisticalValueKey
import models.viewmodels.TariffContentKey
import play.api.data.FormError

class StatisticalValueFormSpec extends DeclarationPageBaseSpec {

  "StatisticalValue form" should {

    "return a form without errors" when {
      "provided with valid values" in {
        val form = StatisticalValue.form.bind(correctStatisticalValueMap("1234567890123.45"))
        form.hasErrors mustBe false
      }

      "not provided with a value when the form is optional" in {
        val form = StatisticalValue.formOptional.bind(correctStatisticalValueMap(""))
        form.hasErrors mustBe false
      }
    }

    "return a form with errors" when {

      "the statistical value is too long" in {
        val form = StatisticalValue.form.bind(correctStatisticalValueMap("1234567890123456"))
        form.errors mustBe List(FormError(statisticalValueKey, "declaration.statisticalValue.error.length"))
      }

      "the statistical value contains too many decimals" in {
        val form = StatisticalValue.form.bind(correctStatisticalValueMap("123.456"))
        form.errors mustBe List(FormError(statisticalValueKey, "declaration.statisticalValue.error.wrongFormat"))
      }

      "the statistical value contains non-digit characters" in {
        val form = StatisticalValue.form.bind(correctStatisticalValueMap("12a.456"))
        form.errors mustBe List(FormError(statisticalValueKey, "declaration.statisticalValue.error.wrongFormat"))
      }

      "not provided with a value" in {
        val form = StatisticalValue.form.bind(correctStatisticalValueMap(""))
        form.errors mustBe List(FormError(statisticalValueKey, "declaration.statisticalValue.error.empty"))
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] = List(TariffContentKey(s"${messageKey}.common"))

  "StatisticalValue form" when {
    testTariffContentKeysNoSpecialisation(StatisticalValue, "tariff.declaration.item.statisticalValue")
  }

  private def correctStatisticalValueMap(value: String): Map[String, String] =
    Map(statisticalValueKey -> value)
}
