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

package forms

import base.ExportsTestData._
import forms.common.DeclarationPageBaseSpec
import models.viewmodels.TariffContentKey
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CopyDeclarationSpec extends DeclarationPageBaseSpec with ScalaFutures {

  import CopyDeclarationSpec._

  private def boundedForm(data: JsValue): Form[CopyDeclaration] = CopyDeclaration.form.bind(data, Form.FromJsonMaxChars)

  "CopyDeclaration.form.bind" should {

    "return form without errors" when {

      s"provided with valid input" in {
        boundedForm(copyDeclarationJSON).hasErrors mustBe false
      }

      "provided with valid input (lowercase DUCR)" in {
        boundedForm(copyDeclarationLowercaseDucrJSON).hasErrors mustBe false
      }

      "provided with valid input (spaces top and tail of LRN)" in {
        val form = boundedForm(copyDeclarationLrnWithUntrimmmedSpacesJSON)
        form.hasErrors mustBe false
      }

      "provided with valid input (LRN with spaces)" in {
        val form = boundedForm(copyDeclarationLrnWithSpacesJSON)
        form.hasErrors mustBe false
      }
    }

    "return form with errors" when {

      s"provided with empty input" in {
        val form = boundedForm(emptyJSON)

        form.hasErrors mustBe true
        form.errors.length mustBe 2
        form.errors(0).key mustBe "ducr.ducr"
        form.errors(0).message mustBe "error.required"
        form.errors(1).key mustBe "lrn"
        form.errors(1).message mustBe "error.required"
      }

      "provided with invalid input (no DUCR)" in {
        val form = boundedForm(copyDeclarationNoDucrJSON)

        form.hasErrors mustBe true
        form.errors.length mustBe 1
        form.errors.head.key mustBe "ducr.ducr"
        form.errors.head.message mustBe "declaration.consignmentReferences.ducr.error.empty"
      }

      "provided with invalid input (no LRN)" in {
        val form = boundedForm(copyDeclarationNoLrnJSON)

        form.hasErrors mustBe true
        form.errors.length mustBe 1
        form.errors.head.key mustBe "lrn"
        form.errors.head.message mustBe "declaration.consignmentReferences.lrn.error.empty"
      }

      "provided with invalid input (invalid chars in LRN)" in {
        val form = boundedForm(copyDeclarationBadLrnJSON)

        form.hasErrors mustBe true
        form.errors.length mustBe 1
        form.errors.head.key mustBe "lrn"
        form.errors.head.message mustBe "declaration.consignmentReferences.lrn.error.specialCharacter"
      }

      "provided with invalid input (LRN too long)" in {
        val form = boundedForm(copyDeclarationLrnTooLongJSON)

        form.hasErrors mustBe true
        form.errors.length mustBe 1
        form.errors.head.key mustBe "lrn"
        form.errors.head.message mustBe "declaration.consignmentReferences.lrn.error.length"
      }

      "provided with invalid input (LRN invalid chars and too long) only show invalid char error" in {
        val form = boundedForm(copyDeclarationLrnBadAndTooLongJSON)

        form.hasErrors mustBe true
        form.errors.length mustBe 1
        form.errors.head.key mustBe "lrn"
        form.errors.head.message mustBe "declaration.consignmentReferences.lrn.error.specialCharacter"
      }
    }
  }

  "LrnValidatorHelper.verifyLrnValidity" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    "return form without errors" when {
      "LrnValidator returns false" in {
        val lrnValidator = mock[LrnValidator]
        when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(false))

        val form = boundedForm(copyDeclarationJSON)

        val result = form.verifyLrnValidity(lrnValidator).futureValue

        result.hasErrors mustBe false
      }
    }

    "return form with errors" when {
      "LrnValidator returns true" in {
        val lrnValidator = mock[LrnValidator]
        when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(true))

        val form = boundedForm(copyDeclarationJSON)

        val result = form.verifyLrnValidity(lrnValidator).futureValue

        result.hasErrors mustBe true
        result.errors.length mustBe 1
        result.errors.head.key mustBe "lrn"
        result.errors.head.message mustBe "declaration.consignmentReferences.lrn.error.notExpiredYet"
      }
    }
  }

  "CopyDeclaration" when {
    testTariffContentKeys(CopyDeclaration, "tariff.declaration.consignmentReferences")
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"), TariffContentKey(s"${messageKey}.3.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(
      TariffContentKey(s"${messageKey}.1.clearance"),
      TariffContentKey(s"${messageKey}.2.clearance"),
      TariffContentKey(s"${messageKey}.3.clearance")
    )

  override def getSupplementaryTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(
      TariffContentKey(s"${messageKey}.1.supplementary"),
      TariffContentKey(s"${messageKey}.1.common"),
      TariffContentKey(s"${messageKey}.2.common"),
      TariffContentKey(s"${messageKey}.3.common")
    )
}

object CopyDeclarationSpec {

  val emptyJSON = Json.obj("" -> "")

  val copyDeclarationJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr), "lrn" -> lrn)

  val copyDeclarationLrnWithUntrimmmedSpacesJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr), "lrn" -> s" $lrn ")
  val copyDeclarationLrnWithSpacesJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr), "lrn" -> lrnWithSpaces)
  val copyDeclarationLowercaseDucrJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr.toLowerCase), "lrn" -> lrn)

  val copyDeclarationNoDucrJSON = Json.obj("ducr" -> Json.obj("ducr" -> ""), "lrn" -> lrn)
  val copyDeclarationNoLrnJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr), "lrn" -> "")
  val copyDeclarationBadLrnJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr), "lrn" -> s"${lrn}*")
  val copyDeclarationLrnTooLongJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr), "lrn" -> "12345678901234567890123")
  val copyDeclarationLrnBadAndTooLongJSON = Json.obj("ducr" -> Json.obj("ducr" -> ducr), "lrn" -> "1234567890123456789012*")
}
