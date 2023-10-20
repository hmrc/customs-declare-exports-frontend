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
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.exporter.ExporterDetails
import models.declaration.{AdditionalActors, AuthorisationHolders, Container, EoriSource, Parties, RepresentativeDetails}
import models.declaration.EoriSource.UserEori
import services.AlteredField
import services.AlteredField.constructAlteredField

class PartiesSpec extends UnitSpec {
  val address = Address("latestFullName", "latestAddressLine", "latestTownOrCity", "latestPostCode", "latestCountry")
  val otherAddress = address.copy(fullName = "originalFullName")

  "Address.createDiff" should {
    val baseFieldPointer = Address.pointer
    val originalValue = "original"

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        address.createDiff(address, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's fullName field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.fullNamePointer}"
        address.createDiff(address.copy(fullName = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.fullName)
        )
      }

      "the original version's addressLine field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.addressLinePointer}"

        address.createDiff(address.copy(addressLine = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.addressLine)
        )
      }

      "the original version's townOrCity field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.townOrCityPointer}"
        address.createDiff(address.copy(townOrCity = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.townOrCity)
        )
      }

      "the original version's postCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.postCodePointer}"
        address.createDiff(address.copy(postCode = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.postCode)
        )
      }

      "the original version's country field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${Address.countryPointer}"
        address.createDiff(address.copy(country = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, address.country)
        )
      }
    }
  }

  "EntityDetails.createDiff" should {
    val baseFieldPointer = "details"

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

      "the original version's consigneeDetails field has a different value to this one" when {
        "one value is none" in {
          val fieldPointer = s"${baseFieldPointer}.${ConsigneeDetails.pointer}"
          val originalValue = Some(ConsigneeDetails(EntityDetails(None, None)))
          parties.createDiff(parties.copy(consigneeDetails = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, parties.consigneeDetails)
          )
        }

        "both have some value" in {
          val parties = Parties().copy(consigneeDetails = Some(ConsigneeDetails(EntityDetails(None, Some(otherAddress)))))
          val fieldPointer = s"${baseFieldPointer}.${ConsigneeDetails.pointer}.${Address.pointer}.${Address.fullNamePointer}"
          val originalValue = Some(ConsigneeDetails(EntityDetails(None, Some(address))))
          parties.createDiff(parties.copy(consigneeDetails = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(
              fieldPointer,
              originalValue.flatMap(_.details.address.map(_.fullName)),
              parties.consigneeDetails.flatMap(_.details.address.map(_.fullName))
            )
          )
        }
      }

      "the original version's consignorDetails field has a different value to this one" when {
        "one value is none" in {
          val fieldPointer = s"${baseFieldPointer}.${ConsignorDetails.pointer}"
          val originalValue = Some(ConsignorDetails(EntityDetails(None, None)))
          parties.createDiff(parties.copy(consignorDetails = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, parties.consignorDetails)
          )
        }

        "both have some value" in {
          val parties = Parties().copy(consignorDetails = Some(ConsignorDetails(EntityDetails(None, Some(otherAddress)))))
          val fieldPointer = s"${baseFieldPointer}.${ConsignorDetails.pointer}.${Address.pointer}.${Address.fullNamePointer}"
          val originalValue = Some(ConsignorDetails(EntityDetails(None, Some(address))))
          parties.createDiff(parties.copy(consignorDetails = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(
              fieldPointer,
              originalValue.flatMap(_.details.address.map(_.fullName)),
              parties.consignorDetails.flatMap(_.details.address.map(_.fullName))
            )
          )
        }
      }

      "the original version's declarantDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${DeclarantDetails.pointer}"
        val originalValue = Some(DeclarantDetails(EntityDetails(None, None)))
        parties.createDiff(parties.copy(declarantDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.declarantDetails)
        )
      }

      "the original version's declarationAdditionalActorsData field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${AdditionalActors.pointer}.${AdditionalActor.pointer}.#1"
        val originalValue = Some(AdditionalActors(Seq(AdditionalActor(Some(Eori("1234")), None))))
        parties.createDiff(parties.copy(declarationAdditionalActorsData = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, Some(originalValue.value.actors.head), None)
        )
      }

      "the original version's representativeDetails field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${RepresentativeDetails.pointer}"
        val originalValue = Some(RepresentativeDetails(None, None, Some("other")))
        parties.createDiff(parties.copy(representativeDetails = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, parties.representativeDetails)
        )
      }

      "the original version's declarationHoldersData field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${AuthorisationHolders.pointer}.${AuthorisationHolder.pointer}.#1"
        val originalValue = Some(AuthorisationHolders(Seq(AuthorisationHolder(Some("TypeCode"), Some(Eori("1234")), Some(UserEori))), None))
        parties.createDiff(parties.copy(declarationHoldersData = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, Some(originalValue.value.holders.head), None)
        )
      }

      "the original version's carrierDetails field has a different value to this one" when {
        "one value is none" in {
          val fieldPointer = s"${baseFieldPointer}.${CarrierDetails.pointer}"
          val originalValue = Some(CarrierDetails(EntityDetails(None, None)))
          parties.createDiff(parties.copy(carrierDetails = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, parties.carrierDetails)
          )
        }

        "both have some value" in {
          val parties = Parties().copy(carrierDetails = Some(CarrierDetails(EntityDetails(None, Some(otherAddress)))))
          val fieldPointer = s"${baseFieldPointer}.${CarrierDetails.pointer}.${Address.pointer}.${Address.fullNamePointer}"
          val originalValue = Some(CarrierDetails(EntityDetails(None, Some(address))))
          parties.createDiff(parties.copy(carrierDetails = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(
              fieldPointer,
              originalValue.flatMap(_.details.address.map(_.fullName)),
              parties.carrierDetails.flatMap(_.details.address.map(_.fullName))
            )
          )
        }
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
        val fieldPointer = s"${baseFieldPointer}.${EntityDetails.eoriPointer}"
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
        val fieldPointer = s"${baseFieldPointer}"
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
    val baseFieldPointer = AdditionalActor.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val declarationAdditionalActor = AdditionalActor(None, None)
        declarationAdditionalActor.createDiff(declarationAdditionalActor, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's eori field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${AdditionalActor.eoriPointer}"
        val declarationAdditionalActor = AdditionalActor(Some(Eori("latestEori")), Some("latestPartyType"))
        val originalValue = Some(Eori("original"))
        declarationAdditionalActor.createDiff(declarationAdditionalActor.copy(eori = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, declarationAdditionalActor.eori)
        )
      }

      "the original version's partyType field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${AdditionalActor.partyTypePointer}"
        val declarationAdditionalActor = AdditionalActor(Some(Eori("latestEori")), Some("latestPartyType"))
        val originalValue = Some("original")
        declarationAdditionalActor.createDiff(declarationAdditionalActor.copy(partyType = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, declarationAdditionalActor.partyType)
        )
      }
    }
  }

  "DeclarationAdditionalActorsData.createDiff" should {
    val baseFieldPointer = AdditionalActors.pointer
    val actors = Seq(
      AdditionalActor(Some(Eori("latestEoriOne")), Some("latestPartyTypeOne")),
      AdditionalActor(Some(Eori("latestEoriTwo")), Some("latestPartyTypeTwo")),
      AdditionalActor(Some(Eori("latestEoriThree")), Some("latestPartyTypeThree"))
    )

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("when no actors are present") {
          val decActors = AdditionalActors(Seq.empty)
          decActors.createDiff(decActors, baseFieldPointer, Some(1)) mustBe Seq.empty[AlteredField]
        }

        withClue("when actors are present") {
          val decActors = AdditionalActors(actors)
          decActors.createDiff(decActors, baseFieldPointer, Some(1)) mustBe Seq.empty[AlteredField]
        }
      }

      "when actors are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${AdditionalActor.pointer}"
        withClue("original container's actors are not present") {
          val decActors = AdditionalActors(actors)
          decActors.createDiff(decActors.copy(actors = Seq.empty), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(actors(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(actors(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(actors(2)))
          )
        }

        withClue("this container's seals are not present") {
          val decActors = AdditionalActors(Seq.empty)
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(actors(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(actors(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(actors(2)), None)
          )
        }

        withClue("both container seals contain different number of elements") {
          val decActors = AdditionalActors(actors.drop(1))
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1.${AdditionalActor.eoriPointer}", actors(0).eori, actors(1).eori),
            constructAlteredField(s"${fieldPointer}.#1.${AdditionalActor.partyTypePointer}", actors(0).partyType, actors(1).partyType),
            constructAlteredField(s"${fieldPointer}.#2.${AdditionalActor.eoriPointer}", actors(1).eori, actors(2).eori),
            constructAlteredField(s"${fieldPointer}.#2.${AdditionalActor.partyTypePointer}", actors(1).partyType, actors(2).partyType),
            constructAlteredField(s"${fieldPointer}.#3", Some(actors(2)), None)
          )
        }

        withClue("both container seals contain same elements but in different order") {
          val decActors = AdditionalActors(actors.reverse)
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1.${AdditionalActor.eoriPointer}", actors(0).eori, actors(2).eori),
            constructAlteredField(s"${fieldPointer}.#1.${AdditionalActor.partyTypePointer}", actors(0).partyType, actors(2).partyType),
            constructAlteredField(s"${fieldPointer}.#3.${AdditionalActor.eoriPointer}", actors(2).eori, actors(0).eori),
            constructAlteredField(s"${fieldPointer}.#3.${AdditionalActor.partyTypePointer}", actors(2).partyType, actors(0).partyType)
          )
        }

        withClue("container seals contain elements with different values") {
          val otherVal = "other"
          val decActors = AdditionalActors(Seq(AdditionalActor(Some(Eori(otherVal)), Some(otherVal))) ++ actors.drop(1))
          decActors.createDiff(decActors.copy(actors = actors), baseFieldPointer) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1.${AdditionalActor.eoriPointer}", actors(0).eori, Some(Eori(otherVal))),
            constructAlteredField(s"${fieldPointer}.#1.${AdditionalActor.partyTypePointer}", actors(0).partyType, Some(otherVal))
          )
        }
      }
    }
  }

  "AuthorisationHolder.createDiff" should {
    val baseFieldPointer = AuthorisationHolder.pointer
    val originalValue = Some("original")

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        val authorisationHolder = AuthorisationHolder(None, None, None)
        authorisationHolder.createDiff(authorisationHolder, baseFieldPointer) mustBe Seq.empty[AlteredField]
      }

      "the original version's authorisationTypeCode field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${AuthorisationHolder.authorisationTypeCodePointer}"
        val authorisationHolder = AuthorisationHolder(Some("latestAuthorisationTypeCode"), Some(Eori("latestEori")), Some(EoriSource.UserEori))
        authorisationHolder.createDiff(authorisationHolder.copy(authorisationTypeCode = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, authorisationHolder.authorisationTypeCode)
        )
      }

      "the original version's eori field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${AuthorisationHolder.eoriPointer}"
        val authorisationHolder = AuthorisationHolder(Some("latestAuthorisationTypeCode"), Some(Eori("latestEori")), Some(EoriSource.UserEori))
        authorisationHolder.createDiff(authorisationHolder.copy(eori = originalValue.map(Eori(_))), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue.map(Eori(_)), authorisationHolder.eori)
        )
      }
    }
  }

  "AuthorisationHolders.createDiff" should {
    val baseFieldPointer = s"${AuthorisationHolders.pointer}"

    val holders = Seq(
      AuthorisationHolder(Some("authorisationTypeCodeOne"), Some(Eori("eoriOne")), Some(EoriSource.UserEori)),
      AuthorisationHolder(Some("authorisationTypeCodeTwo"), Some(Eori("eoriTwo")), Some(EoriSource.OtherEori)),
      AuthorisationHolder(Some("authorisationTypeCodeThree"), Some(Eori("eoriThree")), Some(EoriSource.UserEori))
    )

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("when no holders are present") {
          val authorisationHolders = AuthorisationHolders(Seq.empty, None)
          authorisationHolders.createDiff(authorisationHolders, Container.pointer, Some(1)) mustBe Seq.empty[AlteredField]
        }

        withClue("when holders are present") {
          val authorisationHolders = AuthorisationHolders(holders, YesNoAnswer.Yes)
          authorisationHolders.createDiff(authorisationHolders, Container.pointer, Some(1)) mustBe Seq.empty[AlteredField]
        }
      }

      "when holders are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.${AuthorisationHolder.pointer}"
        withClue("original AuthorisationHolder's holders are not present") {
          val authorisationHolders = AuthorisationHolders(holders, YesNoAnswer.Yes)
          authorisationHolders.createDiff(authorisationHolders.copy(holders = Seq.empty), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(holders(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(holders(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(holders(2)))
          )
        }

        withClue("this AuthorisationHolder's holders are not present") {
          val authorisationHolders = AuthorisationHolders(Seq.empty, YesNoAnswer.Yes)
          authorisationHolders.createDiff(authorisationHolders.copy(holders = holders), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(holders(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(holders(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(holders(2)), None)
          )
        }

        withClue("both AuthorisationHolder's holders contain different number of elements") {
          val authorisationHolders = AuthorisationHolders(holders.drop(1), YesNoAnswer.Yes)
          authorisationHolders.createDiff(authorisationHolders.copy(holders = holders), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(
              s"${fieldPointer}.#1.${AuthorisationHolder.authorisationTypeCodePointer}",
              holders(0).authorisationTypeCode,
              holders(1).authorisationTypeCode
            ),
            constructAlteredField(s"${fieldPointer}.#1.${AuthorisationHolder.eoriPointer}", holders(0).eori, holders(1).eori),
            constructAlteredField(
              s"${fieldPointer}.#2.${AuthorisationHolder.authorisationTypeCodePointer}",
              holders(1).authorisationTypeCode,
              holders(2).authorisationTypeCode
            ),
            constructAlteredField(s"${fieldPointer}.#2.${AuthorisationHolder.eoriPointer}", holders(1).eori, holders(2).eori),
            constructAlteredField(s"${fieldPointer}.#3", Some(holders(2)), None)
          )
        }

        withClue("both AuthorisationHolder's holders contain same elements but in different order") {
          val authorisationHolders = AuthorisationHolders(holders, YesNoAnswer.Yes)
          authorisationHolders.createDiff(authorisationHolders.copy(holders = holders.reverse), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(
              s"${fieldPointer}.#1.${AuthorisationHolder.authorisationTypeCodePointer}",
              holders(2).authorisationTypeCode,
              holders(0).authorisationTypeCode
            ),
            constructAlteredField(s"${fieldPointer}.#1.${AuthorisationHolder.eoriPointer}", holders(2).eori, holders(0).eori),
            constructAlteredField(
              s"${fieldPointer}.#3.${AuthorisationHolder.authorisationTypeCodePointer}",
              holders(0).authorisationTypeCode,
              holders(2).authorisationTypeCode
            ),
            constructAlteredField(s"${fieldPointer}.#3.${AuthorisationHolder.eoriPointer}", holders(0).eori, holders(2).eori)
          )
        }

        withClue("AuthorisationHolder's holders contain elements with different values") {
          val otherHolder = AuthorisationHolder(Some("authorisationTypeCodeOther"), Some(Eori("eoriOther")), Some(EoriSource.UserEori))
          val authorisationHolders = AuthorisationHolders(Seq(otherHolder) ++ holders.drop(1), YesNoAnswer.Yes)
          authorisationHolders.createDiff(authorisationHolders.copy(holders = holders), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(
              s"${fieldPointer}.#1.${AuthorisationHolder.authorisationTypeCodePointer}",
              holders(0).authorisationTypeCode,
              otherHolder.authorisationTypeCode
            ),
            constructAlteredField(s"${fieldPointer}.#1.${AuthorisationHolder.eoriPointer}", holders(0).eori, otherHolder.eori)
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
