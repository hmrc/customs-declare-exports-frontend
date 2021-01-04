/*
 * Copyright 2021 HM Revenue & Customs
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
import unit.base.{JourneyTypeTestRunner, UnitSpec}

class PackageInformationSpec extends UnitSpec with JourneyTypeTestRunner {

  private def formAllFieldsMandatory = PackageInformation.form()

  "Package Information" should {

    "has correct form id" in {

      PackageInformation.formId must be("PackageInformation")
    }

    "has limit equal to 99" in {

      PackageInformation.limit must be(99)
    }

    "has correct type of package text" in {
      val model = PackageInformation("id", Some("PK"), Some(10), Some("marks"))

      model.typesOfPackagesText mustBe Some("Package (PK)")
    }
  }

  "Package Information form" when {

    onEveryDeclarationJourney() { request =>
      "return form with mappingAllFieldsMandatory" in {

        val form = PackageInformation.form()
        form.mapping mustBe PackageInformation.mapping
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

      "number of packages is 0" in {

        val correctForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "0", "shippingMarks" -> "correct")

        val result = formAllFieldsMandatory.bind(correctForm)

        result.errors must be(empty)
      }

      "number of packages is 99999" in {

        val correctForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "99999", "shippingMarks" -> "correct")

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
          List(
            "declaration.packageInformation.typesOfPackages.empty",
            "declaration.packageInformation.numberOfPackages.error",
            "declaration.packageInformation.shippingMarks.empty"
          )
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
            "declaration.packageInformation.typesOfPackages.error",
            "declaration.packageInformation.numberOfPackages.error",
            "declaration.packageInformation.shippingMarks.lengthError"
          )
        )
      }

      "number of packages is -1" in {

        val incorrectForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "-1", "shippingMarks" -> "correct")

        val result = formAllFieldsMandatory.bind(incorrectForm)

        val errorMessages = result.errors.map(_.message)
        errorMessages mustBe List("declaration.packageInformation.numberOfPackages.error")
      }

      "number of packages is 100000" in {

        val incorrectForm = Map("typesOfPackages" -> "ID", "numberOfPackages" -> "100000", "shippingMarks" -> "correct")

        val result = formAllFieldsMandatory.bind(incorrectForm)

        val errorMessages = result.errors.map(_.message)
        errorMessages mustBe List("declaration.packageInformation.numberOfPackages.error")
      }
    }
  }

}
