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
import forms.declaration.countries.Country
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.ModeOfTransportCode.{Maritime, Rail}
import models.declaration.{GoodsLocation, Locations, RoutingCountry}
import services.AlteredField
import services.AlteredField.constructAlteredField

class LocationsSpec extends UnitSpec {
  "Country.createDiff" should {
    val baseFieldPointer = Locations.pointer
    val someVal = Some("GB")

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("both values are None") {
          val country = Country(None)
          country.createDiff(country, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }

        withClue("both values are same Some value") {
          val country = Country(someVal)
          country.createDiff(country, baseFieldPointer) mustBe Seq.empty[AlteredField]
        }
      }

      "differences exist between the two versions" in {
        withClue("original is None but this has Some") {
          val fieldPointer = s"${baseFieldPointer}.${Country.pointer}"
          val country = Country(someVal)
          val originalValue = None
          country.createDiff(country.copy(code = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, country.code)
          )
        }

        withClue("original is Some but this has None") {
          val fieldPointer = s"${baseFieldPointer}.${Country.pointer}"
          val country = Country(None)
          val originalValue = someVal
          country.createDiff(country.copy(code = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, country.code)
          )
        }

        withClue("original is Some and this is Some but values are different") {
          val fieldPointer = s"${baseFieldPointer}.${Country.pointer}"
          val country = Country(Some("FR"))
          val originalValue = someVal
          country.createDiff(country.copy(code = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, country.code)
          )
        }
      }
    }
  }

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
        val fieldPointer = s"${baseFieldPointer}.${GoodsLocation.countryPointer}"
        val goodsLocation =
          GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
        val originalValue = "original"
        goodsLocation.createDiff(goodsLocation.copy(country = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, goodsLocation.country)
        )
      }

      "the original version's typeOfLocation field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${GoodsLocation.typeOfLocationPointer}"
        val goodsLocation =
          GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
        val originalValue = "original"
        goodsLocation.createDiff(goodsLocation.copy(typeOfLocation = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, goodsLocation.typeOfLocation)
        )
      }

      "the original version's qualifierOfIdentification field has a different value to this one" in {
        val fieldPointer = s"${baseFieldPointer}.${GoodsLocation.qualifierOfIdentificationPointer}"
        val goodsLocation =
          GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
        val originalValue = "original"
        goodsLocation.createDiff(goodsLocation.copy(qualifierOfIdentification = originalValue), baseFieldPointer) mustBe Seq(
          constructAlteredField(fieldPointer, originalValue, goodsLocation.qualifierOfIdentification)
        )
      }

      "the original version's identificationOfLocation field has a different value to this one" in {
        withClue("both version have Some value") {
          val fieldPointer = s"${baseFieldPointer}.${GoodsLocation.identificationOfLocationPointer}"
          val goodsLocation =
            GoodsLocation("latestCountry", "latestTypeOfLocation", "latestQualifierOfIdentification", "latestIdentificationOfLocation")
          val originalValue = "original"
          goodsLocation.createDiff(goodsLocation.copy(identificationOfLocation = originalValue), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue, goodsLocation.identificationOfLocation)
          )
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
          val fieldPointer = s"${baseFieldPointer}.${Locations.originationCountryPointer}.${Country.pointer}"
          val locations = Locations(originationCountry = Some(latestCountry))
          val originalValue = Country(Some("FR"))
          locations.createDiff(locations.copy(originationCountry = Some(originalValue)), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue.code, locations.originationCountry.get.code)
          )
        }

        "the original version's destinationCountry field has a different value to this one" in {
          val fieldPointer = s"${baseFieldPointer}.${Locations.destinationCountryPointer}.${Country.pointer}"
          val locations = Locations(destinationCountry = Some(latestCountry))
          val originalValue = Country(Some("FR"))
          locations.createDiff(locations.copy(destinationCountry = Some(originalValue)), baseFieldPointer) mustBe Seq(
            constructAlteredField(fieldPointer, originalValue.code, locations.destinationCountry.get.code)
          )
        }

        "when routingCountries are not equal" in {
          val fieldPointer = s"$baseFieldPointer.${Locations.routingCountriesPointer}"
          val routingCountries =
            Seq(RoutingCountry(1, Country(Some("one"))), RoutingCountry(2, Country(Some("two"))), RoutingCountry(3, Country(Some("three"))))

          withClue("original Locations routingCountries are not present") {
            val locations = Locations(routingCountries = routingCountries)
            locations.createDiff(locations.copy(routingCountries = Seq.empty), baseFieldPointer, Some(1)) must contain theSameElementsAs Seq(
              constructAlteredField(s"${fieldPointer}.1", None, Some(routingCountries(0))),
              constructAlteredField(s"${fieldPointer}.2", None, Some(routingCountries(1))),
              constructAlteredField(s"${fieldPointer}.3", None, Some(routingCountries(2)))
            )
          }

          withClue("this locations routingCountries are not present") {
            val locations = Locations(routingCountries = Seq.empty)
            locations.createDiff(locations.copy(routingCountries = routingCountries), baseFieldPointer, Some(1)) mustBe Seq(
              constructAlteredField(s"${fieldPointer}.1", Some(routingCountries(0)), None),
              constructAlteredField(s"${fieldPointer}.2", Some(routingCountries(1)), None),
              constructAlteredField(s"${fieldPointer}.3", Some(routingCountries(2)), None)
            )
          }

          withClue("both locations routingCountries contain different number of elements") {
            val locations = Locations(routingCountries = routingCountries.drop(1))
            locations.createDiff(locations.copy(routingCountries = routingCountries), baseFieldPointer, Some(1)) mustBe Seq(
              constructAlteredField(s"${fieldPointer}.1", Some(routingCountries(0)), None)
            )
          }

          withClue("locations routingCountries contain elements with different values") {
            val locations = Locations(routingCountries =
              Seq(RoutingCountry(4, Country(Some("other"))), RoutingCountry(5, Country(Some("other"))), RoutingCountry(6, Country(Some("other"))))
            )
            locations.createDiff(locations.copy(routingCountries = routingCountries), baseFieldPointer, Some(1)) must contain theSameElementsAs Seq(
              constructAlteredField(s"${fieldPointer}.1", Some(routingCountries(0)), None),
              constructAlteredField(s"${fieldPointer}.2", Some(routingCountries(1)), None),
              constructAlteredField(s"${fieldPointer}.3", Some(routingCountries(2)), None),
              constructAlteredField(s"${fieldPointer}.4", None, Some(RoutingCountry(4, Country(Some("other"))))),
              constructAlteredField(s"${fieldPointer}.5", None, Some(RoutingCountry(5, Country(Some("other"))))),
              constructAlteredField(s"${fieldPointer}.6", None, Some(RoutingCountry(6, Country(Some("other")))))
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
            val originalValue = InlandModeOfTransportCode(Some(Rail))
            locations.createDiff(locations.copy(inlandModeOfTransportCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
              constructAlteredField(
                fieldPointer,
                originalValue.inlandModeOfTransportCode,
                locations.inlandModeOfTransportCode.get.inlandModeOfTransportCode
              )
            )
          }

          withClue("the original version's inlandModeOfTransportCode field is Some(None) but this one has Some(Some) value") {
            val locations = Locations(inlandModeOfTransportCode = Some(InlandModeOfTransportCode(Some(Maritime))))
            val originalValue = InlandModeOfTransportCode(None)
            locations.createDiff(locations.copy(inlandModeOfTransportCode = Some(originalValue)), baseFieldPointer) mustBe Seq(
              constructAlteredField(
                fieldPointer,
                originalValue.inlandModeOfTransportCode,
                locations.inlandModeOfTransportCode.get.inlandModeOfTransportCode
              )
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
