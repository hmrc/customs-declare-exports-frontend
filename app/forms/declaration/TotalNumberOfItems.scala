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

import forms.MappingHelper.requiredRadio
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.{AdditionalConstraintsMapping, ConditionalConstraint, DeclarationPage}
import models.DeclarationType.DeclarationType
import models.declaration.Totals
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{nonEmptyText, optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import uk.gov.voa.play.form.Condition
import utils.validators.forms.FieldValidator._

case class TotalNumberOfItems(
  exchangeRate: Option[String],
  totalAmountInvoiced: String,
  totalAmountInvoicedCurrency: Option[String],
  exchangeRateAnswer: String
)

object TotalNumberOfItems extends DeclarationPage {
  implicit val format = Json.format[TotalNumberOfItems]

  def apply(totals: Totals): TotalNumberOfItems =
    TotalNumberOfItems(
      totals.exchangeRate,
      totals.totalAmountInvoiced.fold("")(x => x),
      totals.totalAmountInvoicedCurrency,
      totals.exchangeRateAnswer.fold("")(x => x)
    )

  val formId = "TotalNumberOfItems"
  val exchangeRate = "exchangeRate"
  val totalAmountInvoiced = "totalAmountInvoiced"
  val totalAmountInvoicedCurrency = "totalAmountInvoicedCurrency"
  val exchangeRateAnswer = "exchangeRateAnswer"

  val rateFieldErrorKey = "declaration.exchangeRate.error"
  val invoiceFieldErrorKey = "declaration.totalAmountInvoiced.error"
  val invoiceFieldErrorEmptyKey = "declaration.totalAmountInvoiced.error.empty"
  val invoiceCurrencyFieldErrorKey = "declaration.totalAmountInvoicedCurrency.error.empty"
  val invoiceCurrencyFieldWithExchangeRateErrorKey = "declaration.totalAmountInvoicedCurrency.exchangeRatePresent.error.invalid"
  val invoiceCurrencyFieldWithoutExchangeRateErrorKey = "declaration.totalAmountInvoicedCurrency.exchangeRateMissing.error.invalid"
  val exchangeRateNoFixedRateErrorKey = "declaration.exchangeRate.noFixedRate.error"
  val exchangeRateNoAnswerErrorKey = "declaration.exchangeRate.noAnswer.error"
  val exchangeRateYesRadioSelectedErrorKey = "declaration.exchangeRate.yesRadioSelected.error"

  val totalAmountInvoicedPattern = Seq("[0-9]{0,16}[.]{0,1}", "[0-9]{0,15}[.][0-9]{1}", "[0-9]{0,14}[.][0-9]{1,2}").mkString("|")

  val exchangeRatePattern = Seq(
    "[0-9]{0,12}[.]{0,1}",
    "[0-9]{0,11}[.][0-9]{1}",
    "[0-9]{0,10}[.][0-9]{1,2}",
    "[0-9]{0,9}[.][0-9]{1,3}",
    "[0-9]{0,8}[.][0-9]{1,4}",
    "[0-9]{0,7}[.][0-9]{1,5}"
  ).mkString("|")

  val removeCommasFirst = (validator: String => Boolean) => (input: String) => validator(input.replaceAll(",", ""))
  val notJustCommas = (input: String) => !input.forall(_.equals(','))
  val removeCommasFirstOption = (validator: String => Boolean) => (input: Option[String]) => input.fold(false)(x => validator(x.replaceAll(",", "")))
  val notJustCommasOption = (input: Option[String]) => !input.fold(false)(_.forall(_.equals(',')))

  val equalsIgnoreCaseOptionString = (value: String) => (input: Option[String]) => input.exists(_.equalsIgnoreCase(value))
  val isEmptyOptionString = (input: Option[String]) => isEmpty(input.getOrElse(""))
  val nonEmptyOptionString = (input: Option[String]) => nonEmpty(input.getOrElse(""))
  val isAlphabeticOptionString = (input: Option[String]) => isAlphabetic(input.getOrElse(""))
  val lengthInRangeOptionString = (min: Int) => (max: Int) => (input: Option[String]) => lengthInRange(min)(max)(input.getOrElse(""))

  def isFieldEmpty(field: String): Condition = _.get(field).forall(_.isEmpty())
  def isFieldNotEmpty(field: String): Condition = _.get(field).exists(_.nonEmpty)
  def isFieldIgnoreCaseString(field: String, value: String): Condition = _.get(field).exists(_.equalsIgnoreCase(value))
  def isAmountLessThan(field: String, amount: Int): Condition = _.get(field).fold(false)(x => x.nonEmpty && x.toInt < amount)

  //We allow the user to enter commas when specifying these optional numerical values but we strip out the commas with `removeCommasFirst` before validating
  //the number of digits. To prevent the validation from allowing an invalid value like ",,,," we also must use the `notJustCommas`
  //function to specifically guard against this.
  val mapping = Forms.mapping(
    exchangeRate ->
      AdditionalConstraintsMapping(
        optional(text()).transform(_.map(_.toUpperCase), (o: Option[String]) => o),
        Seq(
          ConditionalConstraint(
            isFieldIgnoreCaseString(totalAmountInvoicedCurrency, "GBP") and isAmountLessThan(totalAmountInvoiced, 100000),
            exchangeRateNoFixedRateErrorKey,
            isEmptyOptionString
          ),
          ConditionalConstraint(
            isFieldNotEmpty(exchangeRate),
            rateFieldErrorKey,
            notJustCommasOption and removeCommasFirstOption(ofPattern(exchangeRatePattern))
          ),
          ConditionalConstraint(isFieldIgnoreCaseString(exchangeRateAnswer, "Yes"), exchangeRateYesRadioSelectedErrorKey, nonEmptyOptionString)
        )
      ),
    totalAmountInvoiced ->
      text()
        .verifying(invoiceFieldErrorEmptyKey, nonEmpty)
        .verifying(invoiceFieldErrorKey, isEmpty or (notJustCommas and removeCommasFirst(ofPattern(totalAmountInvoicedPattern)))),
    totalAmountInvoicedCurrency ->
      AdditionalConstraintsMapping(
        optional(text()).transform(_.map(_.toUpperCase), (o: Option[String]) => o),
        Seq(
          ConditionalConstraint(
            isFieldEmpty(totalAmountInvoicedCurrency) and isFieldNotEmpty(totalAmountInvoiced),
            invoiceCurrencyFieldErrorKey,
            (input: Option[String]) => false
          ),
          ConditionalConstraint(
            isFieldNotEmpty(exchangeRate),
            invoiceCurrencyFieldWithExchangeRateErrorKey,
            isEmptyOptionString or equalsIgnoreCaseOptionString("GBP")
          ),
          ConditionalConstraint(
            isFieldEmpty(exchangeRate),
            invoiceCurrencyFieldWithoutExchangeRateErrorKey,
            isEmptyOptionString or (isAlphabeticOptionString and lengthInRangeOptionString(3)(3))
          )
        )
      ),
    exchangeRateAnswer ->
      requiredRadio(exchangeRateNoAnswerErrorKey, YesNoAnswer.allowedValues)
  )(TotalNumberOfItems.apply)(TotalNumberOfItems.unapply)

  def form(): Form[TotalNumberOfItems] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.totalNumbersOfItems.2.common"), TariffContentKey("tariff.declaration.totalNumbersOfItems.1.common"))
}
