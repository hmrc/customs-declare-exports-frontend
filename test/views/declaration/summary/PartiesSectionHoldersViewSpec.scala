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
import forms.declaration.DeclarationHolder
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section_holders

class PartiesSectionHoldersViewSpec extends UnitViewSpec with ExportsTestData {

  val eori1 = "eori1"
  val eori2 = "eori2"
  val authorisationTypeCode1 = "partyType1"
  val authorisationTypeCode2 = "partyType2"

  val holders =
    Seq(DeclarationHolder(Some(authorisationTypeCode1), Some(Eori(eori1))), DeclarationHolder(Some(authorisationTypeCode2), Some(Eori(eori2))))

  "Holders (authorised) parties section" should {

    "display holders with answer no if empty" in {

      val view = parties_section_holders(Mode.Normal, Seq.empty)(messages, journeyRequest())

      view.getElementById("holders-label").text() mustBe messages("declaration.summary.parties.holders")
      view.getElementById("holders").text() mustBe messages("site.no")
    }

    "display holders if exists" in {

      val view = parties_section_holders(Mode.Normal, holders)(messages, journeyRequest())

      view.getElementById("holders").text() mustBe messages("declaration.summary.parties.holders")
      view.getElementById("holders-type").text() mustBe messages("declaration.summary.parties.holders.type")
      view.getElementById("holders-eori").text() mustBe messages("declaration.summary.parties.holders.eori")
      view.getElementById("holder-type-0").text() mustBe messages(authorisationTypeCode1)
      view.getElementById("holder-eori-0").text() mustBe messages(eori1)
      view.getElementById("holder-type-1").text() mustBe messages(authorisationTypeCode2)
      view.getElementById("holder-eori-1").text() mustBe messages(eori2)
    }

    "provide change button if there is no holders" in {

      val view = parties_section_holders(Mode.Normal, Seq.empty)(messages, journeyRequest())

      val List(changeHolder, accessibleChangeHolder) = view.getElementById("holders-change").text().split(" ").toList

      changeHolder mustBe messages("site.change")
      accessibleChangeHolder mustBe messages("declaration.summary.parties.holders.empty.change")

      view.getElementById("holders-change") must haveHref(controllers.declaration.routes.DeclarationHolderController.displayPage())
    }

    "provide change button for every holder" in {

      val view = parties_section_holders(Mode.Normal, holders)(messages, journeyRequest())

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
