/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.forms.declaration

import base.TestHelper
import forms.declaration.PackageInformation
import forms.declaration.PackageInformation.{mappingAllFieldsMandatory, mappingAllFieldsOptional}
import models.DeclarationType._
import play.api.data.Form
import views.declaration.spec.UnitViewSpec

class PackageInformationSpec extends UnitViewSpec {

  private def formAllFieldsMandatory = Form(mappingAllFieldsMandatory)
  private def formAllFieldsOptional = Form(mappingAllFieldsOptional)

  "Package Information" should {

    "has correct form id" in {

      PackageInformation.formId must be("PackageInformation")
    }

    "has limit equal to 99" in {

      PackageInformation.limit must be(99)
    }

    "has correct type of package text" in {
      val model = PackageInformation(Some("PK"), Some(10), Some("marks"))

      model.typesOfPackagesText mustBe Some("Package - PK")
    }
  }

  "Package Information form" should {

    "return form with mappingAllFieldsOptional" when {

      "provided with Clearance declaration type" in {

        val form = PackageInformation.form(CLEARANCE)

        form.mapping mustBe PackageInformation.mappingAllFieldsOptional
      }
    }

    "return form with mappingAllFieldsMandatory" when {

      onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
        "provided with Clearance declaration type" in {

          val form = PackageInformation.form(request.declarationType)

          form.mapping mustBe PackageInformation.mappingAllFieldsMandatory
        }
      }
    }
  }

  "Package Information form with mappingAllFieldsMandatory" should {

    "have no errors" when {

      "correct data is provided" in {

        val correctForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "123", "shippingMarks" -> "correct")

        val result = formAllFieldsMandatory.bind(correctForm)

        result.errors must be(empty)
      }
    }

    "have errors" when {

      "all inputs are empty" in {

        val incorrectForm = Map("typesOfPackages" -> "", "numberOfPackages" -> "", "shippingMarks" -> "")

        val result = formAllFieldsMandatory.bind(incorrectForm)
        val errorMessages = result.errors.map(_.message)

        errorMessages must be(
          List("supplementary.packageInformation.typesOfPackages.empty", "error.number", "supplementary.packageInformation.shippingMarks.empty")
        )
      }

      "inputs are incorrect" in {

        val incorrectForm = Map(
          "typesOfPackages" -> "incorrect Type",
          "numberOfPackages" -> "1000000",
          "shippingMarks" -> TestHelper.createRandomAlphanumericString(43)
        )

        val result = formAllFieldsMandatory.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("typesOfPackages", "numberOfPackages", "shippingMarks"))
        errorMessages must be(
          List(
            "supplementary.packageInformation.typesOfPackages.error",
            "supplementary.packageInformation.numberOfPackages.error",
            "supplementary.packageInformation.shippingMarks.lengthError"
          )
        )
      }

      "number of packages is 0" in {

        val incorrectForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "0", "shippingMarks" -> "correct")

        val result = formAllFieldsMandatory.bind(incorrectForm)

        val errorMessages = result.errors.map(_.message)
        errorMessages mustBe List("supplementary.packageInformation.numberOfPackages.error")
      }

      "number of packages is 1 mln" in {

        val incorrectForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "1000000", "shippingMarks" -> "correct")

        val result = formAllFieldsMandatory.bind(incorrectForm)

        val errorMessages = result.errors.map(_.message)
        errorMessages mustBe List("supplementary.packageInformation.numberOfPackages.error")
      }
    }
  }

  "Package Information form with mappingAllFieldsOptional" should {

    "have no errors" when {

      "correct data is provided" in {

        val correctForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "123", "shippingMarks" -> "correct")

        val result = formAllFieldsOptional.bind(correctForm)

        result.errors must be(empty)
      }

      "one field is empty" when {

        "it is typesOfPackages" in {

          val correctForm = Map("typesOfPackages" -> "", "numberOfPackages" -> "123", "shippingMarks" -> "correct")

          val result = formAllFieldsOptional.bind(correctForm)

          result.errors must be(empty)
        }

        "it is numberOfPackages" in {

          val correctForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "", "shippingMarks" -> "correct")

          val result = formAllFieldsOptional.bind(correctForm)

          result.errors must be(empty)
        }

        "it is shippingMarks" in {

          val correctForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "123", "shippingMarks" -> "")

          val result = formAllFieldsOptional.bind(correctForm)

          result.errors must be(empty)
        }
      }

      "only a single field is provided" when {

        "it is typesOfPackages" in {

          val correctForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "", "shippingMarks" -> "")

          val result = formAllFieldsOptional.bind(correctForm)

          result.errors must be(empty)
        }

        "it is numberOfPackages" in {

          val correctForm = Map("typesOfPackages" -> "", "numberOfPackages" -> "123", "shippingMarks" -> "")

          val result = formAllFieldsOptional.bind(correctForm)

          result.errors must be(empty)
        }

        "it is shippingMarks" in {

          val correctForm = Map("typesOfPackages" -> "", "numberOfPackages" -> "", "shippingMarks" -> "correct")

          val result = formAllFieldsOptional.bind(correctForm)

          result.errors must be(empty)
        }
      }
    }

    "have errors" when {

      "inputs are incorrect" in {

        val incorrectForm = Map(
          "typesOfPackages" -> "incorrect Type",
          "numberOfPackages" -> "1000000",
          "shippingMarks" -> TestHelper.createRandomAlphanumericString(43)
        )

        val result = formAllFieldsOptional.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("typesOfPackages", "numberOfPackages", "shippingMarks"))
        errorMessages must be(
          List(
            "supplementary.packageInformation.typesOfPackages.error",
            "supplementary.packageInformation.numberOfPackages.error",
            "supplementary.packageInformation.shippingMarks.lengthError"
          )
        )
      }

      "all inputs are empty" in {

        val incorrectForm = Map("typesOfPackages" -> "", "numberOfPackages" -> "", "shippingMarks" -> "")

        val result = formAllFieldsOptional.bind(incorrectForm)

        val errorMessages = result.errors.map(_.message)
        errorMessages must be(List("supplementary.packageInformation.empty"))
      }

      "number of packages is 0" in {

        val incorrectForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "0", "shippingMarks" -> "correct")

        val result = formAllFieldsOptional.bind(incorrectForm)

        val errorMessages = result.errors.map(_.message)
        errorMessages mustBe List("supplementary.packageInformation.numberOfPackages.error")
      }

      "number of packages is 1 mln" in {

        val incorrectForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "1000000", "shippingMarks" -> "correct")

        val result = formAllFieldsOptional.bind(incorrectForm)

        val errorMessages = result.errors.map(_.message)
        errorMessages mustBe List("supplementary.packageInformation.numberOfPackages.error")
      }
    }
  }

}
