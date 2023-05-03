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

package services.model

import base.UnitSpec
import forms.declaration.{PackageInformation, Seal}
import forms.declaration.countries.Country
import models.ExportsDeclaration
import models.declaration.{Container, ExportItem, Locations, ProcedureCodesData, RoutingCountry, Transport}
import org.scalatest.GivenWhenThen
import services.{AlteredField, OriginalAndNewValues}
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}

class DiffToolsISpec extends UnitSpec with ExportsDeclarationBuilder with ExportsItemBuilder with GivenWhenThen {

  private val baseFieldPointer = ExportsDeclaration.pointer
  private val goodsItemQuantityFieldPointer = s"$baseFieldPointer.${ExportsDeclaration.goodsItemQuantityPointer}"
  private val destinationCountryPointer = s"$baseFieldPointer.${Locations.pointer}.${Locations.destinationCountryPointer}.${Country.pointer}"
  private def itemFieldPointer(itemSeqId: Int) = s"$baseFieldPointer.${ExportItem.pointer}.#$itemSeqId"
  private def procedureCodePointer(itemSeqId: Int) =
    s"$baseFieldPointer.${ExportItem.pointer}.#$itemSeqId.${ProcedureCodesData.pointer}.${ProcedureCodesData.procedureCodesPointer}"
  private def packageInfoPointer(seqId: Int, itemSeqId: Int = 1) = itemFieldPointer(itemSeqId) + s".${PackageInformation.pointer}.#$seqId"
  private def containerPointer(seqId: Int) = s"$baseFieldPointer.${Transport.pointer}.${Container.pointer}.#$seqId"
  private def sealPointer(seqId: Int, containerSeqId: Int = 1) = s"${containerPointer(containerSeqId)}.${Seal.pointer}.#$seqId"
  private def routingCountryPointer(seqId: Int) = s"$baseFieldPointer.${Locations.pointer}.${Locations.routingCountriesPointer}.#$seqId"

