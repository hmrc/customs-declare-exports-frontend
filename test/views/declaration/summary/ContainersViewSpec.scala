/*
 * Copyright 2023 HM Revenue & Customs
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

import base.Injector
import controllers.declaration.routes.TransportContainerController
import forms.declaration.Seal
import models.declaration.Container
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.containers

class ContainersViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  val firstContainerID = "951357"
  val secondContainerID = "456789"
  val firstSeal = "1254"
  val secondSeal = "98745"
  val containerWithoutSeals = Container(1, firstContainerID, Seq.empty)
  val containerWithSeals = Container(2, secondContainerID, Seq(Seal(1, firstSeal), Seal(2, secondSeal)))
  val containers = Seq(containerWithoutSeals, containerWithSeals)

  val section = instanceOf[containers]

  "Containers" should {

    "display title only and change link" when {

      "Containers is empty" in {
        val view = section(Seq.empty)(messages)
        val row = view.getElementsByClass("containers-row")

        row must haveSummaryKey(messages("declaration.summary.transport.containers"))
        row must haveSummaryValue(messages("site.no"))

        row must haveSummaryActionsTexts("site.change", "declaration.summary.transport.containers.change")
        row must haveSummaryActionWithPlaceholder(TransportContainerController.displayContainerSummary)
      }
    }

    "display all containers and seals" in {
      val view = section(containers)(messages)

      val id1 = view.getElementById("container-1").getElementsByClass("govuk-summary-list__key")
      val seal1 = view.getElementById("container-1").getElementsByClass("govuk-summary-list__value")
      val link1 = view.getElementById("container-1").getElementsByClass("govuk-summary-list__actions").first()

      id1.get(0).text() mustBe messages("declaration.summary.container.id")
      id1.get(1).text() mustBe messages("declaration.summary.container.securitySeals")
      seal1.get(0).text() mustBe firstContainerID
      seal1.get(1).text() mustBe messages("declaration.summary.container.securitySeals.none")
      link1 must containMessage("site.change")
      link1.getElementsByTag("a").first() must haveHrefWithPlaceholder(TransportContainerController.displayContainerSummary)
      link1 must containMessage("declaration.summary.container.change", firstContainerID)

      val id2 = view.getElementById("container-2").getElementsByClass("govuk-summary-list__key")
      val seal2 = view.getElementById("container-2").getElementsByClass("govuk-summary-list__value")
      val link2 = view.getElementById("container-2").getElementsByClass("govuk-summary-list__actions").first()

      id2.get(0).text() mustBe messages("declaration.summary.container.id")
      id2.get(1).text() mustBe messages("declaration.summary.container.securitySeals")
      seal2.get(0).text() mustBe secondContainerID
      seal2.get(1).text() mustBe s"$firstSeal, $secondSeal"
      link2 must containMessage("site.change")
      link2.getElementsByTag("a").first() must haveHrefWithPlaceholder(TransportContainerController.displayContainerSummary)
      link2 must containMessage("declaration.summary.container.change", secondContainerID)

    }
  }
}
