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

package models.declaration

import forms.declaration._
import forms.declaration.destinationCountries.DestinationCountriesSupplementary
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.{MustMatchers, WordSpec}

class LocationsSpec extends WordSpec with MustMatchers {

  private trait SimpleTest {
    val destinationCountriesMock = mock(classOf[DestinationCountriesSupplementary])
    val goodsLocationMock = mock(classOf[GoodsLocation])
    val procedureCodesMock = mock(classOf[ProcedureCodesData])
    val warehouseIdentificationMock = mock(classOf[WarehouseIdentification])
    val officeOfExitMock = mock(classOf[OfficeOfExit])
    val locations = Locations(
      destinationCountries = Some(destinationCountriesMock),
      goodsLocation = Some(goodsLocationMock),
      warehouseIdentification = Some(warehouseIdentificationMock),
      officeOfExit = Some(officeOfExitMock)
    )

    when(destinationCountriesMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(goodsLocationMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(warehouseIdentificationMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(officeOfExitMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
  }

  private trait TestMapConcatenation extends SimpleTest {
    val destinationCountriesMap = Map("DestinationCountries" -> "DestinationCountriesValue")
    val goodsLocationMap = Map("GoodsLocation" -> "GoodsLocationValue")
    val procedureCodesMap = Map("ProcedureCodes" -> "ProcedureCodesValue")
    val warehouseIdentificationMap = Map("WarehouseIdentification" -> "WarehouseIdentificationValue")
    val officeOfExitMap = Map("OfficeOfExit" -> "OfficeOfExitValue")
    when(destinationCountriesMock.toMetadataProperties()).thenReturn(destinationCountriesMap)
    when(goodsLocationMock.toMetadataProperties()).thenReturn(goodsLocationMap)
    when(warehouseIdentificationMock.toMetadataProperties()).thenReturn(warehouseIdentificationMap)
    when(officeOfExitMock.toMetadataProperties()).thenReturn(officeOfExitMap)
  }

  "Locations" when {

    "method toMetadataProperties is invoked" should {
      "call the toMetadataProperties on all contained objects" in new SimpleTest {
        locations.toMetadataProperties()

        verify(destinationCountriesMock, times(1)).toMetadataProperties()
        verify(goodsLocationMock, times(1)).toMetadataProperties()
        verify(warehouseIdentificationMock, times(1)).toMetadataProperties()
        verify(officeOfExitMock, times(1)).toMetadataProperties()
      }

      "return Map being sum of all Maps from sub-objects" in new TestMapConcatenation {
        locations.toMetadataProperties() must equal(
          destinationCountriesMap ++ goodsLocationMap ++ warehouseIdentificationMap ++ officeOfExitMap
        )
      }
    }
  }

}