  "DiffTools" should {
    "produce the expected field pointers on item addition and removal" when {

      "an existing item is removed" in {
        And("another item has been modified")
        val originalItem1 = anItem(withSequenceId(1), withItemId("1"), withProcedureCodes(Some("1042")))
        val currentItem1 = anItem(withSequenceId(1), withItemId("1"), withProcedureCodes(Some("1044")))
        val originalItem2 = anItem(withSequenceId(2), withItemId("2"), withProcedureCodes(Some("9999")))

        And("the declaration has been modified elsewhere")
        val originalDeclaration = aDeclaration(withItems(originalItem1, originalItem2), withDestinationCountry(Country(Some("FR"))))
        val amendedDeclaration = aDeclaration(withItems(currentItem1), withDestinationCountry(Country(Some("DE"))))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(itemFieldPointer(2), OriginalAndNewValues(Some(originalItem2), None)),
          AlteredField(goodsItemQuantityFieldPointer, OriginalAndNewValues(Some(2), Some(1))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE"))),
          AlteredField(procedureCodePointer(1), OriginalAndNewValues(Some("1042"), Some("1044")))
        )
      }

      "a goods item is added" in {
        And("an existing item is modified")
        val originalItem1 = anItem(withSequenceId(1), withItemId("1"), withProcedureCodes(Some("1042")))
        val currentItem1 = anItem(withSequenceId(1), withItemId("1"), withProcedureCodes(Some("1044")))
        val currentItem2 = anItem(withSequenceId(2), withItemId("2"), withProcedureCodes(Some("9999")))

        And("the declaration is modified elsewhere")
        val originalDeclaration = aDeclaration(withItems(originalItem1), withDestinationCountry(Country(Some("FR"))))
        val amendedDeclaration = aDeclaration(withItems(currentItem1, currentItem2), withDestinationCountry(Country(Some("DE"))))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(itemFieldPointer(2), OriginalAndNewValues(None, Some(currentItem2))),
          AlteredField(goodsItemQuantityFieldPointer, OriginalAndNewValues(Some(1), Some(2))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE"))),
          AlteredField(procedureCodePointer(1), OriginalAndNewValues(Some("1042"), Some("1044")))
        )
      }

      "an item is removed and another added" in {
        And("an existing item is modified")
        val originalItem1 = anItem(withSequenceId(1), withItemId("1"), withProcedureCodes(Some("1042")))
        val originalItem2 = anItem(withSequenceId(2), withItemId("2"), withProcedureCodes(Some("9999")))
        val currentItem1 = anItem(withSequenceId(1), withItemId("1"), withProcedureCodes(Some("1044")))
        val currentItem3 = anItem(withSequenceId(3), withItemId("3"), withProcedureCodes(Some("1111")))

        And("the declaration is modified elsewhere")
        val originalDeclaration = aDeclaration(withItems(originalItem1, originalItem2), withDestinationCountry(Country(Some("FR"))))
        val amendedDeclaration = aDeclaration(withItems(currentItem1, currentItem3), withDestinationCountry(Country(Some("DE"))))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(itemFieldPointer(2), OriginalAndNewValues(Some(originalItem2), None)),
          AlteredField(itemFieldPointer(3), OriginalAndNewValues(None, Some(currentItem3))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE"))),
          AlteredField(procedureCodePointer(1), OriginalAndNewValues(Some("1042"), Some("1044")))
        )
      }
    }

    "produce the expected field pointers on PackageInformation addition and removal" when {

      "an existing Package Information is removed" in {
        And("another Package Information has been modified")
        val originalPackageInfo1 = PackageInformation(1, "1", None, None, None)
        val currentPackageInfo1 = PackageInformation(1, "1", Some("box"), None, None)
        val originalPackageInfo2 = PackageInformation(2, "2", None, None, None)

        And("the declaration has been modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1), withPackageInformation(originalPackageInfo1, originalPackageInfo2))),
          withDestinationCountry(Country(Some("FR")))
        )
        val amendedDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1), withPackageInformation(currentPackageInfo1))), withDestinationCountry(Country(Some("DE"))))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(packageInfoPointer(2), OriginalAndNewValues(Some(originalPackageInfo2), None)),
          AlteredField(s"${packageInfoPointer(1)}.${PackageInformation.typesOfPackagesPointer}", OriginalAndNewValues(None, Some("box"))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a Package Information is added" in {
        And("an existing Package Information is modified")
        val originalPackageInfo1 = PackageInformation(1, "1", None, None, None)
        val currentPackageInfo1 = PackageInformation(1, "1", Some("box"), None, None)
        val currentPackageInfo2 = PackageInformation(2, "2", None, None, None)

        And("the declaration is modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1), withPackageInformation(originalPackageInfo1))),
          withDestinationCountry(Country(Some("FR")))
        )
        val amendedDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1), withPackageInformation(currentPackageInfo1, currentPackageInfo2))),
          withDestinationCountry(Country(Some("DE")))
        )

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(packageInfoPointer(2), OriginalAndNewValues(None, Some(currentPackageInfo2))),
          AlteredField(s"${packageInfoPointer(1)}.${PackageInformation.typesOfPackagesPointer}", OriginalAndNewValues(None, Some("box"))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a Package Information is removed and another added" in {
        And("an existing Package Information is modified")
        val originalPackageInfo1 = PackageInformation(1, "1", None, None, None)
        val originalPackageInfo2 = PackageInformation(2, "2", None, None, None)
        val currentPackageInfo1 = PackageInformation(1, "1", Some("box"), None, None)
        val currentPackageInfo3 = PackageInformation(3, "3", None, None, None)

        And("the declaration is modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1), withPackageInformation(originalPackageInfo1, originalPackageInfo2))),
          withDestinationCountry(Country(Some("FR")))
        )
        val amendedDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1), withPackageInformation(currentPackageInfo1, currentPackageInfo3))),
          withDestinationCountry(Country(Some("DE")))
        )

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(packageInfoPointer(2), OriginalAndNewValues(Some(originalPackageInfo2), None)),
          AlteredField(packageInfoPointer(3), OriginalAndNewValues(None, Some(currentPackageInfo3))),
          AlteredField(s"${packageInfoPointer(1)}.${PackageInformation.typesOfPackagesPointer}", OriginalAndNewValues(None, Some("box"))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }
    }

    "produce the expected field pointers on Container addition and removal" when {

      "an existing Container is removed" in {
        And("an existing container is modified")
        val currentSeal1 = Seal(1, "1")
        val originalContainer1 = Container(1, "1", Seq())
        val originalContainer2 = Container(2, "2", Seq())
        val currentContainer = Container(1, "1", Seq(currentSeal1))

        And("the declaration has been modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("FR"))),
          withContainerData(originalContainer1, originalContainer2)
        )
        val amendedDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("DE"))), withContainerData(currentContainer))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(sealPointer(1), OriginalAndNewValues(None, Some(currentSeal1))),
          AlteredField(containerPointer(2), OriginalAndNewValues(Some(originalContainer2), None)),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a Container is added" in {
        And("an existing container is modified")
        val currentSeal1 = Seal(1, "1")
        val originalContainer1 = Container(1, "1", Seq())
        val currentContainer1 = Container(1, "1", Seq(currentSeal1))
        val currentContainer2 = Container(2, "2", Seq())

        And("the declaration has been modified elsewhere")
        val originalDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("FR"))), withContainerData(originalContainer1))
        val amendedDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("DE"))),
          withContainerData(currentContainer1, currentContainer2)
        )

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(sealPointer(1), OriginalAndNewValues(None, Some(currentSeal1))),
          AlteredField(containerPointer(2), OriginalAndNewValues(None, Some(currentContainer2))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a Container is removed and another added" in {
        And("an existing container is modified")
        val currentSeal1 = Seal(1, "1")
        val originalContainer1 = Container(1, "1", Seq())
        val originalContainer2 = Container(2, "2", Seq())
        val currentContainer1 = Container(1, "1", Seq(currentSeal1))
        val currentContainer3 = Container(3, "3", Seq())

        And("the declaration has been modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("FR"))),
          withContainerData(originalContainer1, originalContainer2)
        )
        val amendedDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("DE"))),
          withContainerData(currentContainer1, currentContainer3)
        )

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(sealPointer(1), OriginalAndNewValues(None, Some(currentSeal1))),
          AlteredField(containerPointer(2), OriginalAndNewValues(Some(originalContainer2), None)),
          AlteredField(containerPointer(3), OriginalAndNewValues(None, Some(currentContainer3))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }
    }

    "produce the expected field pointers on seal addition and removal" when {
      "an existing seal is removed" in {
        val originalSeal1 = Seal(1, "1")
        val originalSeal2 = Seal(2, "2")
        val originalContainer = Container(1, "1", Seq(originalSeal1, originalSeal2))
        val currentContainer = Container(1, "1", Seq(originalSeal1))

        And("the declaration has been modified elsewhere")
        val originalDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("FR"))), withContainerData(originalContainer))
        val amendedDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("DE"))), withContainerData(currentContainer))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(sealPointer(2), OriginalAndNewValues(Some(originalSeal2), None)),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a seal is added" in {
        val originalSeal1 = Seal(1, "1")
        val currentSeal2 = Seal(2, "2")
        val originalContainer = Container(1, "1", Seq(originalSeal1))
        val currentContainer = Container(1, "1", Seq(originalSeal1, currentSeal2))

        And("the declaration has been modified elsewhere")
        val originalDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("FR"))), withContainerData(originalContainer))
        val amendedDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("DE"))), withContainerData(currentContainer))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(sealPointer(2), OriginalAndNewValues(None, Some(currentSeal2))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a seal is removed and another added" in {
        val originalSeal1 = Seal(1, "1")
        val currentSeal2 = Seal(2, "2")
        val originalContainer = Container(1, "1", Seq(originalSeal1))
        val currentContainer = Container(1, "1", Seq(currentSeal2))

        And("the declaration has been modified elsewhere")
        val originalDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("FR"))), withContainerData(originalContainer))
        val amendedDeclaration =
          aDeclaration(withItems(anItem(withSequenceId(1))), withDestinationCountry(Country(Some("DE"))), withContainerData(currentContainer))

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(sealPointer(2), OriginalAndNewValues(None, Some(currentSeal2))),
          AlteredField(sealPointer(1), OriginalAndNewValues(Some(originalSeal1), None)),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }
    }

    "produce the expected field pointers on routing country addition and removal" when {
      "an existing routing country is removed" in {
        val originalRoutingCountry1 = RoutingCountry(1, Country(Some("1")))
        val originalRoutingCountry2 = RoutingCountry(2, Country(Some("2")))

        And("the declaration has been modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("FR"))),
          withRoutingCountries(Seq(originalRoutingCountry1.country, originalRoutingCountry2.country))
        )
        val amendedDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("DE"))),
          withRoutingCountries(Seq(originalRoutingCountry1.country))
        )

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(routingCountryPointer(2), OriginalAndNewValues(Some(originalRoutingCountry2), None)),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a routing country is added" in {
        val originalRoutingCountry1 = RoutingCountry(1, Country(Some("1")))
        val currentRoutingCountry2 = RoutingCountry(2, Country(Some("2")))

        And("the declaration has been modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("FR"))),
          withRoutingCountries(Seq(originalRoutingCountry1.country))
        )
        val amendedDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("DE"))),
          withRoutingCountries(Seq(originalRoutingCountry1.country, currentRoutingCountry2.country))
        )

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(routingCountryPointer(2), OriginalAndNewValues(None, Some(currentRoutingCountry2))),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }

      "a routing country is removed and another added" in {
        val originalRoutingCountry1 = RoutingCountry(1, Country(Some("1")))
        val currentRoutingCountry2 = RoutingCountry(2, Country(Some("2")))

        And("the declaration has been modified elsewhere")
        val originalDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("FR"))),
          withRoutingCountriesWithSeqId(Seq(originalRoutingCountry1))
        )
        val amendedDeclaration = aDeclaration(
          withItems(anItem(withSequenceId(1))),
          withDestinationCountry(Country(Some("DE"))),
          withRoutingCountriesWithSeqId(Seq(currentRoutingCountry2))
        )

        amendedDeclaration.createDiff(originalDeclaration) must contain theSameElementsAs Seq(
          AlteredField(routingCountryPointer(2), OriginalAndNewValues(None, Some(currentRoutingCountry2))),
          AlteredField(routingCountryPointer(1), OriginalAndNewValues(Some(originalRoutingCountry1), None)),
          AlteredField(destinationCountryPointer, OriginalAndNewValues(Some("FR"), Some("DE")))
        )
      }
    }
  }
}
