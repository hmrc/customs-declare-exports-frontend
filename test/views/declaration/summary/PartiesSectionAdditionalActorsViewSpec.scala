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

import forms.common.Eori
import forms.declaration.DeclarationAdditionalActors
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section_additional_actors

class PartiesSectionAdditionalActorsViewSpec extends UnitViewSpec with ExportsTestData {

  val eori1 = "eori1"
  val partyType1 = "CS"
  val eori2 = "eori2"
  val partyType2 = "MF"

  val additionalActors =
    Seq(DeclarationAdditionalActors(Some(Eori(eori1)), Some(partyType1)), DeclarationAdditionalActors(Some(Eori(eori2)), Some(partyType2)))

  "Additional actors parties section" should {

    "display additional actors with answer no if empty" in {

      val view = parties_section_additional_actors(Mode.Normal, Seq.empty)(messages, journeyRequest())

      view.getElementById("additionalActors-label").text() mustBe messages("declaration.summary.parties.additional")
      view.getElementById("additionalActors").text() mustBe messages("site.no")
    }

    "display additional actors if exists" in {

      val view = parties_section_additional_actors(Mode.Normal, additionalActors)(messages, journeyRequest())

      view.getElementById("additionalActors").text() mustBe messages("declaration.summary.parties.additional")
      view.getElementById("additionalActors-type").text() mustBe messages("declaration.additionalActors.partyType")
      view.getElementById("additionalActors-eori").text() mustBe messages("declaration.additionalActors.eori")
      view.getElementById("additionalActor-type-0").text() mustBe messages("declaration.partyType.CS")
      view.getElementById("additionalActor-eori-0").text() mustBe messages(eori1)
      view.getElementById("additionalActor-type-1").text() mustBe messages("declaration.partyType.MF")
      view.getElementById("additionalActor-eori-1").text() mustBe messages(eori2)
    }

    "provide change button if there is no additional actors" in {

      val view = parties_section_additional_actors(Mode.Normal, Seq.empty)(messages, journeyRequest())

      val List(changeActor, accessibleChangeActor) = view.getElementById("additionalActors-change").text().split(" ").toList

      changeActor mustBe messages("site.change")
      accessibleChangeActor mustBe messages("declaration.summary.parties.additional.empty.change")

      view.getElementById("additionalActors-change") must haveHref(controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage())

    }

    "provide change button for every actor" in {

      val view = parties_section_additional_actors(Mode.Normal, additionalActors)(messages, journeyRequest())

      val List(change1, accessibleChange1) = view.getElementById("additionalActor-0-change").text().split(" ").toList

      change1 mustBe messages("site.change")
      accessibleChange1 mustBe messages("declaration.summary.parties.additional.change", partyType1, eori1)

      view.getElementById("additionalActor-0-change") must haveHref(
        controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage()
      )

      val List(change2, accessibleChange2) = view.getElementById("additionalActor-1-change").text().split(" ").toList

      change2 mustBe messages("site.change")
      accessibleChange2 mustBe messages("declaration.summary.parties.additional.change", partyType2, eori2)

      view.getElementById("additionalActor-1-change") must haveHref(
        controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage()
      )
    }

  }
}
