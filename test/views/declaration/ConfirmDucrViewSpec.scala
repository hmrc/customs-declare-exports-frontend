package views.declaration

import base.Injector
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.confirm_ducr
import views.tags.ViewTest

@ViewTest
abstract class ConfirmDucrViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[confirm_ducr]

  "Confirm DUCR view" should {

    "display title" in {

    }

    "display row with DUCR" in {

    }

    "display body text" in {

    }

    "display radio input for Yes/No" in {

    }

    "display expander 1" in {

    }

    "display expander 2" in {

    }
  }
}
