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

package forms.section3

import base.{MessageSpec, TestHelper}
import connectors.CodeListConnector
import forms.common.DeclarationPageBaseSpec
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section3.LocationOfGoods.{gvmsGoodsLocationsForArrivedDecls, radioGroupId, userChoice}
import models.codes.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json

import scala.collection.immutable.ListMap

class LocationOfGoodsSpec extends DeclarationPageBaseSpec with BeforeAndAfterEach with MessageSpec with MockitoSugar {

  implicit val mockCodeListConnector: CodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private val validCode = "GBAUFXTFXTFXT"

  "LocationOfGoods for version 1, 6 or 7 of the page" should {

    def yesNoForm(yesNo: String, code: String): Form[LocationOfGoods] = {
      val json = Json.obj("yesNo" -> yesNo, "glc" -> "", "code" -> code)
      LocationOfGoods.form(version = 1).bind(json, Form.FromJsonMaxChars)
    }

    "return a form with errors" when {
      "provided with a Code" which {
        def boundedForm(code: String): Form[LocationOfGoods] = yesNoForm(YesNoAnswers.no, code)

        "is missing" in {
          val form = LocationOfGoods.form(version = 1).bind(Json.obj("unexpected" -> ""), Form.FromJsonMaxChars)

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "error.yesNo.required"
        }

        "is empty" in {
          val form = boundedForm("")

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.empty"
        }

        "is longer than 39 characters" in {
          val form = boundedForm(s"GBAU${TestHelper.createRandomAlphanumericString(40)}")

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error.length"
        }

        "is shorter than 10 characters" in {
          val form = boundedForm(s"GBAU")

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error.length"
        }

        "is alphanumeric" in {
          val form = boundedForm(s"${validCode}*")

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }

        "does not contain a valid country" in {
          val form = boundedForm(s"XX${validCode}")

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }

        "does not contain a valid location type" in {
          val form = boundedForm(s"GBX${validCode}")

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }

        "does not contain a valid qualifier code" in {
          val form = boundedForm(s"GBAX${validCode}")

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }
      }
    }

    "convert to upper case" in {
      val form = yesNoForm(YesNoAnswers.no, validCode.toLowerCase)
      form.value.map(_.code) mustBe Some(validCode)
    }

    "trim white spaces" in {
      val form = yesNoForm(YesNoAnswers.no, s"\n \t${validCode}\t \n")
      form.value.map(_.code) mustBe Some(validCode)
    }
  }

  "LocationOfGoods for version 3, or 5 of the page" should {

    def radioGroupForm(radioId: String, code: Option[String] = Some(validCode)): Form[LocationOfGoods] = {
      val json = Json.obj(radioGroupId -> radioId, userChoice -> (if (radioId == userChoice) code.fold("")(_.toLowerCase) else ""))
      LocationOfGoods.form(version = 3).bind(json, Form.FromJsonMaxChars)
    }

    "return the expected value" in {
      gvmsGoodsLocationsForArrivedDecls.foreach { radioId =>
        val form = radioGroupForm(radioId)
        form.value.map(_.code) mustBe Some(if (radioId == userChoice) validCode else radioId)
      }
    }

    "trim white spaces" when {
      "the 'Enter a code manually' field is provided with a Code containing spaces" in {
        val form = radioGroupForm(userChoice, Some(s"\n \t${validCode}\t \n"))
        form.value.map(_.code) mustBe Some(validCode)
      }
    }

    "return a form with errors" when {

      "the binding is illegal" in {
        val form = LocationOfGoods.form(version = 3).bind(Json.obj("unexpected" -> ""), Form.FromJsonMaxChars)

        form.hasErrors mustBe true
        form.errors.length mustBe 1
        form.errors.head.message mustBe "declaration.locationOfGoods.error.empty"
      }

      "nothing is selected" in {
        val form = radioGroupForm("")

        form.hasErrors mustBe true
        form.errors.length mustBe 1
        form.errors.head.message mustBe "declaration.locationOfGoods.error.empty"
      }

      "the 'Enter a code manually' field is provided with a Code" which {

        "is empty" in {
          val form = radioGroupForm(userChoice, Some(""))

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.empty"
        }

        "is longer than 39 characters" in {
          val form = radioGroupForm(userChoice, Some(s"GBAU${TestHelper.createRandomAlphanumericString(40)}"))

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error.length"
        }

        "is shorter than 10 characters" in {
          val form = radioGroupForm(userChoice, Some(s"GBAU"))

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error.length"
        }

        "is alphanumeric" in {
          val form = radioGroupForm(userChoice, Some(s"${validCode}*"))

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }

        "does not contain a valid country" in {
          val form = radioGroupForm(userChoice, Some(s"XX${validCode}"))

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }

        "does not contain a valid location type" in {
          val form = radioGroupForm(userChoice, Some(s"GBX${validCode}"))

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }

        "does not contain a valid qualifier code" in {
          val form = radioGroupForm(userChoice, Some(s"GBAX${validCode}"))

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.locationOfGoods.code.error"
        }
      }
    }
  }

  "LocationOfGoods" when {
    testTariffContentKeys(LocationOfGoods, "tariff.declaration.locationOfGoods")
  }
}
