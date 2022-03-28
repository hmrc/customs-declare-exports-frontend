/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.TotalNumberOfItems._
import models.viewmodels.TariffContentKey
import play.api.data.FormError

class TotalNumberOfItemsSpec extends DeclarationPageBaseSpec {

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"))

  private def mapTo(formData: Option[String], fieldName: String) =
    formData.map(r => Map(fieldName -> r)).getOrElse(Map.empty[String, String])

  private def formData(
    rate: Option[String] = None,
    amount: Option[String] = None,
    currency: Option[String] = None,
    rateYesNo: Option[String] = None
  ): Map[String, String] =
    mapTo(rate, exchangeRate) ++
      mapTo(amount, totalAmountInvoiced) ++
      mapTo(currency, totalAmountInvoicedCurrency) ++
      mapTo(rateYesNo, agreedExchangeRateYesNo)

  private def exchangeRateWithValidFields(exchangeRate: Option[String]): Map[String, String] =
    formData(
      rate = exchangeRate,
      amount = Some(validTotalAmountInvoicedOver100k),
      currency = Some(validCurrencyCode),
      rateYesNo = Some(YesNoAnswers.yes)
    )

  private def invoiceAmountWithValidFields(invoiceAmount: Option[String]): Map[String, String] =
    formData(rate = None, amount = invoiceAmount, currency = Some(validCurrencyCode), rateYesNo = Some(YesNoAnswers.no))

  private val exchangeFormErrors = Seq(FormError(exchangeRate, rateFieldErrorKey))
  private val invoicedFormErrors = Seq(FormError(totalAmountInvoiced, invoiceFieldErrorKey))
  private val currencyEmptyFormErrors = Seq(FormError(totalAmountInvoicedCurrency, invoiceCurrencyFieldErrorKey))
  private val currencyInvalidWithoutExchangeRateFormErrors = Seq(
    FormError(totalAmountInvoicedCurrency, invoiceCurrencyFieldWithoutExchangeRateErrorKey)
  )
  private val currencyInvalidWithExchangeRateFormErrors = Seq(FormError(totalAmountInvoicedCurrency, invoiceCurrencyFieldWithExchangeRateErrorKey))

  private val validCurrencyCode = "GBP"
  private val validTotalAmountInvoicedOver100k = "100001"
  private val validExchangeRate = "100"

