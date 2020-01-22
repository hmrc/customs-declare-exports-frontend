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

import forms.declaration.{DeclarationAdditionalActors, DeclarationHolder}
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.additional_and_authorised_parties_section

class AdditionalAndAuthorisedPartiesSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val eori1 = "eori1"
  val partyType1 = "CS"
  val eori2 = "eori2"
  val partyType2 = "MF"
  val authorisationTypeCode1 = "partyType1"
  val authorisationTypeCode2 = "partyType2"

  val additionalActors = Seq(DeclarationAdditionalActors(Some(eori1), Some(partyType1)), DeclarationAdditionalActors(Some(eori2), Some(partyType2)))

  val holders = Seq(DeclarationHolder(Some(authorisationTypeCode1), Some(eori1)), DeclarationHolder(Some(authorisationTypeCode2), Some(eori2)))

  "Additional and authorised parties section" should {

    "display additional actors with answer no if empty" in {

      val view = additional_and_authorised_parties_section(Mode.Normal, Seq.empty, Seq.empty)(messages, journeyRequest())

      view.getElementById("additionalActors-label").text() mustBe messages("declaration.summary.parties.additional")
      view.getElementById("additionalActors").text() mustBe messages("site.no")
    }

    "display holders with answer no if empty" in {

      val view = additional_and_authorised_parties_section(Mode.Normal, Seq.empty, Seq.empty)(messages, journeyRequest())

      view.getElementById("holders-label").text() mustBe messages("declaration.summary.parties.holders")
      view.getElementById("holders").text() mustBe messages("site.no")
    }

    "display additional actors if exists" in {

      val view = additional_and_authorised_parties_section(Mode.Normal, additionalActors, Seq.empty)(messages, journeyRequest())

      view.getElementById("additionalActors").text() mustBe messages("declaration.summary.parties.additional")
      view.getElementById("additionalActors-type").text() mustBe messages("declaration.summary.parties.additional.type")
      view.getElementById("additionalActors-eori").text() mustBe messages("declaration.summary.parties.additional.eori")
      view.getElementById("additionalActor-type-0").text() mustBe messages("declaration.summary.parties.additional.CS")
      view.getElementById("additionalActor-eori-0").text() mustBe messages(eori1)
      view.getElementById("additionalActor-type-1").text() mustBe messages("declaration.summary.parties.additional.MF")
      view.getElementById("additionalActor-eori-1").text() mustBe messages(eori2)
    }

    "display holders if exists" in {

      val view = additional_and_authorised_parties_section(Mode.Normal, Seq.empty, holders)(messages, journeyRequest())

      view.getElementById("holders").text() mustBe messages("declaration.summary.parties.holders")
      view.getElementById("holders-type").text() mustBe messages("declaration.summary.parties.holders.type")
      view.getElementById("holders-eori").text() mustBe messages("declaration.summary.parties.holders.eori")
      view.getElementById("holder-type-0").text() mustBe messages(authorisationTypeCode1)
      view.getElementById("holder-eori-0").text() mustBe messages(eori1)
      view.getElementById("holder-type-1").text() mustBe messages(authorisationTypeCode2)
      view.getElementById("holder-eori-1").text() mustBe messages(eori2)
    }

    "provide change button if there is no additional actors and holders" in {

      val view = additional_and_authorised_parties_section(Mode.Normal, Seq.empty, Seq.empty)(messages, journeyRequest())

      val List(changeActor, accessibleChangeActor) = view.getElementById("additionalActors-change").text().split(" ").toList

      changeActor mustBe messages("site.change")
      accessibleChangeActor mustBe messages("declaration.summary.parties.additional.empty.change")

      view.getElementById("additionalActors-change") must haveHref(controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage())

      val List(changeHolder, accessibleChangeHolder) = view.getElementById("holders-change").text().split(" ").toList

      changeHolder mustBe messages("site.change")
      accessibleChangeHolder mustBe messages("declaration.summary.parties.holders.empty.change")

      view.getElementById("holders-change") must haveHref(controllers.declaration.routes.DeclarationHolderController.displayPage())
    }

    "provide change button for every actor" in {

      val view = additional_and_authorised_parties_section(Mode.Normal, additionalActors, Seq.empty)(messages, journeyRequest())

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

    "provide change button for every holder" in {

      val view = additional_and_authorised_parties_section(Mode.Normal, Seq.empty, holders)(messages, journeyRequest())

      val List(change1, accessibleChange1) = view.getElementById("holder-0-change").text().split(" ").toList

      change1 mustBe messages("site.change")
      accessibleChange1 mustBe messages("declaration.summary.parties.holders.change", authorisationTypeCode1, eori1)

      view.getElementById("holder-0-change") must haveHref(controllers.declaration.routes.DeclarationHolderController.displayPage())

      val List(change2, accessibleChange2) = view.getElementById("holder-1-change").text().split(" ").toList

      change2 mustBe messages("site.change")
      accessibleChange2 mustBe messages("declaration.summary.parties.holders.change", authorisationTypeCode2, eori2)

      view.getElementById("holder-1-change") must haveHref(controllers.declaration.routes.DeclarationHolderController.displayPage())
    }
  }
}
