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

package forms.section1

import forms.DeclarationPage
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import play.api.data.Forms._
import play.api.data.{Form, FormError, Mapping}
import play.api.libs.json.{Format, JsString, Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import utils.validators.forms.FieldValidator._

import scala.concurrent.{ExecutionContext, Future}

case class Lrn(lrn: String) {
  def isEmpty: Boolean = lrn.isEmpty
  def nonEmpty: Boolean = !isEmpty
}

object Lrn extends DeclarationPage {

  implicit val format: Format[Lrn] =
    Format[Lrn](Reads.StringReads.map(Lrn.apply), Writes[Lrn](lrn => JsString(lrn.lrn)))

  private val lrnMaxLength = 22

  val form: Form[Lrn] = Form(mapping("declaration.consignmentReferences.lrn").withPrefix("lrn"))

  def form2Data(lrn: String): Lrn = new Lrn(lrn.trim.toUpperCase)

  def model2Form: Lrn => Option[String] =
    model => Some(model.lrn)

  def mapping(prefix: String): Mapping[Lrn] =
    text()
      .verifying(s"$prefix.error.empty", nonEmpty)
      .verifying(s"$prefix.error.length", isEmpty or isNotAlphanumericWithSpace or noLongerThanAfterTrim(lrnMaxLength))
      .verifying(s"$prefix.error.specialCharacter", isEmpty or isAlphanumericWithSpace)
      .transform[Lrn](form2Data, _.lrn)
  implicit class LrnFormEnhanced(form: Form[Lrn]) {
    def verifyLrnValidity(lrnValidator: LrnValidator)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Form[Lrn]] =
      form.value.fold(Future.successful(form)) { lrn =>
        lrnValidator.hasBeenSubmittedInThePast48Hours(lrn).map {
          case true  => form.copy(errors = Seq(FormError("lrn", "declaration.consignmentReferences.lrn.error.notExpiredYet")))
          case false => form
        }
      }
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE => Seq(TariffContentKey("tariff.declaration.lrn.clearance"))
      case _ =>
        Seq(TariffContentKey("tariff.declaration.lrn.common"))
    }
}