  "TotalNumberOfItems" should {
    testTariffContentKeysNoSpecialisation(TotalNumberOfItems, "tariff.declaration.totalNumbersOfItems")

    "return no errors" when {

      "exchange rate form field" when {

        "populated with max whole value" in {
          val form = TotalNumberOfItems
            .form()
            .bind(exchangeRateWithValidFields(Some("100000000000")))
          form.errors.size mustBe 0
        }

        "populated with max decimal value" in {
          val form = TotalNumberOfItems
            .form()
            .bind(exchangeRateWithValidFields(Some("1000000.12345")))
          form.errors.size mustBe 0
        }

        "populated with commas" in {
          val form = TotalNumberOfItems
            .form()
            .bind(exchangeRateWithValidFields(Some(",,1,0,2,3.12")))
          form.errors.size mustBe 0
        }

        "populated with a leading period char" in {
          val form = TotalNumberOfItems
            .form()
            .bind(exchangeRateWithValidFields(Some(".12")))
          form.errors.size mustBe 0
        }

        "populated with a trailing period char" in {
          val form = TotalNumberOfItems
            .form()
            .bind(exchangeRateWithValidFields(Some("12.")))
          form.errors.size mustBe 0
        }

      }

      "invoice amount form field" when {

        "populated with max whole value" in {
          val form = TotalNumberOfItems
            .form()
            .bind(invoiceAmountWithValidFields(Some("1000000000000000")))
          form.errors.size mustBe 0
        }

        "populated with max decimal value" in {
          val form = TotalNumberOfItems
            .form()
            .bind(invoiceAmountWithValidFields(Some("10000000000000.12")))
          form.errors.size mustBe 0
        }

        "populated with commas" in {
          val form = TotalNumberOfItems
            .form()
            .bind(invoiceAmountWithValidFields(Some(",,1,0,2,3.12")))
          form.errors.size mustBe 0
        }

        "populated with a leading period char" in {
          val form = TotalNumberOfItems
            .form()
            .bind(invoiceAmountWithValidFields(Some(".12")))
          form.errors.size mustBe 0
        }

        "populated with a trailing period char" in {
          val form = TotalNumberOfItems
            .form()
            .bind(invoiceAmountWithValidFields(Some("12.")))
          form.errors.size mustBe 0
        }

      }

      "when all form fields are populated" in {
        val form = TotalNumberOfItems
          .form()
          .bind(
            formData(
              rate = Some(validExchangeRate),
              amount = Some(validTotalAmountInvoicedOver100k),
              currency = Some(validCurrencyCode),
              rateYesNo = Some(YesNoAnswers.yes)
            )
          )
        form.errors.size mustBe 0
      }
    }

    "convert currency of amount invoiced to upper case" when {
      "the user enters a currency in lower case" in {
        val form = TotalNumberOfItems
          .form()
          .bind(formData(currency = Some("gbp"), amount = Some(validTotalAmountInvoicedOver100k), rateYesNo = Some(YesNoAnswers.no)))

        form.errors mustBe empty
        form.value.flatMap(_.totalAmountInvoicedCurrency) mustBe Some("GBP")
      }
    }

    "return errors" when {

      "form fields are empty" in {
        val form = TotalNumberOfItems
          .form()
          .bind(Map(totalAmountInvoiced -> ""))

        form.errors mustBe Seq(
          FormError(totalAmountInvoiced, invoiceFieldErrorEmptyKey),
          FormError(agreedExchangeRateYesNo, exchangeRateNoAnswerErrorKey)
        )
      }

      "exchange rate specified" that {

        "contains a char other than a digit, period or comma" in {
          withClue("contains an alpha char") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("12E"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }

          withClue("contains a special char") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("12%"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }
        }

        "contains too many digits" in {
          withClue("More than 12 whole number digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("1234567890123"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }

          withClue("12 whole numbers and 1 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("1234567890123.1"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }

          withClue("11 whole numbers and 2 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("1234567890123.12"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }

          withClue("10 whole numbers and 3 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("1234567890123.123"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }

          withClue("9 whole numbers and 4 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("1234567890123.1234"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }

          withClue("8 whole numbers and 5 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("1234567890123.12345"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }

          withClue("more than 5 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(
                formData(
                  rate = Some("0.123456"),
                  amount = Some(validTotalAmountInvoicedOver100k),
                  currency = Some("GBP"),
                  rateYesNo = Some(YesNoAnswers.yes)
                )
              )
            form.errors mustBe exchangeFormErrors
          }
        }

        "contains only commas" in {
          val form = TotalNumberOfItems
            .form()
            .bind(
              formData(
                rate = Some(",,,"),
                amount = Some(validTotalAmountInvoicedOver100k),
                currency = Some("GBP"),
                rateYesNo = Some(YesNoAnswers.yes)
              )
            )
          form.errors mustBe exchangeFormErrors
        }
      }

      "invoice amount specified" that {
        "does not have a currency code populated" in {
          val form = TotalNumberOfItems
            .form()
            .bind(formData(amount = Some("12"), rateYesNo = Some(YesNoAnswers.no)))
          form.errors mustBe currencyEmptyFormErrors
        }

        "contains a char other than a digit, period or comma" in {
          withClue("contains an alpha char") {
            val form = TotalNumberOfItems
              .form()
              .bind(invoiceAmountWithValidFields(Some("12E")))
            form.errors mustBe invoicedFormErrors
          }

          withClue("contains a special char") {
            val form = TotalNumberOfItems
              .form()
              .bind(invoiceAmountWithValidFields(Some("12%")))
            form.errors mustBe invoicedFormErrors
          }
        }

        "contains too many digits" in {
          withClue("More than 16 whole number digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(invoiceAmountWithValidFields(Some("12345678901234567")))
            form.errors mustBe invoicedFormErrors
          }

          withClue("16 whole numbers and 1 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(invoiceAmountWithValidFields(Some("1234567890123456.1")))
            form.errors mustBe invoicedFormErrors
          }

          withClue("15 whole numbers and 2 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(invoiceAmountWithValidFields(Some("123456789012345.12")))
            form.errors mustBe invoicedFormErrors
          }

          withClue("more than 2 decimal digits") {
            val form = TotalNumberOfItems
              .form()
              .bind(invoiceAmountWithValidFields(Some("0.123")))
            form.errors mustBe invoicedFormErrors
          }
        }

        "contains only commas" in {
          val form = TotalNumberOfItems
            .form()
            .bind(invoiceAmountWithValidFields(Some(",,,")))
          form.errors mustBe invoicedFormErrors
        }
      }

      "currency code specified" that {

        "is GBP" when {
          "amount invoiced is less than 100,000" when {
            "all numeric" in {
              val form = TotalNumberOfItems
                .form()
                .bind(formData(amount = Some("100"), currency = Some("GBP"), rate = Some("12"), rateYesNo = Some(YesNoAnswers.yes)))
              form.errors mustBe Seq(FormError(exchangeRate, exchangeRateNoFixedRateErrorKey))
            }

            "commas" in {
              val form = TotalNumberOfItems
                .form()
                .bind(formData(amount = Some("10,000"), currency = Some("GBP"), rate = Some("10"), rateYesNo = Some(YesNoAnswers.yes)))
              form.errors mustBe Seq(FormError(exchangeRate, exchangeRateNoFixedRateErrorKey))
            }

            "decimals" when {
              "decimal in number" in {
                val form = TotalNumberOfItems
                  .form()
                  .bind(formData(amount = Some("10.00"), currency = Some("GBP"), rate = Some("10"), rateYesNo = Some(YesNoAnswers.yes)))
                form.errors mustBe Seq(FormError(exchangeRate, exchangeRateNoFixedRateErrorKey))
              }

              "starts with decimal" in {
                val form = TotalNumberOfItems
                  .form()
                  .bind(formData(amount = Some(".10"), currency = Some("GBP"), rate = Some("10"), rateYesNo = Some(YesNoAnswers.yes)))
                form.errors mustBe Seq(FormError(exchangeRate, exchangeRateNoFixedRateErrorKey))
              }
            }
          }
        }

        "has not got an exchange rate specified" should {
          "reject codes that are not three char in length" in {
            withClue("less than three chars") {
              val form = TotalNumberOfItems
                .form()
                .bind(formData(amount = Some("12"), currency = Some("GB"), rateYesNo = Some(YesNoAnswers.no)))
              form.errors mustBe currencyInvalidWithoutExchangeRateFormErrors
            }

            withClue("more than three chars") {
              val form = TotalNumberOfItems
                .form()
                .bind(formData(amount = Some("12"), currency = Some("GBPP"), rateYesNo = Some(YesNoAnswers.no)))
              form.errors mustBe currencyInvalidWithoutExchangeRateFormErrors
            }
          }
        }

        "has an exchange rate specified also" should {
          "not accept any value other than GBP" in {
            val form = TotalNumberOfItems
              .form()
              .bind(formData(rate = Some("12"), amount = Some("12"), currency = Some("USD"), rateYesNo = Some(YesNoAnswers.yes)))

            form.errors mustBe currencyInvalidWithExchangeRateFormErrors
          }
        }
      }
    }
  }
}
