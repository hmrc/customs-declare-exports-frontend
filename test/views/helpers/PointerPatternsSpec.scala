package views.helpers

import models.Pointer
import services.cache.ExportsTestHelper
import views.common.UnitViewSpec

class PointerPatternsSpec extends UnitViewSpec with ExportsTestHelper {

  val additionalActorsPointer = "declaration.parties.additionalActors.actors.$"

  "PointerPatterns" can {
    "expand single parent pointers into a sequence of their child pointers, so" should {
      s"expand $additionalActorsPointer into its child elements" in {
        PointerPatterns.expandPointer(Pointer(additionalActorsPointer), aDeclaration(), aDeclaration())
      }
    }
  }

}
