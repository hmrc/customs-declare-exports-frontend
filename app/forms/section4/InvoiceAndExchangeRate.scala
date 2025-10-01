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

package forms.section4

import forms.mappings.MappingHelper.requiredRadio
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.mappings.{AdditionalConstraintsMapping, ConditionalConstraint}
import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.declaration.InvoiceAndPackageTotals
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{FieldMapping, Form, Forms, Mapping}
import uk.gov.voa.play.form.Condition
import utils.validators.forms.FieldValidator._

import scala.util.Try

case class InvoiceAndExchangeRate(
  totalAmountInvoiced: Option[String],
  totalAmountInvoicedCurrency: Option[String],
  agreedExchangeRate: String,
  exchangeRate: Option[String]
)

object InvoiceAndExchangeRate extends DeclarationPage {

  def apply(totals: InvoiceAndPackageTotals): InvoiceAndExchangeRate =
    InvoiceAndExchangeRate(
      totals.totalAmountInvoiced,
      totals.totalAmountInvoicedCurrency,
      totals.agreedExchangeRate.fold("")(x => x),
      totals.exchangeRate
    )

  val formId = "InvoiceAndExchangeRate"
  val totalAmountInvoiced = "totalAmountInvoiced"
  val totalAmountInvoicedCurrency = "totalAmountInvoicedCurrency"
  val agreedExchangeRateYesNo = "agreedExchangeRate"
  val exchangeRate = "exchangeRate"

  val rateFieldErrorKey = "declaration.exchangeRate.error"
  val invoiceFieldErrorKey = "declaration.totalAmountInvoiced.error"
  val invoiceFieldErrorEmptyKey = "declaration.totalAmountInvoiced.error.empty"
  val invoiceFieldErrorLessThan100000Key = "declaration.totalAmountInvoiced.error.lessThan100000"
  val invoiceCurrencyFieldErrorKey = "declaration.totalAmountInvoicedCurrency.error.empty"
  val invoiceCurrencyFieldWithExchangeRateErrorKey = "declaration.totalAmountInvoicedCurrency.exchangeRatePresent.error.invalid"
  val invoiceCurrencyFieldWithoutExchangeRateErrorKey = "declaration.totalAmountInvoicedCurrency.exchangeRateMissing.error.invalid"
  val exchangeRateNoAnswerErrorKey = "declaration.exchangeRate.required.error"
  val exchangeRateYesRadioSelectedErrorKey = "declaration.exchangeRate.yesRadioSelected.error"

  val invoiceLimitForExchangeRate = 100000

  val totalAmountInvoicedPattern = Seq("[0-9]{0,16}[.]{0,1}", "[0-9]{0,15}[.][0-9]{1}", "[0-9]{0,14}[.][0-9]{1,2}").mkString("|")

  val exchangeRatePattern = Seq(
    "[0-9]{0,12}[.]{0,1}",
    "[0-9]{0,11}[.][0-9]{1}",
    "[0-9]{0,10}[.][0-9]{1,2}",
    "[0-9]{0,9}[.][0-9]{1,3}",
    "[0-9]{0,8}[.][0-9]{1,4}",
    "[0-9]{0,7}[.][0-9]{1,5}"
  ).mkString("|")

  val validateWithoutCommas = (validator: String => Boolean) => (input: String) => validator(input.replaceAll(",", ""))
  val isNotOnlyCommas = (input: String) => !input.forall(_.equals(','))
  val validateOptionWithoutCommas = (validator: String => Boolean) =>
    (input: Option[String]) => input.fold(false)(x => validator(x.replaceAll(",", "")))
  val isNotOnlyCommasOption = (input: Option[String]) => !input.fold(false)(_.forall(_.equals(',')))
  val validateAsWholeNumber = (validator: String => Boolean) =>
    (input: String) =>
      validator {
        val decimalPoint = input.indexOf(".")

        if (decimalPoint > 0) input.substring(0, decimalPoint)
        else if (decimalPoint == 0) "0"
        else input
      }

  val equalsIgnoreCaseOptionString = (value: String) => (input: Option[String]) => input.exists(_.equalsIgnoreCase(value))
  val isEmptyOptionString = (input: Option[String]) => isEmpty(input.getOrElse(""))
  val nonEmptyOptionString = (input: Option[String]) => nonEmpty(input.getOrElse(""))
  val isAlphabeticOptionString = (input: Option[String]) => isAlphabetic(input.getOrElse(""))
  val lengthInRangeOptionString = (min: Int) => (max: Int) => (input: Option[String]) => lengthInRange(min)(max)(input.getOrElse(""))

