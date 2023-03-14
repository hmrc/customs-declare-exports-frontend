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

package forms.declaration

import base.UnitSpec
import forms.common.{Address, Eori, YesNoAnswer}
import forms.declaration.carrier.CarrierDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.declarationHolder.DeclarationHolder
import forms.declaration.exporter.ExporterDetails
import models.declaration.{Container, DeclarationAdditionalActorsData, DeclarationHoldersData, EoriSource, Parties, RepresentativeDetails}
import services.AlteredField
import services.AlteredField.constructAlteredField

class PartiesSpec extends UnitSpec {
  "Address.createDiff" should {
    val baseFieldPointer = Address.pointer
    val originalValue = "original"

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")
        address.createDiff(address, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's fullName field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.fullNamePointer}"
        val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")
        address.createDiff(address.copy(fullName = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.fullName)
        )
      }

      "the original version's addressLine field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.addressLinePointer}"
        val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")
        address.createDiff(address.copy(addressLine = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.addressLine)
        )
      }

      "the original version's townOrCity field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.townOrCityPointer}"
        val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")
        address.createDiff(address.copy(townOrCity = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.townOrCity)
        )
      }

      "the original version's postCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.postCodePointer}"
        val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")
        address.createDiff(address.copy(postCode = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.postCode)
        )
      }

      "the original version's country field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.countryPointer}"
        val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")
        address.createDiff(address.copy(country = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.country)
        )
      }
    }
  }

  val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")

  "EntityDetails.createDiff" should {
    val baseFieldPointer = EntityDetails.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("both values are None") {
          val details = EntityDetails(None, None)
          details.createDiff(details, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }

        withClue("eori is Some, address is None") {
          val details = EntityDetails(Some(Eori("latestEori")), None)
          details.createDiff(details, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }

        withClue("eori is None, address is Some") {
          val details = EntityDetails(None, Some(address))
          details.createDiff(details, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }

        withClue("eori is Some, address is Some") {
          val details = EntityDetails(Some(Eori("latestEori")), Some(address))
          details.createDiff(details, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }
      }

      "the original version's eori field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${EntityDetails.eoriPointer}"

        withClue("both versions have Some values but values are different") {
          val details = EntityDetails(Some(Eori("latestEori")), Some(address))
          val originalValue = Eori("original")
          details.createDiff(details.copy(eori = Some(originalValue)), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, details.eori.get)
          )
        }

        withClue("the original version's eori field is None but this one has Some value") {
          val details = EntityDetails(Some(Eori("latestEori")), Some(address))
          val originalValue = None
          details.createDiff(details.copy(eori = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, details.eori)
          )
        }

        withClue("the original version's eori field is Some but this one has None as its value") {
          val details = EntityDetails(None, Some(address))
          val originalValue = Some(Eori("original"))
          details.createDiff(details.copy(eori = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, details.eori)
          )
        }
      }

      "the original version's address field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.pointer}"

        withClue("both versions have Some values but values are different") {
          val details = EntityDetails(Some(Eori("latestEori")), Some(address))
          val originalValue = address.copy(fullName = "original")
          details.createDiff(details.copy(address = Some(originalValue)), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"$fieldPointer.${Address.fullNamePointer}", originalValue.fullName, details.address.get.fullName)
          )
        }

        withClue("the original version's address field is None but this one has Some value") {
          val details = EntityDetails(Some(Eori("latestEori")), Some(address))
          val originalValue = None
          details.createDiff(details.copy(address = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, details.address)
          )
        }

        withClue("the original version's eori field is Some but this one has None as its value") {
          val details = EntityDetails(Some(Eori("latestEori")), None)
          val originalValue = Some(address)
          details.createDiff(details.copy(address = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, details.address)
          )
        }
      }
    }
  }

  "Parties.createDiff" should {
    val baseFieldPointer = Parties.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val parties = Parties()
        parties.createDiff(parties, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      val parties = Parties()
      "the original version's exporterDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${ExporterDetails.pointer}"
        val originalValue = Some(ExporterDetails(EntityDetails(None, None)))
        parties.createDiff(parties.copy(exporterDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.exporterDetails)
        )
      }

      "the original version's consigneeDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${ConsigneeDetails.pointer}"
        val originalValue = Some(ConsigneeDetails(EntityDetails(None, None)))
        parties.createDiff(parties.copy(consigneeDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.consigneeDetails)
        )
      }

      "the original version's consignorDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${ConsignorDetails.pointer}"
        val originalValue = Some(ConsignorDetails(EntityDetails(None, None)))
        parties.createDiff(parties.copy(consignorDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.consignorDetails)
        )
      }

      "the original version's declarantDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${DeclarantDetails.pointer}"
        val originalValue = Some(DeclarantDetails(EntityDetails(None, None)))
        parties.createDiff(parties.copy(declarantDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.declarantDetails)
        )
      }

      "the original version's representativeDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${RepresentativeDetails.pointer}"
        val originalValue = Some(RepresentativeDetails(None, None, Some("other")))
        parties.createDiff(parties.copy(representativeDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.representativeDetails)
        )
      }

      "the original version's declarationAdditionalActorsData field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${DeclarationAdditionalActors.pointer}"
        val originalValue = Some(DeclarationAdditionalActorsData(Seq.empty))
        parties.createDiff(parties.copy(declarationAdditionalActorsData = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.declarationAdditionalActorsData)
        )
      }

      "the original version's carrierDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${CarrierDetails.pointer}"
        val originalValue = Some(CarrierDetails(EntityDetails(None, None)))
        parties.createDiff(parties.copy(carrierDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.carrierDetails)
        )
      }

      "the original version's personPresentingGoodsDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${PersonPresentingGoodsDetails.pointer}"
        val originalValue = Some(PersonPresentingGoodsDetails(Eori("original")))
        parties.createDiff(parties.copy(personPresentingGoodsDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.personPresentingGoodsDetails)
        )
      }
    }
  }

  "ExporterDetails.createDiff" should {
    val baseFieldPointer = ExporterDetails.pointer
    val details = EntityDetails(None, None)

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val exporterDetails = ExporterDetails(details)
        exporterDetails.createDiff(exporterDetails, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's eori field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${EntityDetails.pointer}.${EntityDetails.eoriPointer}"
        val exporterDetails = ExporterDetails(details)
        val originalValue = details.copy(eori = Some(Eori("original")))
        exporterDetails.createDiff(exporterDetails.copy(details = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.eori, details.eori)
        )
      }
    }
  }

  "RepresentativeDetails.createDiff" should {
    val baseFieldPointer = RepresentativeDetails.pointer
    val details = EntityDetails(None, None)

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val representativeDetails = RepresentativeDetails(None, None, None)
        representativeDetails.createDiff(representativeDetails, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's details field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${RepresentativeDetails.detailsPointer}"
        val representativeDetails = RepresentativeDetails(None, None, None)
        val originalValue = Some(details)
        representativeDetails.createDiff(representativeDetails.copy(details = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, representativeDetails.details)
        )
      }

      "the original version's statusCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${RepresentativeDetails.statusCodePointer}"
        val representativeDetails = RepresentativeDetails(None, None, None)
        val originalValue = Some("original")
        representativeDetails.createDiff(representativeDetails.copy(statusCode = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, representativeDetails.statusCode)
        )
      }
    }
  }

  "DeclarationAdditionalActor.createDiff" should {
    val baseFieldPointer = DeclarationAdditionalActors.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val declarationAdditionalActor = DeclarationAdditionalActors(None, None)
        declarationAdditionalActor.createDiff(declarationAdditionalActor, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's eori field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${DeclarationAdditionalActors.eoriPointer}"
        val declarationAdditionalActor = DeclarationAdditionalActors(Some(Eori("latestEori")), Some("latestPartyType"))
        val originalValue = Some(Eori("original"))
        declarationAdditionalActor.createDiff(declarationAdditionalActor.copy(eori = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, declarationAdditionalActor.eori)
        )
      }

      "the original version's partyType field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${DeclarationAdditionalActors.partyTypePointer}"
        val declarationAdditionalActor = DeclarationAdditionalActors(Some(Eori("latestEori")), Some("latestPartyType"))
        val originalValue = Some("original")
        declarationAdditionalActor.createDiff(declarationAdditionalActor.copy(partyType = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, declarationAdditionalActor.partyType)
        )
      }
    }
  }

  "DeclarationAdditionalActorsData.createDiff" should {
    val baseFieldPointer = DeclarationAdditionalActors.pointer
    val actors = Seq(
      DeclarationAdditionalActors(Some(Eori("latestEoriOne")), Some("latestPartyTypeOne")),
      DeclarationAdditionalActors(Some(Eori("latestEoriTwo")), Some("latestPartyTypeTwo")),
      DeclarationAdditionalActors(Some(Eori("latestEoriThree")), Some("latestPartyTypeThree"))
    )

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("when no actors are present") {
          val decActors = DeclarationAdditionalActorsData(Seq.empty)
          decActors.createDiff(decActors, baseFieldPointer, Some(1)) mustBe Seq.empty[AlteredField]
        }

        withClue("when actors are present") {
          val decActors = DeclarationAdditionalActorsData(actors)
          decActors.createDiff(decActors, baseFieldPointer, Some(1)) mustBe Seq.empty[AlteredField]
        }
      }

      "when actors are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${DeclarationAdditionalActorsData.pointer}"
        withClue("original container's actors are not present") {
          val decActors = DeclarationAdditionalActorsData(actors)
          decActors.createDiff(decActors.copy(actors = Seq.empty), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}", None, Some(actors(0))),
            constructAlteredField(s"${fieldPointer}", None, Some(actors(1))),
            constructAlteredField(s"${fieldPointer}", None, Some(actors(2)))
          )
        }

        withClue("this container's seals are not present") {
          val decActors = DeclarationAdditionalActorsData(Seq.empty)
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}", Some(actors(0)), None),
            constructAlteredField(s"${fieldPointer}", Some(actors(1)), None),
            constructAlteredField(s"${fieldPointer}", Some(actors(2)), None)
          )
        }

        withClue("both container seals contain different number of elements") {
          val decActors = DeclarationAdditionalActorsData(actors.drop(1))
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.eori", actors(0).eori, actors(1).eori),
            constructAlteredField(s"${fieldPointer}.partyType", actors(0).partyType, actors(1).partyType),
            constructAlteredField(s"${fieldPointer}.eori", actors(1).eori, actors(2).eori),
            constructAlteredField(s"${fieldPointer}.partyType", actors(1).partyType, actors(2).partyType),
            constructAlteredField(s"${fieldPointer}", Some(actors(2)), None)
          )
        }

        withClue("both container seals contain same elements but in different order") {
          val decActors = DeclarationAdditionalActorsData(actors.reverse)
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.eori", actors(0).eori, actors(2).eori),
            constructAlteredField(s"${fieldPointer}.partyType", actors(0).partyType, actors(2).partyType),
            constructAlteredField(s"${fieldPointer}.eori", actors(2).eori, actors(0).eori),
            constructAlteredField(s"${fieldPointer}.partyType", actors(2).partyType, actors(0).partyType)
          )
        }

        withClue("container seals contain elements with different values") {
          val otherVal = "other"
          val decActors = DeclarationAdditionalActorsData(Seq(DeclarationAdditionalActors(Some(Eori(otherVal)), Some(otherVal))) ++ actors.drop(1))
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.eori", actors(0).eori, Some(Eori(otherVal))),
            constructAlteredField(s"${fieldPointer}.partyType", actors(0).partyType, Some(otherVal))
          )
        }
      }
    }
  }

  "DeclarationHolder.createDiff" should {
    val baseFieldPointer = DeclarationHolder.pointer
    val originalValue = Some("original")

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val declarationHolder = DeclarationHolder(None, None, None)
        declarationHolder.createDiff(declarationHolder, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's authorisationTypeCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${DeclarationHolder.authorisationTypeCodePointer}"
        val declarationHolder = DeclarationHolder(Some("latestAuthorisationTypeCode"), Some(Eori("latestEori")), Some(EoriSource.UserEori))
        declarationHolder.createDiff(declarationHolder.copy(authorisationTypeCode = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, declarationHolder.authorisationTypeCode)
        )
      }

      "the original version's eori field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${DeclarationHolder.eoriPointer}"
        val declarationHolder = DeclarationHolder(Some("latestAuthorisationTypeCode"), Some(Eori("latestEori")), Some(EoriSource.UserEori))
        declarationHolder.createDiff(declarationHolder.copy(eori = originalValue.map(Eori(_))), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.map(Eori(_)), declarationHolder.eori)
        )
      }
    }
  }

  "DeclarationHolders.createDiff" should {
    val baseFieldPointer = s"${DeclarationHoldersData.pointer}"

    val holders = Seq(
      DeclarationHolder(Some("authorisationTypeCodeOne"), Some(Eori("eoriOne")), Some(EoriSource.UserEori)),
      DeclarationHolder(Some("authorisationTypeCodeTwo"), Some(Eori("eoriTwo")), Some(EoriSource.OtherEori)),
      DeclarationHolder(Some("authorisationTypeCodeThree"), Some(Eori("eoriThree")), Some(EoriSource.UserEori))
    )

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("when no holders are present") {
          val declarationHolders = DeclarationHoldersData(Seq.empty, None)
          declarationHolders.createDiff(declarationHolders, Container.pointer, Some(1)) mustBe Seq.empty[AlteredField]
        }

        withClue("when holders are present") {
          val declarationHolders = DeclarationHoldersData(holders, YesNoAnswer.Yes)
          declarationHolders.createDiff(declarationHolders, Container.pointer, Some(1)) mustBe Seq.empty[AlteredField]
        }
      }

      "when holders are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${DeclarationHolder.pointer}"
        withClue("original DeclarationHolder's holders are not present") {
          val declarationHolders = DeclarationHoldersData(holders, YesNoAnswer.Yes)
          declarationHolders.createDiff(declarationHolders.copy(holders = Seq.empty), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.1", None, Some(holders(0))),
            constructAlteredField(s"${fieldPointer}.2", None, Some(holders(1))),
            constructAlteredField(s"${fieldPointer}.3", None, Some(holders(2)))
          )
        }

        withClue("this DeclarationHolder's holders are not present") {
          val declarationHolders = DeclarationHoldersData(Seq.empty, YesNoAnswer.Yes)
          declarationHolders.createDiff(declarationHolders.copy(holders = holders), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.1", Some(holders(0)), None),
            constructAlteredField(s"${fieldPointer}.2", Some(holders(1)), None),
            constructAlteredField(s"${fieldPointer}.3", Some(holders(2)), None)
          )
        }

        withClue("both DeclarationHolder's holders contain different number of elements") {
          val declarationHolders = DeclarationHoldersData(holders.drop(1), YesNoAnswer.Yes)
          declarationHolders.createDiff(declarationHolders.copy(holders = holders), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.1.authorisationTypeCode", holders(0).authorisationTypeCode, holders(1).authorisationTypeCode),
            constructAlteredField(s"${fieldPointer}.1.eori", holders(0).eori, holders(1).eori),
            constructAlteredField(s"${fieldPointer}.2.authorisationTypeCode", holders(1).authorisationTypeCode, holders(2).authorisationTypeCode),
            constructAlteredField(s"${fieldPointer}.2.eori", holders(1).eori, holders(2).eori),
            constructAlteredField(s"${fieldPointer}.3", Some(holders(2)), None)
          )
        }

        withClue("both DeclarationHolder's holders contain same elements but in different order") {
          val declarationHolders = DeclarationHoldersData(holders, YesNoAnswer.Yes)
          declarationHolders.createDiff(declarationHolders.copy(holders = holders.reverse), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.1.authorisationTypeCode", holders(2).authorisationTypeCode, holders(0).authorisationTypeCode),
            constructAlteredField(s"${fieldPointer}.1.eori", holders(2).eori, holders(0).eori),
            constructAlteredField(s"${fieldPointer}.3.authorisationTypeCode", holders(0).authorisationTypeCode, holders(2).authorisationTypeCode),
            constructAlteredField(s"${fieldPointer}.3.eori", holders(0).eori, holders(2).eori)
          )
        }

        withClue("DeclarationHolder's holders contain elements with different values") {
          val otherHolder = DeclarationHolder(Some("authorisationTypeCodeOther"), Some(Eori("eoriOther")), Some(EoriSource.UserEori))
          val declarationHolders = DeclarationHoldersData(Seq(otherHolder) ++ holders.drop(1), YesNoAnswer.Yes)
          declarationHolders.createDiff(declarationHolders.copy(holders = holders), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.1.authorisationTypeCode", holders(0).authorisationTypeCode, otherHolder.authorisationTypeCode),
            constructAlteredField(s"${fieldPointer}.1.eori", holders(0).eori, otherHolder.eori)
          )
        }
      }
    }
  }

  "PersonPresentingGoodsDetails.createDiff" should {
    val baseFieldPointer = PersonPresentingGoodsDetails.pointer
    val originalValue = Eori("original")

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val personPresenting = PersonPresentingGoodsDetails(Eori("latest"))
        personPresenting.createDiff(personPresenting, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's personPresentingGoodsDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Eori.pointer}"
        val personPresenting = PersonPresentingGoodsDetails(Eori("latest"))
        personPresenting.createDiff(personPresenting.copy(eori = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, personPresenting.eori)
        )
      }
    }
  }
}