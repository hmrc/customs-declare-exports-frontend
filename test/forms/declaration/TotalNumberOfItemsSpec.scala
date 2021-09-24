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

package forms.declaration

import forms.common.DeclarationPageBaseSpec
import forms.declaration.TotalNumberOfItems.{exchangeRate, invoiceFieldErrorKey, rateFieldErrorKey, totalAmountInvoiced}
import models.viewmodels.TariffContentKey
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

class TotalNumberOfItemsSpec extends DeclarationPageBaseSpec {

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"))

  private def formData(rate: Option[String] = None, amount: Option[String] = None) =
    rate.map(r => Map(exchangeRate -> r)).getOrElse(Map.empty[String, String]) ++
      amount.map(r => Map(totalAmountInvoiced -> r)).getOrElse(Map.empty[String, String])

  private val exchangeFormErrors = Seq(FormError(exchangeRate, rateFieldErrorKey))
  private val invoicedFormErrors = Seq(FormError(totalAmountInvoiced, invoiceFieldErrorKey))

  "TotalNumberOfItems" should {
    testTariffContentKeysNoSpecialisation(TotalNumberOfItems, "tariff.declaration.totalNumbersOfItems")

    "return no errors" when {
      "form fields are empty" in {
        val form = TotalNumberOfItems.form().bind(formData())

        form.errors.size mustBe 0
      }

      "only exchange rate form field is populated" in {
        val form = TotalNumberOfItems.form().bind(formData(rate = Some("12")))

        form.errors.size mustBe 0
      }

      "exchange rate form field is populated with max whole value" in {
        val form = TotalNumberOfItems.form().bind(formData(rate = Some("100000000000")))

        form.errors.size mustBe 0
      }

      "exchange rate form field is populated with max decimal value" in {
        val form = TotalNumberOfItems.form().bind(formData(rate = Some("1000000.12345")))

        form.errors.size mustBe 0
      }

      "exchange rate form field is populated with commas" in {
        val form = TotalNumberOfItems.form().bind(formData(rate = Some(",,1,0,2,3.12")))

        form.errors.size mustBe 0
      }

      "exchange rate form field is populated with a leading period char" in {
        val form = TotalNumberOfItems.form().bind(formData(rate = Some(".12")))

        form.errors.size mustBe 0
      }

      "exchange rate form field is populated with a trailing period char" in {
        val form = TotalNumberOfItems.form().bind(formData(rate = Some("12.")))

        form.errors.size mustBe 0
      }

      "only invoice amount form field is populated" in {
        val form = TotalNumberOfItems.form().bind(formData(amount = Some("12")))

        form.errors.size mustBe 0
      }

      "invoice amount form field is populated with max whole value" in {
        val form = TotalNumberOfItems.form().bind(formData(amount = Some("1000000000000000")))

        form.errors.size mustBe 0
      }

      "invoice amount form field is populated with max decimal value" in {
        val form = TotalNumberOfItems.form().bind(formData(amount = Some("10000000000000.12")))

        form.errors.size mustBe 0
      }

      "invoice amount form field is populated with commas" in {
        val form = TotalNumberOfItems.form().bind(formData(amount = Some(",,1,0,2,3.12")))

        form.errors.size mustBe 0
      }

      "invoice amount form field is populated with a leading period char" in {
        val form = TotalNumberOfItems.form().bind(formData(amount = Some(".12")))

        form.errors.size mustBe 0
      }

      "invoice amount form field is populated with a trailing period char" in {
        val form = TotalNumberOfItems.form().bind(formData(amount = Some("12.")))

        form.errors.size mustBe 0
      }

      "when both form fields are populated" in {
        val form = TotalNumberOfItems.form().bind(formData(rate = Some("12"), amount = Some("12")))

        form.errors.size mustBe 0
      }
    }

    "return errors" when {
      "exchange rate specified" that {
        "contains a char other than a digit, period or comma" in {
          withClue("contains an alpha char") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("12E")))

            form.errors mustBe exchangeFormErrors
          }

          withClue("contains a special char") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("12%")))

            form.errors mustBe exchangeFormErrors
          }
        }

        "contains too many digits" in {
          withClue("More than 12 whole number digits") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("1234567890123")))

            form.errors mustBe exchangeFormErrors
          }

          withClue("12 whole numbers and 1 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("123456789012.1")))

            form.errors mustBe exchangeFormErrors
          }

          withClue("11 whole numbers and 2 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("12345678901.12")))

            form.errors mustBe exchangeFormErrors
          }

          withClue("10 whole numbers and 3 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("1234567890.123")))

            form.errors mustBe exchangeFormErrors
          }

          withClue("9 whole numbers and 4 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("123456789.1234")))

            form.errors mustBe exchangeFormErrors
          }

          withClue("8 whole numbers and 5 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("12345678.12345")))

            form.errors mustBe exchangeFormErrors
          }

          withClue("more than 5 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(rate = Some("0.123456")))

            form.errors mustBe exchangeFormErrors
          }
        }

        "contains only commas" in {
          val form = TotalNumberOfItems.form().bind(formData(rate = Some(",,,")))

          form.errors mustBe exchangeFormErrors
        }
      }

      "invoice amount specified" that {
        "contains a char other than a digit, period or comma" in {
          withClue("contains an alpha char") {
            val form = TotalNumberOfItems.form().bind(formData(amount = Some("12E")))

            form.errors mustBe invoicedFormErrors
          }

          withClue("contains a special char") {
            val form = TotalNumberOfItems.form().bind(formData(amount = Some("12%")))

            form.errors mustBe invoicedFormErrors
          }
        }

        "contains too many digits" in {
          withClue("More than 16 whole number digits") {
            val form = TotalNumberOfItems.form().bind(formData(amount = Some("12345678901234567")))

            form.errors mustBe invoicedFormErrors
          }

          withClue("16 whole numbers and 1 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(amount = Some("1234567890123456.1")))

            form.errors mustBe invoicedFormErrors
          }

          withClue("15 whole numbers and 2 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(amount = Some("123456789012345.12")))

            form.errors mustBe invoicedFormErrors
          }

          withClue("more than 2 decimal digits") {
            val form = TotalNumberOfItems.form().bind(formData(amount = Some("0.123")))

            form.errors mustBe invoicedFormErrors
          }
        }

        "contains only commas" in {
          val form = TotalNumberOfItems.form().bind(formData(amount = Some(",,,")))

          form.errors mustBe invoicedFormErrors
        }
      }
    }
  }
}

object TotalNumberOfItemsSpec {
  val correctTotalNumberOfItemsDecimalValues =
    TotalNumberOfItems(Some("12312312312312.12"), Some("1212121.12345"))
  val correctTotalNumberOfItemsDecimalValuesJSON: JsValue = JsObject(
    Map("totalAmountInvoiced" -> JsString("1212312.12"), "exchangeRate" -> JsString("1212121.12345"))
  )
}
