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

package models.declaration.supplementary

import forms.supplementary._
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.{MustMatchers, WordSpec}

class PartiesSpec extends WordSpec with MustMatchers {

  private trait SimpleTest {
    val exporterDetailsMock = mock(classOf[ExporterDetails])
    val declarantDetailsMock = mock(classOf[DeclarantDetails])
    val representativeDetailsMock = mock(classOf[RepresentativeDetails])
    val consigneeDetailsMock = mock(classOf[ConsigneeDetails])
    val declarationAdditionalActorsMock = mock(classOf[DeclarationAdditionalActors])
    val declarationHoldersDataMock = mock(classOf[DeclarationHoldersData])
    val parties = Parties(
      exporterDetails = Some(exporterDetailsMock),
      declarantDetails = Some(declarantDetailsMock),
      representativeDetails = Some(representativeDetailsMock),
      consigneeDetails = Some(consigneeDetailsMock),
      declarationAdditionalActors = Some(declarationAdditionalActorsMock),
      declarationHoldersData = Some(declarationHoldersDataMock)
    )

    when(exporterDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(declarantDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(representativeDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(consigneeDetailsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(declarationAdditionalActorsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(declarationHoldersDataMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
  }

  private trait TestMapConcatenation extends SimpleTest {
    val exporterDetailsMap = Map("ExporterDetails" -> "ExporterValue")
    val declarantDetailsMap = Map("DeclarantDetails" -> "DeclarantValue")
    val representativeDetailsMap = Map("RepresentativeDetails" -> "RepresentativeValue")
    val consigneeDetailsMap = Map("ConsigneeDetails" -> "ConsigneeValue")
    val declarationAdditionalActorsMap = Map("DeclarationAdditionalActors" -> "AdditionalActorsValue")
    val declarationHolderMap = Map("DeclarationHolder" -> "HolderValue")
    when(exporterDetailsMock.toMetadataProperties()).thenReturn(exporterDetailsMap)
    when(declarantDetailsMock.toMetadataProperties()).thenReturn(declarantDetailsMap)
    when(representativeDetailsMock.toMetadataProperties()).thenReturn(representativeDetailsMap)
    when(consigneeDetailsMock.toMetadataProperties()).thenReturn(consigneeDetailsMap)
    when(declarationAdditionalActorsMock.toMetadataProperties()).thenReturn(declarationAdditionalActorsMap)
    when(declarationHoldersDataMock.toMetadataProperties()).thenReturn(declarationHolderMap)
  }

  "Parties" when {

    "method toMetadataProperties is invoked" should {
      "call the toMetadataProperties on all contained objects" in new SimpleTest {
        parties.toMetadataProperties()

        verify(exporterDetailsMock, times(1)).toMetadataProperties()
        verify(declarantDetailsMock, times(1)).toMetadataProperties()
        verify(representativeDetailsMock, times(1)).toMetadataProperties()
        verify(consigneeDetailsMock, times(1)).toMetadataProperties()
        verify(declarationAdditionalActorsMock, times(1)).toMetadataProperties()
        verify(declarationHoldersDataMock, times(1)).toMetadataProperties()
      }

      "return Map being sum of all Maps from sub-objects" in new TestMapConcatenation {
        parties.toMetadataProperties() must equal(
          exporterDetailsMap ++ declarantDetailsMap ++ representativeDetailsMap ++ consigneeDetailsMap ++ declarationAdditionalActorsMap ++ declarationHolderMap
        )
      }
    }
  }

}
