package views.declaration

import base.Injector
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.trader_reference
import views.tags.ViewTest

@ViewTest
abstract class TraderReferenceViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[trader_reference]

  "Trader Reference view" should {

    "display title" in {

    }

    "display body text" in {

    }

    "display hint text" in {

    }

    "display input for trader reference" in {

    }

    "display expander" in {

    }

    "display error message for incorrect form entry" in {

    }

    "display error message for no form entry" in {

    }
  }
}
