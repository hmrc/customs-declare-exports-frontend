/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.common

import forms.MappingHelper.requiredRadio
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.common.YesNoAnswer.{mappingsForAmendment, valueForYesNo}
import models.{Amendment, ExportsDeclaration}
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.{Parties, Transport}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class YesNoAnswer(answer: String) extends Ordered[YesNoAnswer] with Amendment {

  def isYes: Boolean = answer == yes

  override def compare(that: YesNoAnswer): Int = answer.compare(that.answer)

  def value: String = answer

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAddedValue(pointer, messages(mappingsForAmendment(pointer)), valueForYesNo(answer))

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(mappingsForAmendment(pointer)), valueForYesNo(answer), valueForYesNo(newValue.value))

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forRemovedValue(pointer, messages(mappingsForAmendment(pointer)), valueForYesNo(answer))
}

object YesNoAnswer {

  implicit val format: OFormat[YesNoAnswer] = Json.format[YesNoAnswer]

  private lazy val parties = s"${ExportsDeclaration.pointer}.${Parties.pointer}"
  private lazy val transport = s"${ExportsDeclaration.pointer}.${Transport.pointer}"

  lazy val mappingsForAmendment = Map(
    s"$parties.personPresentingGoodsDetails.eori" -> "declaration.summary.parties.eidr",
    s"$transport.expressConsignment" -> "declaration.summary.transport.expressConsignment"
  )

  def valueForYesNo(isYes: Boolean)(implicit messages: Messages): String =
    messages(if (isYes) "site.yes" else "site.no")

  def valueForYesNo(value: String)(implicit messages: Messages): String =
    messages(if (value.toLowerCase == "yes") "site.yes" else "site.no")

  object YesNoAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import YesNoAnswers._

  val allowedValues: Seq[String] = Seq(yes, no)

  val Yes = Some(YesNoAnswer(yes))
  val No = Some(YesNoAnswer(no))

  val allYesNoAnswers = Seq(YesNoAnswers.yes, YesNoAnswers.no)

  private def mapping(fieldName: String, errorKey: String): Mapping[YesNoAnswer] =
    Forms.mapping(
      fieldName -> requiredRadio(errorKey)
        .verifying(errorKey, isContainedIn(allowedValues))
    )(YesNoAnswer.apply)(YesNoAnswer.unapply)

  val formId = "yesNo"

  def form(fieldName: String = formId, errorKey: String = "error.yesNo.required"): Form[YesNoAnswer] =
    Form(mapping(fieldName, errorKey))
}
