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

import base.Injector
import forms.declaration.Seal
import models.Mode
import models.declaration.Container
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.containers

class ContainersViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val firstContainerID = "951357"
  val secondContainerID = "456789"
  val firstSeal = "1254"
  val secondSeal = "98745"
  val containerWithoutSeals = Container(firstContainerID, Seq.empty)
  val containerWithSeals = Container(secondContainerID, Seq(Seal(firstSeal), Seal(secondSeal)))
  val containers = Seq(containerWithoutSeals, containerWithSeals)

  val section = instanceOf[containers]

  "Containers" should {

    "display title only and change link" when {

      "Containers is empty" in {

        val view = section(Mode.Normal, Seq.empty)(messages, journeyRequest())
        val row = view.getElementsByClass("containers-row")

        row must haveSummaryKey(messages("declaration.summary.transport.containers"))
        row must haveSummaryValue(messages("site.no"))

        row must haveSummaryActionsText("site.change declaration.summary.transport.containers.change")

        row must haveSummaryActionsHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal))
      }
    }

    "display all containers and seals" in {
      val view = section(Mode.Change, containers)(messages, journeyRequest())

      val table = view.getElementById("containers-table")
      table.getElementsByTag("caption").text() mustBe messages("declaration.summary.container")
      table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.container.id")
      table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.container.securitySeals")

      val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
      row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe messages(firstContainerID)
      row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe messages("")

      val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row1ChangeLink must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Change))
      row1ChangeLink.text() mustBe "site.change" + messages("declaration.summary.container.change", 0)

      val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
      row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe messages(secondContainerID)
      row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe messages(s"$firstSeal, $secondSeal")

      val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
      row2ChangeLink must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Change))
      row2ChangeLink.text() mustBe "site.change" + messages("declaration.summary.container.change", 1)
    }
  }
}
