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
import org.scalatest.{MustMatchers, WordSpec}

class PackageInformationSpec extends WordSpec with MustMatchers {

  val form = PackageInformation.form

  "Package Information" should {

    "has correct form id" in {

      PackageInformation.formId must be("PackageInformation")
    }

    "has limit equal to 99" in {

      PackageInformation.limit must be(99)
    }

    "has correct type of package text" in {
      val model = PackageInformation("PK", 10, "marks")

      model.typesOfPackagesText mustBe "Package - PK"
    }
  }

  "Package Information form" should {

    "has no errors" when {

      "correct data is provided" in {

        val correctForm = PackageInformation("1D", 123, "correct")

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }
    }

    "has errors" when {

      "inputs are empty" in {

        val incorrectForm = Map("typesOfPackages" -> "", "numberOfPackages" -> "", "shippingMarks" -> "")

        val result = form.bind(incorrectForm)
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

        val result = form.bind(incorrectForm)
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
    }
  }
}