  def isFieldEmpty(field: String): Condition = _.get(field).forall(_.isEmpty())
  def isFieldNotEmpty(field: String): Condition = _.get(field).exists(_.nonEmpty)
  def isFieldIgnoreCaseString(field: String, value: String): Condition = _.get(field).exists(_.equalsIgnoreCase(value))

  def isAmountLessThan(field: String): Condition =
    _.get(field).fold(false) {
      validateAsWholeNumber(
        validateWithoutCommas(x => Try(x.toInt).isSuccess && isNumeric(x) && (x.nonEmpty && x.toInt < invoiceLimitForExchangeRate))
      )
    }

  // We allow the user to enter commas when specifying these optional numerical values but we strip out the commas
  // with `validateWithoutCommas` before validating the number of digits.
  // To prevent the validation from allowing an invalid value like ",,,," we also must use the `isNotOnlyCommas`
  // function to specifically guard against this.
  val mapping: Mapping[InvoiceAndExchangeRate] = Forms.mapping(
    totalAmountInvoiced -> validateTotalAmountInvoiced,
    totalAmountInvoicedCurrency -> validateTotalAmountInvoicedCurrency,
    agreedExchangeRateYesNo -> validateAgreedExchangeRateYesNo,
    exchangeRate -> validateExchangeRate
  )(InvoiceAndExchangeRate.apply)(InvoiceAndExchangeRate.unapply)

  def form: Form[InvoiceAndExchangeRate] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    List(TariffContentKey("tariff.declaration.totalNumbersOfItems.common"))

  private def validateExchangeRate: AdditionalConstraintsMapping[Option[String]] =
    AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(
          isFieldIgnoreCaseString(agreedExchangeRateYesNo, YesNoAnswers.yes) and isFieldNotEmpty(exchangeRate),
          rateFieldErrorKey,
          isNotOnlyCommasOption and validateOptionWithoutCommas(ofPattern(exchangeRatePattern))
        )
      )
    )

  private def validateTotalAmountInvoiced: AdditionalConstraintsMapping[Option[String]] =
    AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(
          isFieldIgnoreCaseString(totalAmountInvoicedCurrency, "GBP") and isAmountLessThan(totalAmountInvoiced),
          invoiceFieldErrorLessThan100000Key,
          isEmptyOptionString
        ),
        ConditionalConstraint(
          isFieldNotEmpty(totalAmountInvoiced),
          invoiceFieldErrorKey,
          isEmptyOptionString or (isNotOnlyCommasOption and validateOptionWithoutCommas(ofPattern(totalAmountInvoicedPattern)))
        ),
        ConditionalConstraint(isFieldEmpty(totalAmountInvoiced), invoiceFieldErrorEmptyKey, nonEmptyOptionString)
      )
    )

  private def validateTotalAmountInvoicedCurrency: AdditionalConstraintsMapping[Option[String]] =
    AdditionalConstraintsMapping(
      optional(text()).transform(_.map(_.toUpperCase), (o: Option[String]) => o),
      Seq(
        ConditionalConstraint(
          isFieldEmpty(totalAmountInvoicedCurrency) and isFieldNotEmpty(totalAmountInvoiced),
          invoiceCurrencyFieldErrorKey,
          (_: Option[String]) => false
        ),
        ConditionalConstraint(
          isFieldIgnoreCaseString(agreedExchangeRateYesNo, YesNoAnswers.yes) and isFieldNotEmpty(exchangeRate),
          invoiceCurrencyFieldWithExchangeRateErrorKey,
          isEmptyOptionString or equalsIgnoreCaseOptionString("GBP")
        ),
        ConditionalConstraint(
          isFieldEmpty(exchangeRate),
          invoiceCurrencyFieldWithoutExchangeRateErrorKey,
          isEmptyOptionString or (isAlphabeticOptionString and lengthInRangeOptionString(3)(3))
        )
      )
    )

  private def validateAgreedExchangeRateYesNo: FieldMapping[String] =
    requiredRadio(exchangeRateNoAnswerErrorKey, YesNoAnswer.allowedValues)
}
