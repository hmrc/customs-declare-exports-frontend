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
