package unit.models

import models.Mode.{Amend, Change, ChangeAmend, Draft, Normal}
import unit.base.UnitSpec

class ModeSpec extends UnitSpec {

  "Normal mode" must {
    "be same after submitting form" in {
      Normal.next mustBe Normal
    }
  }

  "Change mode" must {
    "become Normal after submitting form" in {
      Change.next mustBe Normal
    }
  }

  "Amend mode" must {
    "be same after submitting form" in {
      Amend.next mustBe Amend
    }
  }

  "Change-Amend" must {
    "become Amend after submitting form" in {
      ChangeAmend.next mustBe Amend
    }
  }

  "Draft" must {
    "be same after submitting page" in {
      Draft.next mustBe Draft
    }
  }

}
