package services.cache

import java.time.LocalDateTime
import java.util.UUID

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.{ConsignmentReferences, DeclarationHolder, DispatchLocation, TotalNumberOfItems}
import forms.{Choice, Ducr}
import models.declaration.DeclarationHoldersData

trait ExportsCacheModelBuilder {

  protected val DUCR = "5GB123456789000-123ABC456DEFIIIII"
  protected val LRN = "FG7676767889"

  private def uuid: String = UUID.randomUUID().toString

  private val modelWithDefaults: ExportsCacheModel = ExportsCacheModel(
    sessionId = uuid,
    draftId = uuid,
    createdDateTime = LocalDateTime.of(2019, 1, 1, 0, 0, 0),
    updatedDateTime = LocalDateTime.of(2019, 2, 2, 0, 0, 0),
    choice = Choice.AllowedChoiceValues.StandardDec
  )

  def aCacheModel(modifiers: (ExportsCacheModel => ExportsCacheModel)*): ExportsCacheModel =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withTotalNumberOfItems(
    totalAmountInvoiced: Option[String] = None,
    exchangeRate: Option[String] = None,
    totalPackage: String = "1"
  ): ExportsCacheModel => ExportsCacheModel =
    _.copy(totalNumberOfItems = Some(TotalNumberOfItems(totalAmountInvoiced, exchangeRate, totalPackage)))

  def withConsignmentReference(
    ducr: Option[String] = Some(DUCR),
    lrn: String = LRN
  ): ExportsCacheModel => ExportsCacheModel =
    _.copy(consignmentReferences = Some(ConsignmentReferences(ducr.map(Ducr(_)), lrn)))

  def withAdditionalDeclarationType(
    decType: String = AllowedAdditionalDeclarationTypes.Standard
  ): ExportsCacheModel => ExportsCacheModel =
    _.copy(additionalDeclarationType = Some(AdditionalDeclarationType(decType)))

  def withDispatchLocation(location: String = "GB"): ExportsCacheModel => ExportsCacheModel =
    _.copy(dispatchLocation = Some(DispatchLocation(location)))

  def withItem(id: String = uuid): ExportsCacheModel => ExportsCacheModel =
    m => m.copy(items = m.items + ExportItem(id = id))

  def withItems(count: Int): ExportsCacheModel => ExportsCacheModel =
    m => m.copy(items = m.items ++ (1 to count).map(_ => ExportItem(id = uuid)).toSet)

  def withDeclarationHolder(
    authorisationTypeCode: Option[String] = None,
    eori: Option[String] = None
  ): ExportsCacheModel => ExportsCacheModel = { m =>
    val existing: Seq[DeclarationHolder] = m.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)
    val holdersData = DeclarationHoldersData(existing :+ DeclarationHolder(authorisationTypeCode, eori))
    m.copy(parties = m.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withDeclarationHolders(holders: DeclarationHolder*): ExportsCacheModel => ExportsCacheModel =
    m => m.copy(parties = m.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders))))
}
