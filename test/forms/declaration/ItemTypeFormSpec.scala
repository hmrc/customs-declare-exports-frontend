/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.declaration.ItemTypeForm._
import forms.declaration.ItemTypeFormSpec.correctItemTypeMap
import models.DeclarationType
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError

class ItemTypeFormSpec extends WordSpec with MustMatchers {

  "Item Type form" should {

    "return form without errors" when {
      "provided with valid values" in {
        val form = ItemTypeForm.form().bind(correctItemTypeMap)

        form.hasErrors must be(false)
      }
    }

    "return form with errors" when {

      "descriptionOfGoods is missing" in {
        val form = ItemTypeForm.form().bind(correctItemTypeMap - "descriptionOfGoods")

        form.errors mustBe Seq(FormError("descriptionOfGoods", "error.required"))
      }

      "statisticalValue is missing" in {
        val form = ItemTypeForm.form().bind(correctItemTypeMap - "statisticalValue")

        form.errors mustBe Seq(FormError("statisticalValue", "error.required"))
      }

    }
  }
}

object ItemTypeFormSpec {
  private val combinedNomenclatureCode = "ABCD1234"
  private val taricAdditionalCode = "AB12"
  private val nationalAdditionalCode = "VATE"
  private val descriptionOfGoods = "Description of goods."
  private val cusCode = "QWER0987"
  private val unDangerousGoodsCode = "12CD"
  private val statisticalValue = "1234567890123.45"

  val correctItemTypeMap: Map[String, String] =
    Map(
      combinedNomenclatureCodeKey -> combinedNomenclatureCode,
      taricAdditionalCodeKey -> taricAdditionalCode,
      nationalAdditionalCodeKey -> nationalAdditionalCode,
      descriptionOfGoodsKey -> descriptionOfGoods,
      cusCodeKey -> cusCode,
      statisticalValueKey -> statisticalValue,
      unDangerousGoodsCodeKey -> unDangerousGoodsCode
    )
}
