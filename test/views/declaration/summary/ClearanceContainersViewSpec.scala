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

package views.declaration.summary

import models.Mode
import models.declaration.Container
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary

class ClearanceContainersViewSpec extends UnitViewSpec with ExportsTestData {

  val firstContainerID = "951357"
  val secondContainerID = "456789"
  val containerOne = Container(firstContainerID, Seq.empty)
  val containerTwo = Container(secondContainerID, Seq.empty)
  val containers = Seq(containerOne, containerTwo)

  "Containers" should {

    "display all containers and seals" in {

      val view = summary.clearance_containers(Mode.Normal, containers)(messages, journeyRequest())

      view.getElementById("container").text() mustBe messages("declaration.summary.container")
      view.getElementById("container-id").text() mustBe messages("declaration.summary.container.id")
      view.getElementById("container-0-id").text() mustBe firstContainerID
      view.getElementById("container-1-id").text() mustBe secondContainerID
    }

    "display change buttons for every container" in {

      val view = summary.clearance_containers(Mode.Normal, containers)(messages, journeyRequest())

      val List(change1, accessibleChange1) = view.getElementById("container-0-change").text().split(" ").toList

      change1 mustBe messages("site.change")
      accessibleChange1 mustBe messages("declaration.summary.container.change", firstContainerID)

      view.getElementById("container-0-change") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())

      val List(change2, accessibleChange2) = view.getElementById("container-1-change").text().split(" ").toList

      change2 mustBe messages("site.change")
      accessibleChange2 mustBe messages("declaration.summary.container.change", secondContainerID)

      view.getElementById("container-1-change") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary())
    }
  }
}
