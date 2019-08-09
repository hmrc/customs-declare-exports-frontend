package connectors.request

import java.time.Instant

import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.declaration.{Locations, Parties, TransportInformationContainerData}
import models.{DeclarationStatus, ExportsDeclaration}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.{ExportItem, ExportsDeclarationBuilder}

class ExportsDeclarationRequestSpec extends WordSpec with Matchers with ExportsDeclarationBuilder with MockitoSugar {

  private val status = DeclarationStatus.COMPLETE
  private val choice = "choice"
  private val createdDate = Instant.MIN
  private val updatedDate = Instant.MAX
  private val sessionId = "session-id"
  private val draftId = "draft-id"
  private val dispatchLocation = mock[DispatchLocation]
  private val additionalDeclarationType = mock[AdditionalDeclarationType]
  private val consignmentReferences = mock[ConsignmentReferences]
  private val borderTransport = mock[BorderTransport]
  private val transportDetails = mock[TransportDetails]
  private val containers = mock[TransportInformationContainerData]
  private val parties = mock[Parties]
  private val locations = mock[Locations]
  private val item = mock[ExportItem]
  private val totalNumberOfItems = mock[TotalNumberOfItems]
  private val previousDocuments = mock[PreviousDocumentsData]
  private val natureOfTransaction = mock[NatureOfTransaction]
  private val seal = mock[Seal]

  private val request = ExportsDeclarationRequest(
    status = status,
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
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
    natureOfTransaction = Some(natureOfTransaction),
    seals = Seq(seal)
  )

  private val declaration = ExportsDeclaration(
    status = status,
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
    sessionId = sessionId,
    draftId = draftId,
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
    natureOfTransaction = Some(natureOfTransaction),
    seals = Seq(seal)
  )

  "Request" should {
    "map to ExportsDeclaration" in {
      request.toExportsDeclaration(sessionId, draftId) shouldBe declaration
    }

    "map from ExportsDeclaration" in {
      ExportsDeclarationRequest(declaration) shouldBe request
    }
  }

}
