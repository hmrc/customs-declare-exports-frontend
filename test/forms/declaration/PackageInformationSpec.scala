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

import base.CustomExportsBaseSpec
import forms.FormMatchers
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import services.PackageTypes

class PackageInformationSpec
    extends CustomExportsBaseSpec with MustMatchers with PropertyChecks with Generators with FormMatchers {

  "packagingInformationMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { packaging: PackageInformation =>
          Form(PackageInformation.mapping)
            .fillAndValidate(packaging)
            .fold(_ => fail("form should not fail"), success => success mustBe packaging)
        }
      }
    }

    "fail" when {

      "marksNumbersId is longer than 42 characters" in {

        forAll(arbitrary[PackageInformation], minStringLength(43)) { (packaging, id) =>
          val data = packaging.copy(shippingMarks = Some(id))
          Form(PackageInformation.mapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Shipping marks can only be up to 42 characters"),
              _ => fail("should not succeed")
            )
        }
      }

      "numberOfPackages is larger than 99999" in {

        forAll(arbitrary[PackageInformation], intGreaterThan(99999)) { (packaging, quantity) =>
          val data = packaging.copy(numberOfPackages = Some(quantity))
          Form(PackageInformation.mapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Number of packages must be greater than 0 and less than 99999"),
              _ => fail("should not succeed")
            )
        }
      }

      "numberOfPackages is less than or equal to 0" in {

        forAll(arbitrary[PackageInformation], intLessThan(1)) { (packaging, quantity) =>
          val data = packaging.copy(numberOfPackages = Some(quantity))
          Form(PackageInformation.mapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Number of packages must be greater than 0 and less than 99999"),
              _ => fail("should not succeed")
            )
        }
      }

      "typesOfPackages not in list" in {
        val permitted = PackageTypes.all.map(_.code).toSet
        forAll(arbitrary[PackageInformation], nonEmptyString) { (packaging, pkgTypeCode) =>
          whenever(!permitted.contains(pkgTypeCode)) {
            val data = packaging.copy(typesOfPackages = Some(pkgTypeCode))
            Form(PackageInformation.mapping)
              .fillAndValidate(data)
              .fold(
                _ must haveErrorMessage("Type of package should be a 2 character code"),
                _ => fail("should not succeed")
              )
          }
        }
      }

      "Shipping Marks, Number of Packages or Type for package not supplied" in {
        Form(PackageInformation.mapping)
          .bind(Map.empty[String, String])
          .fold(
            _ must haveErrorMessage(
              "You must provide 6/9 item packaged, 6/10 Shipping Marks, 6/11 Number of Packages  for a package to be added"
            ),
            _ => fail("should not succeed")
          )
      }
    }
  }
}
