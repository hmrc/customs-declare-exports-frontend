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

import forms.declaration.Seal
import models.declaration.Container
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary

class ContainersViewSpec extends UnitViewSpec with ExportsTestData {

  val firstContainerID = "951357"
  val secondContainerID = "456789"
  val firstSeal = "1254"
  val secondSeal = "98745"
  val containerWithoutSeals = Container(firstContainerID, Seq.empty)
  val containerWithSeals = Container(secondContainerID, Seq(Seal(firstSeal), Seal(secondSeal)))
  val containers = Seq(containerWithoutSeals, containerWithSeals)

  "Containers" should {

    "display all containers and seals" in {

      val view = summary.containers(containers)(messages, journeyRequest())

      view.getElementById("container").text() mustBe messages("declaration.summary.container")
      view.getElementById("container-id").text() mustBe messages("declaration.summary.container.id")
      view.getElementById("container-seals").text() mustBe messages("declaration.summary.container.securitySeals")
      view.getElementById("container-0-id").text() mustBe firstContainerID
      view.getElementById("container-0-seals").text() mustBe empty
      view.getElementById("container-1-id").text() mustBe secondContainerID
      view.getElementById("container-1-seals").text() mustBe s"$firstSeal, $secondSeal"
    }

    "display change buttons for every container" in {

      val view = summary.containers(containers)(messages, journeyRequest())

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
