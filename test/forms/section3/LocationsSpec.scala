/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section3

import base.UnitSpec
import forms.common.Country
import forms.section6.ModeOfTransportCode.{Maritime, Rail}
import forms.section6.{InlandModeOfTransportCode, SupervisingCustomsOffice, WarehouseIdentification}
import models.declaration.{GoodsLocation, Locations, RoutingCountry}
import services.AlteredField
import services.AlteredField.constructAlteredField

class LocationsSpec extends UnitSpec {

  "GoodsLocation.createDiff" should {
    val baseFieldPointer = GoodsLocation.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("all values are None") {
          val goodsLocation =
            GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
          goodsLocation.createDiff(goodsLocation, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }
      }

      "the original version's country field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val goodsLocation =
          GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
        val originalValue = goodsLocation.copy(country = "original")
        goodsLocation.createDiff(originalValue, baseFieldPointer) mustBe Seq(constructAlteredField(fieldPointer, originalValue, goodsLocation))
      }

      "the original version's typeOfLocation field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val goodsLocation =
          GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
        val originalValue = goodsLocation.copy(typeOfLocation = "original")
        goodsLocation.createDiff(originalValue, baseFieldPointer) mustBe Seq(constructAlteredField(fieldPointer, originalValue, goodsLocation))
      }

      "the original version's qualifierOfIdentification field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}"
        val goodsLocation =
          GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
        val originalValue = goodsLocation.copy(qualifierOfIdentification = "original")
        goodsLocation.createDiff(originalValue, baseFieldPointer) mustBe Seq(constructAlteredField(fieldPointer, originalValue, goodsLocation))
      }

      "the original version's identificationOfLocation field has a different value to this one" in {
        withClue("both version have Some value") {
          val fieldPointer = s"${baseFieldPointer}"
          val goodsLocation =
            GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
          val originalValue = goodsLocation.copy(qualifierOfIdentification = "original")
          goodsLocation.createDiff(originalValue, baseFieldPointer) mustBe Seq(constructAlteredField(fieldPointer, originalValue, goodsLocation))
        }
      }
    }
  }

  "Locations.createDiff" should {
    val baseFieldPointer = Locations.pointer

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("both values are None") {
          val locations = Locations()
          locations.createDiff(locations, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }
      }

      "differences exist between the two versions" when {
        val latestCountry = Country(Some("GB"))

        "the original version's originationCountry field has a different value to this one" in {
          val fieldPointer = s"${baseFieldPointer}.${Locations.originationCountryPointer}"
          val locations = Locations(originationCountry = Some(latestCountry))
          val originalValue = Some(Country(Some("FR")))
          locations.createDiff(locations.copy(originationCountry = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, locations.originationCountry)
          )
        }

        "the original version's destinationCountry field has a different value to this one" in {
          val fieldPointer = s"${baseFieldPointer}.${Locations.destinationCountryPointer}"
          val locations = Locations(destinationCountry = Some(latestCountry))
          val originalValue = Some(Country(Some("FR")))
          locations.createDiff(locations.copy(destinationCountry = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, locations.destinationCountry)
          )
        }

        "when routingCountries are not equal" in {
          val fieldPointer = s"$baseFieldPointer.${Locations.routingCountriesPointer}"
          val routingCountries =
            Seq(RoutingCountry(1, Country(Some("one"))), RoutingCountry(2, Country(Some("two"))), RoutingCountry(3, Country(Some("three"))))

          withClue("original Locations routingCountries are not present") {
            val locations = Locations(routingCountries = routingCountries)
            locations.createDiff(locations.copy(routingCountries = Seq.empty), baseFieldPointer, Some(1)) must contain theSameElementsAs Seq(
              constructAlteredField(s"${fieldPointer}.#1", None, Some(routingCountries(0))),
              constructAlteredField(s"${fieldPointer}.#2", None, Some(routingCountries(1))),
              constructAlteredField(s"${fieldPointer}.#3", None, Some(routingCountries(2)))
            )
          }

          withClue("this locations routingCountries are not present") {
            val locations = Locations(routingCountries = Seq.empty)
            locations.createDiff(locations.copy(routingCountries = routingCountries), baseFieldPointer, Some(1)) mustBe Seq(
              constructAlteredField(s"${fieldPointer}.#1", Some(routingCountries(0)), None),
              constructAlteredField(s"${fieldPointer}.#2", Some(routingCountries(1)), None),
              constructAlteredField(s"${fieldPointer}.#3", Some(routingCountries(2)), None)
            )
          }

          withClue("both locations routingCountries contain different number of elements") {
            val locations = Locations(routingCountries = routingCountries.drop(1))
            locations.createDiff(locations.copy(routingCountries = routingCountries), baseFieldPointer, Some(1)) mustBe Seq(
              constructAlteredField(s"${fieldPointer}.#1", Some(routingCountries(0)), None)
            )
          }

          withClue("locations routingCountries contain elements with different values") {
            val locations = Locations(routingCountries =
              Seq(RoutingCountry(4, Country(Some("other"))), RoutingCountry(5, Country(Some("other"))), RoutingCountry(6, Country(Some("other"))))
            )
            locations.createDiff(locations.copy(routingCountries = routingCountries), baseFieldPointer, Some(1)) must contain theSameElementsAs Seq(
              constructAlteredField(s"${fieldPointer}.#1", Some(routingCountries(0)), None),
              constructAlteredField(s"${fieldPointer}.#2", Some(routingCountries(1)), None),
              constructAlteredField(s"${fieldPointer}.#3", Some(routingCountries(2)), None),
              constructAlteredField(s"${fieldPointer}.#4", None, Some(RoutingCountry(4, Country(Some("other"))))),
              constructAlteredField(s"${fieldPointer}.#5", None, Some(RoutingCountry(5, Country(Some("other"))))),
              constructAlteredField(s"${fieldPointer}.#6", None, Some(RoutingCountry(6, Country(Some("other")))))
            )
          }

          withClue("locations routingCountries contain elements with different values") {
            val newRoutingCountries =
              Seq(RoutingCountry(1, Country(Some("one"))), RoutingCountry(2, Country(Some("DIFF"))), RoutingCountry(4, Country(Some("four"))))
            val locations = Locations(routingCountries = newRoutingCountries)

            locations.createDiff(
              locations.copy(routingCountries = routingCountries.dropRight(1)),
              baseFieldPointer,
              Some(1)
            ) must contain theSameElementsAs Seq(
              constructAlteredField(s"${fieldPointer}.#2", Some(routingCountries(1).country), Some(newRoutingCountries(1).country)),
              constructAlteredField(s"${fieldPointer}.#4", None, Some(RoutingCountry(4, Country(Some("four")))))
            )
          }
        }

        "the original version's officeOfExit field has a different value to this one" in {
          val fieldPointer = s"${baseFieldPointer}.${OfficeOfExit.pointer}"
          withClue("both versions have Some(Some) values but values are different") {
            val locations = Locations(officeOfExit = Some(OfficeOfExit("latest")))
            val originalValue = OfficeOfExit("original")
            locations.createDiff(locations.copy(officeOfExit = Some(originalValue)), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.officeOfExit.get)
            )
          }
        }

        "the original version's supervisingCustomsOffice field has a different value to this one" in {
          val fieldPointer = s"${baseFieldPointer}.${SupervisingCustomsOffice.pointer}"
          withClue("both versions have Some(Some) values but values are different") {
            val locations = Locations(supervisingCustomsOffice = Some(SupervisingCustomsOffice(Some("latest"))))
            val originalValue = SupervisingCustomsOffice(Some("original"))
            locations.createDiff(locations.copy(supervisingCustomsOffice = Some(originalValue)), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.supervisingCustomsOffice.get)
            )
          }

          withClue("the original version's supervisingCustomsOffice field is Some(None) but this one has Some(Some) value") {
            val locations = Locations(supervisingCustomsOffice = Some(SupervisingCustomsOffice(Some("latest"))))
            val originalValue = SupervisingCustomsOffice(None)
            locations.createDiff(locations.copy(supervisingCustomsOffice = Some(originalValue)), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.supervisingCustomsOffice.get)
            )
          }

          withClue("the original version's supervisingCustomsOffice field is None but this one has Some(Some) value") {
            val locations = Locations(supervisingCustomsOffice = Some(SupervisingCustomsOffice(Some("latest"))))
            val originalValue = None
            locations.createDiff(locations.copy(supervisingCustomsOffice = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.supervisingCustomsOffice)
            )
          }

          withClue("the original version's supervisingCustomsOffice field is None but this one has Some(None) as its value") {
            val locations = Locations(supervisingCustomsOffice = Some(SupervisingCustomsOffice(None)))
            val originalValue = None
            locations.createDiff(locations.copy(supervisingCustomsOffice = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.supervisingCustomsOffice)
            )
          }

          withClue("the original version's supervisingCustomsOffice field is Some(None) but this one has None as its value") {
            val locations = Locations(supervisingCustomsOffice = None)
            val originalValue = Some(SupervisingCustomsOffice(None))
            locations.createDiff(locations.copy(supervisingCustomsOffice = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.supervisingCustomsOffice)
            )
          }

          withClue("both versions have None values") {
            val locations = Locations(supervisingCustomsOffice = None)
            locations.createDiff(locations, baseFieldPointer) mustBe Seq.empty
          }
        }

        "the original version's warehouseIdentification field has a different value to this one" in {
          val fieldPointer = s"${baseFieldPointer}.${WarehouseIdentification.pointer}"
          withClue("both versions have Some(Some) values but values are different") {
            val locations = Locations(warehouseIdentification = Some(WarehouseIdentification(Some("latest"))))
            val originalValue = WarehouseIdentification(Some("original"))
            locations.createDiff(locations.copy(warehouseIdentification = Some(originalValue)), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.warehouseIdentification.get)
            )
          }

          withClue("the original version's warehouseIdentification field is Some(None) but this one has Some(Some) value") {
            val locations = Locations(warehouseIdentification = Some(WarehouseIdentification(Some("latest"))))
            val originalValue = WarehouseIdentification(None)
            locations.createDiff(locations.copy(warehouseIdentification = Some(originalValue)), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.warehouseIdentification.get)
            )
          }

          withClue("the original version's warehouseIdentification field is None but this one has Some(Some) value") {
            val locations = Locations(warehouseIdentification = Some(WarehouseIdentification(Some("latest"))))
            val originalValue = None
            locations.createDiff(locations.copy(warehouseIdentification = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.warehouseIdentification)
            )
          }

          withClue("the original version's warehouseIdentification field is None but this one has Some(None) as its value") {
            val locations = Locations(warehouseIdentification = Some(WarehouseIdentification(None)))
            val originalValue = None
            locations.createDiff(locations.copy(warehouseIdentification = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.warehouseIdentification)
            )
          }

          withClue("the original version's warehouseIdentification field is Some(None) but this one has None as its value") {
            val locations = Locations(supervisingCustomsOffice = None)
            val originalValue = Some(WarehouseIdentification(None))
            locations.createDiff(locations.copy(warehouseIdentification = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.warehouseIdentification)
            )
          }

          withClue("both versions have None values") {
            val locations = Locations(warehouseIdentification = None)
            locations.createDiff(locations, baseFieldPointer) mustBe Seq.empty
          }
        }

        "the original version's inlandModeOfTransportCode field has a different value to this one" in {
          val fieldPointer = s"${baseFieldPointer}.${InlandModeOfTransportCode.pointer}"
          withClue("both versions have Some(Some) values but values are different") {
            val locations = Locations(inlandModeOfTransportCode = Some(InlandModeOfTransportCode(Some(Maritime))))
            val originalValue = Some(InlandModeOfTransportCode(Some(Rail)))
            locations.createDiff(locations.copy(inlandModeOfTransportCode = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.inlandModeOfTransportCode)
            )
          }

          withClue("the original version's inlandModeOfTransportCode field is Some(None) but this one has Some(Some) value") {
            val locations = Locations(inlandModeOfTransportCode = Some(InlandModeOfTransportCode(Some(Maritime))))
            val originalValue = Some(InlandModeOfTransportCode(None))
            locations.createDiff(locations.copy(inlandModeOfTransportCode = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.inlandModeOfTransportCode)
            )
          }

          withClue("the original version's inlandModeOfTransportCode field is None but this one has Some(Some) value") {
            val locations = Locations(inlandModeOfTransportCode = Some(InlandModeOfTransportCode(Some(Maritime))))
            val originalValue = None
            locations.createDiff(locations.copy(inlandModeOfTransportCode = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.inlandModeOfTransportCode)
            )
          }

          withClue("the original version's inlandModeOfTransportCode field is None but this one has Some(None) as its value") {
            val locations = Locations(inlandModeOfTransportCode = Some(InlandModeOfTransportCode(None)))
            val originalValue = None
            locations.createDiff(locations.copy(inlandModeOfTransportCode = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.inlandModeOfTransportCode)
            )
          }

          withClue("the original version's inlandModeOfTransportCode field is Some(None) but this one has None as its value") {
            val locations = Locations(supervisingCustomsOffice = None)
            val originalValue = Some(InlandModeOfTransportCode(None))
            locations.createDiff(locations.copy(inlandModeOfTransportCode = originalValue), baseFieldPointer) mustBe Seq(
              constructAlteredField(fieldPointer, originalValue, locations.inlandModeOfTransportCode)
            )
          }

          withClue("both versions have None values") {
            val locations = Locations(inlandModeOfTransportCode = None)
            locations.createDiff(locations, baseFieldPointer) mustBe Seq.empty
          }
        }
      }
    }
  }
}
