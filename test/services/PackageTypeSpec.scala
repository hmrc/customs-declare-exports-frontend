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

package services

import uk.gov.hmrc.play.test.UnitSpec

class PackageTypeSpec extends UnitSpec {

  "Package type list" should {
    "return package types containing commas and quotes" in {
      val somePackageTypes: List[PackageType] = PackageType.all.filter((packageType: PackageType) => packageType.code == "43")
      somePackageTypes shouldBe List(PackageType("43", "Bag, super bulk"))
    }

    "return package types' with codes in alphabetical order of name" in {
      val expectedCodes = Set("43", "AD", "ZZ")
      val somePackageTypes: List[PackageType] = PackageType.all.filter((packageType: PackageType) => expectedCodes.contains(packageType.code))
      somePackageTypes shouldBe List(
        PackageType("43", "Bag, super bulk"),
        PackageType("ZZ", "Defined mutually"),
        PackageType("AD", "Wooden receptacle")
      )
    }
  }
}
