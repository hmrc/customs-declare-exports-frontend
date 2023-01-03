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

package forms

import forms.declaration.ConsignmentReferences
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, FormError}
import play.api.data.Forms.mapping
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class CopyDeclaration(ducr: Ducr, lrn: Lrn)

object CopyDeclaration extends DeclarationPage {

  implicit val format = Json.format[CopyDeclaration]

  def form: Form[CopyDeclaration] =
    Form(
      mapping("ducr" -> Ducr.mapping, "lrn" -> Lrn.mapping("declaration.consignmentReferences.lrn").verifying())(CopyDeclaration.apply)(
        CopyDeclaration.unapply
      )
    )

  implicit class LrnValidatorHelper(form: Form[CopyDeclaration]) {

    def verifyLrnValidity(lrnValidator: LrnValidator)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Form[CopyDeclaration]] =
      form.value.fold(Future.successful(form)) { copyDeclaration =>
        lrnValidator.hasBeenSubmittedInThePast48Hours(copyDeclaration.lrn).map {
          case true  => form.copy(errors = Seq(FormError("lrn", "declaration.consignmentReferences.lrn.error.notExpiredYet")))
          case false => form
        }
      }
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    ConsignmentReferences.defineTariffContentKeys(decType)
}
