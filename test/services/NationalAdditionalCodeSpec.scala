package services

import uk.gov.hmrc.play.test.UnitSpec

class NationalAdditionalCodeSpec extends UnitSpec {

  "National Additional Code" should {
    "read from file" in {
      val codes = NationalAdditionalCode.all
      codes should contain(NationalAdditionalCode("VATE")) // First in file
      codes should contain(NationalAdditionalCode("X99D")) // Last in file
    }

    "exclude header" in {
      val codes = NationalAdditionalCode.all
      codes shouldNot contain(NationalAdditionalCode("Code"))
    }

    "sort by value" in {
      val codes = NationalAdditionalCode.all.filter(code => Set("VATE", "X99D").contains(code.value))
      codes shouldBe List(NationalAdditionalCode("VATE"), NationalAdditionalCode("X99D"))
    }
  }

}
