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

package connectors.request

import java.time.Instant

import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.DeclarationStatus.DeclarationStatus
import models.ExportsDeclaration
import models.declaration.{Locations, Parties, TransportInformationContainerData}
import play.api.libs.json.{Json, OFormat}
import services.cache.ExportItem

case class ExportsDeclarationRequest(status: DeclarationStatus,
                                     createdDateTime: Instant,
                                     updatedDateTime: Instant,
                                     choice: String,
                                     dispatchLocation: Option[DispatchLocation] = None,
                                     additionalDeclarationType: Option[AdditionalDeclarationType] = None,
                                     consignmentReferences: Option[ConsignmentReferences] = None,
                                     borderTransport: Option[BorderTransport] = None,
                                     transportDetails: Option[TransportDetails] = None,
                                     containerData: Option[TransportInformationContainerData] = None,
                                     parties: Parties = Parties(),
                                     locations: Locations = Locations(),
                                     items: Set[ExportItem] = Set.empty[ExportItem],
                                     totalNumberOfItems: Option[TotalNumberOfItems] = None,
                                     previousDocuments: Option[PreviousDocumentsData] = None,
                                     natureOfTransaction: Option[NatureOfTransaction] = None,
                                     seals: Seq[Seal] = Seq.empty
                                    ) {
  def toExportsDeclaration(sessionId: String, draftId: String): ExportsDeclaration = ExportsDeclaration(
    sessionId = sessionId,
    draftId = draftId,
    status = this.status,
    createdDateTime = this.createdDateTime,
    updatedDateTime = this.updatedDateTime,
    choice = this.choice,
    dispatchLocation = this.dispatchLocation,
    additionalDeclarationType = this.additionalDeclarationType,
    consignmentReferences = this.consignmentReferences,
    borderTransport = this.borderTransport,
    transportDetails = this.transportDetails,
    containerData = this.containerData,
    parties = this.parties,
    locations = this.locations,
    items = this.items,
    totalNumberOfItems = this.totalNumberOfItems,
    previousDocuments = this.previousDocuments,
    natureOfTransaction = this.natureOfTransaction,
    seals = this.seals
  )
}

object ExportsDeclarationRequest {
  implicit val format: OFormat[ExportsDeclarationRequest] = Json.format[ExportsDeclarationRequest]

  def apply(declaration: ExportsDeclaration): ExportsDeclarationRequest = ExportsDeclarationRequest(
    status = declaration.status,
    createdDateTime = declaration.createdDateTime,
    updatedDateTime = declaration.updatedDateTime,
    choice = declaration.choice,
    dispatchLocation = declaration.dispatchLocation,
    additionalDeclarationType = declaration.additionalDeclarationType,
    consignmentReferences = declaration.consignmentReferences,
    borderTransport = declaration.borderTransport,
    transportDetails = declaration.transportDetails,
    containerData = declaration.containerData,
    parties = declaration.parties,
    locations = declaration.locations,
    items = declaration.items,
    totalNumberOfItems = declaration.totalNumberOfItems,
    previousDocuments = declaration.previousDocuments,
    natureOfTransaction = declaration.natureOfTransaction,
    seals = declaration.seals
  )
}
