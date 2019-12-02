/*
 * Copyright 2019 HM Revenue & Customs
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
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.additional_and_authorised_parties_section

class AdditionalAndAuthorisedPartiesSectionViewSpec extends UnitViewSpec with ExportsTestData {

  "Additional and authorised parties section" should {

    "display additional actors with answer no if empty" in {

      val view = additional_and_authorised_parties_section(Seq.empty, Seq.empty)(messages, journeyRequest())

      view.getElementById("additionalActors-label").text() mustBe messages("declaration.summary.parties.additional")
      view.getElementById("additionalActors").text() mustBe messages("site.no")
    }

    "display holders with answer no if empty" in {

      val view = additional_and_authorised_parties_section(Seq.empty, Seq.empty)(messages, journeyRequest())

      view.getElementById("holders-label").text() mustBe messages("declaration.summary.parties.holders")
      view.getElementById("holders").text() mustBe messages("site.no")
    }

    "display additional actors if exists" in {

      val eori1 = "eori1"
      val partyType1 = "CS"
      val eori2 = "eori2"
      val partyType2 = "MF"

      val additionalActors =
        Seq(DeclarationAdditionalActors(Some(eori1), Some(partyType1)), DeclarationAdditionalActors(Some(eori2), Some(partyType2)))

      val view = additional_and_authorised_parties_section(additionalActors, Seq.empty)(messages, journeyRequest())

      view.getElementById("additionalActors").text() mustBe messages("declaration.summary.parties.additional")
      view.getElementById("additionalActors-type").text() mustBe messages("declaration.summary.parties.additional.type")
      view.getElementById("additionalActors-eori").text() mustBe messages("declaration.summary.parties.additional.eori")
      view.getElementById("additionalActor-type-0").text() mustBe messages("declaration.summary.parties.additional.CS")
      view.getElementById("additionalActor-eori-0").text() mustBe messages(eori1)
      view.getElementById("additionalActor-type-1").text() mustBe messages("declaration.summary.parties.additional.MF")
      view.getElementById("additionalActor-eori-1").text() mustBe messages(eori2)
    }

    "display holders if exists" in {

      val eori1 = "eori1"
      val authorisationTypeCode1 = "partyType1"
      val eori2 = "eori2"
      val authorisationTypeCode2 = "partyType2"

      val holders = Seq(DeclarationHolder(Some(authorisationTypeCode1), Some(eori1)), DeclarationHolder(Some(authorisationTypeCode2), Some(eori2)))

      val view = additional_and_authorised_parties_section(Seq.empty, holders)(messages, journeyRequest())

      view.getElementById("holders").text() mustBe messages("declaration.summary.parties.holders")
      view.getElementById("holders-type").text() mustBe messages("declaration.summary.parties.holders.type")
      view.getElementById("holders-eori").text() mustBe messages("declaration.summary.parties.holders.eori")
      view.getElementById("holder-type-0").text() mustBe messages(authorisationTypeCode1)
      view.getElementById("holder-eori-0").text() mustBe messages(eori1)
      view.getElementById("holder-type-1").text() mustBe messages(authorisationTypeCode2)
      view.getElementById("holder-eori-1").text() mustBe messages(eori2)
    }
  }
}
