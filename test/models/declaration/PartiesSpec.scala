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
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.{MustMatchers, WordSpec}

class PartiesSpec extends WordSpec with MustMatchers {

  private trait SimpleTest {
    val exporterDetailsMock = mock(classOf[ExporterDetails])
    val consigneeDetailsMock = mock(classOf[ConsigneeDetails])
    val declarantDetailsMock = mock(classOf[DeclarantDetails])
    val representativeDetailsMock = mock(classOf[RepresentativeDetails])
    val declarationAdditionalActorsDataMock = mock(classOf[DeclarationAdditionalActorsData])
    val declarationHoldersDataMock = mock(classOf[DeclarationHoldersData])
    val parties = Parties(
      exporterDetails = Some(exporterDetailsMock),
      consigneeDetails = Some(consigneeDetailsMock),
      declarantDetails = Some(declarantDetailsMock),
      representativeDetails = Some(representativeDetailsMock),
      declarationAdditionalActorsData = Some(declarationAdditionalActorsDataMock),
      declarationHoldersData = Some(declarationHoldersDataMock)
    )

    when(exporterDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(consigneeDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(declarantDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(representativeDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(declarationAdditionalActorsDataMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(declarationHoldersDataMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
  }

  private trait TestMapConcatenation extends SimpleTest {
    val exporterDetailsMap = Map("ExporterDetails" -> "ExporterValue")
    val consigneeDetailsMap = Map("ConsigneeDetails" -> "ConsigneeValue")
    val declarantDetailsMap = Map("DeclarantDetails" -> "DeclarantValue")
    val representativeDetailsMap = Map("RepresentativeDetails" -> "RepresentativeValue")
    val declarationAdditionalActorsDataMap = Map("DeclarationAdditionalActorsData" -> "AdditionalActorsValue")
    val declarationHolderMap = Map("DeclarationHolder" -> "HolderValue")
    when(exporterDetailsMock.toMetadataProperties()).thenReturn(exporterDetailsMap)
    when(consigneeDetailsMock.toMetadataProperties()).thenReturn(consigneeDetailsMap)
    when(declarantDetailsMock.toMetadataProperties()).thenReturn(declarantDetailsMap)
    when(representativeDetailsMock.toMetadataProperties()).thenReturn(representativeDetailsMap)
    when(declarationAdditionalActorsDataMock.toMetadataProperties()).thenReturn(declarationAdditionalActorsDataMap)
    when(declarationHoldersDataMock.toMetadataProperties()).thenReturn(declarationHolderMap)
  }

  "Parties" when {

    "method toMetadataProperties is invoked" should {
      "call the toMetadataProperties on all contained objects" in new SimpleTest {
        parties.toMetadataProperties()

        verify(exporterDetailsMock, times(1)).toMetadataProperties()
        verify(consigneeDetailsMock, times(1)).toMetadataProperties()
        verify(declarantDetailsMock, times(1)).toMetadataProperties()
        verify(representativeDetailsMock, times(1)).toMetadataProperties()
        verify(declarationAdditionalActorsDataMock, times(1)).toMetadataProperties()
        verify(declarationHoldersDataMock, times(1)).toMetadataProperties()
      }

      "return Map being sum of all Maps from sub-objects" in new TestMapConcatenation {
        parties.toMetadataProperties() must equal(
          exporterDetailsMap ++ consigneeDetailsMap ++ declarantDetailsMap ++ representativeDetailsMap ++ declarationAdditionalActorsDataMap ++ declarationHolderMap
        )
      }
    }
  }

}
