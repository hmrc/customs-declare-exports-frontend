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

package connectors.exchange

import java.time.Instant

import forms.{Ducr, Lrn}
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.declaration.{Locations, Parties, TransportInformationContainerData}
import models.{DeclarationStatus, ExportsDeclaration}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.scalatest.{Matchers, WordSpec}
import services.cache.{ExportItem, ExportsDeclarationBuilder}

class ExportsDeclarationExchangeSpec extends WordSpec with Matchers with ExportsDeclarationBuilder with MockitoSugar {

  private val id = "id"
  private val sourceId = "source-id"
  private val status = DeclarationStatus.COMPLETE
  private val choice = "choice"
  private val createdDate = Instant.MIN
  private val updatedDate = Instant.MAX
  private val dispatchLocation = mock[DispatchLocation]
  private val additionalDeclarationType = mock[AdditionalDeclarationType]

  private val consignmentReferences = mock[ConsignmentReferences]
  when(consignmentReferences.ducr).thenReturn(Ducr(""))
  when(consignmentReferences.lrn).thenReturn(Lrn(""))

  private val borderTransport = mock[DepartureTransport]
  private val transportDetails = mock[BorderTransport]
  private val containers = mock[TransportInformationContainerData]
  private val parties = mock[Parties]
  private val locations = mock[Locations]
  private val item = mock[ExportItem]
  private val totalNumberOfItems = mock[TotalNumberOfItems]
  private val previousDocuments = mock[PreviousDocumentsData]
  private val natureOfTransaction = mock[NatureOfTransaction]

  private val request = ExportsDeclarationExchange(
    id = Some(id),
    status = status,
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
    sourceId = Some(sourceId),
    choice = choice,
    dispatchLocation = Some(dispatchLocation),
    additionalDeclarationType = Some(additionalDeclarationType),
    consignmentReferences = Some(consignmentReferences),
    borderTransport = Some(borderTransport),
    transportDetails = Some(transportDetails),
    containerData = Some(containers),
    parties = parties,
    locations = locations,
    items = Set(item),
    totalNumberOfItems = Some(totalNumberOfItems),
    previousDocuments = Some(previousDocuments),
    natureOfTransaction = Some(natureOfTransaction)
  )

  private val declaration = ExportsDeclaration(
    id = id,
    status = status,
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
    sourceId = Some(sourceId),
    choice = choice,
    dispatchLocation = Some(dispatchLocation),
    additionalDeclarationType = Some(additionalDeclarationType),
    consignmentReferences = Some(consignmentReferences),
    borderTransport = Some(borderTransport),
    transportDetails = Some(transportDetails),
    containerData = Some(containers),
    parties = parties,
    locations = locations,
    items = Set(item),
    totalNumberOfItems = Some(totalNumberOfItems),
    previousDocuments = Some(previousDocuments),
    natureOfTransaction = Some(natureOfTransaction)
  )

  "Request" should {
    "map to ExportsDeclaration" in {
      request.toExportsDeclaration shouldBe declaration
    }

    "map from ExportsDeclaration" in {
      ExportsDeclarationExchange(declaration) shouldBe request
    }
  }
}
