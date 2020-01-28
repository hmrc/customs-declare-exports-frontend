package views.declaration

import unit.base.UnitSpec
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.total_package_quantity

class TotalPackageQuantity extends UnitViewSpec with Stubs {

  val template = new total_package_quantity(mainTemplate)

  "Total Package Quantity view" should {
    "render input for it" in {
      ???
    }
  }

}
