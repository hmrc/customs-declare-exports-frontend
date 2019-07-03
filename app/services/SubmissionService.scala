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

package services

import com.google.inject.Inject
import config.AppConfig
import connectors.CustomsDeclareExportsConnector
import controllers.util.CacheIdGenerator.cacheId
import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.destinationCountries.{
  DestinationCountries,
  DestinationCountriesStandard,
  DestinationCountriesSupplementary
}
import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitStandard, OfficeOfExitSupplementary}
import forms.declaration.{DeclarantDetails, _}
import javax.inject.Singleton
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.submissionMetric
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.{GovernmentAgencyGoodsItem => InternalAgencyGoodsItem}
import models.declaration.{DeclarationAdditionalActorsData, DeclarationHoldersData, TransportInformationContainerData}
import models.requests.JourneyRequest
import play.api.Logger
import play.api.http.Status.ACCEPTED
import play.api.libs.json.{JsObject, Json}
import services.audit.EventData._
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(
  appConfig: AppConfig,
  cacheService: CustomsCacheService,
  exportsConnector: CustomsDeclareExportsConnector,
  auditService: AuditService,
  exportsMetrics: ExportsMetrics,
  mapper: WcoMetadataMapper
) {

  private val logger = Logger(this.getClass())

  def submit(
    cacheMap: CacheMap
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {

    val timerContext = exportsMetrics.startTimer(submissionMetric)
    val data = format(cacheMap, request.choice)
    auditService.auditAllPagesUserInput(getCachedData(cacheMap))
    exportsConnector.submitExportDeclaration(data.ducr, data.lrn, data.payload).flatMap {
      case HttpResponse(ACCEPTED, _, _, _) =>
        cacheService.remove(cacheId).map { _ =>
          auditService.audit(AuditTypes.Submission, auditData(data.lrn, data.ducr, Success.toString))
          exportsMetrics.incrementCounter(submissionMetric)
          timerContext.stop()
          data.lrn
        }
      case error =>
        logger.error(s"Error response from backend ${error.body}")
        auditService.audit(AuditTypes.Submission, auditData(data.lrn, data.ducr, Failure.toString))
        Future.successful(None)
    }
  }

  private def format(cacheMap: CacheMap, choice: Choice): FormattedData = {
    val metaData = mapper.produceMetaData(cacheMap, choice)

    val lrn = mapper.declarationLrn(metaData)
    val ducr = mapper.declarationUcr(metaData)
    val payload = mapper.toXml(metaData)

    FormattedData(lrn, ducr, payload)
  }

  private def auditData(lrn: Option[String], ducr: Option[String], result: String)(
    implicit request: JourneyRequest[_]
  ) =
    Map(
      EORI.toString -> request.authenticatedRequest.user.eori,
      DecType.toString -> request.choice.value,
      LRN.toString -> lrn.getOrElse(""),
      DUCR.toString -> ducr.getOrElse(""),
      SubmissionResult.toString -> result
    )

  def getCachedData(cacheMap: CacheMap)(implicit request: JourneyRequest[_]): JsObject = {

    val userInput = Map(
      "AdditionalDeclarationType" ->
        Json.toJson(cacheMap.getEntry[AdditionalDeclarationType]("AdditionalDeclarationType")),
      DeclarantDetails.id -> Json.toJson(cacheMap.getEntry[DeclarantDetails](DeclarantDetails.id)),
      ExporterDetails.id -> Json.toJson(cacheMap.getEntry[ExporterDetails](ExporterDetails.id)),
      RepresentativeDetails.formId -> Json.toJson(
        cacheMap.getEntry[RepresentativeDetails](RepresentativeDetails.formId)
      ),
      NatureOfTransaction.formId -> Json.toJson(cacheMap.getEntry[NatureOfTransaction](NatureOfTransaction.formId)),
      CarrierDetails.id -> Json.toJson(cacheMap.getEntry[CarrierDetails](CarrierDetails.id)),
      ConsigneeDetails.id -> Json.toJson(cacheMap.getEntry[ConsigneeDetails](ConsigneeDetails.id)),
      GoodsLocation.formId -> Json.toJson(cacheMap.getEntry[GoodsLocation](GoodsLocation.formId)),
      ConsignmentReferences.id -> Json.toJson(cacheMap.getEntry[ConsignmentReferences](ConsignmentReferences.id)),
      BorderTransport.formId -> Json.toJson(cacheMap.getEntry[BorderTransport](BorderTransport.formId)),
      TransportDetails.formId -> Json.toJson(cacheMap.getEntry[TransportDetails](TransportDetails.formId)),
      DestinationCountries.formId -> getDestinationCountries(cacheMap),
      DispatchLocation.formId -> Json.toJson(cacheMap.getEntry[DispatchLocation](DispatchLocation.formId)),
      OfficeOfExitForms.formId -> getOfficeOfExit(cacheMap),
      DeclarationAdditionalActorsData.formId -> Json.toJson(
        cacheMap
          .getEntry[DeclarationAdditionalActorsData](DeclarationAdditionalActorsData.formId)
          .map(_.actors) getOrElse (Seq.empty)
      ),
      DeclarationHoldersData.formId -> Json.toJson(
        cacheMap.getEntry[DeclarationHoldersData](DeclarationHoldersData.formId).map(_.holders) getOrElse (Seq.empty)
      ),
      TransportInformationContainerData.id -> Json.toJson(
        cacheMap
          .getEntry[TransportInformationContainerData](TransportInformationContainerData.id)
          .map(_.containers) getOrElse (Seq.empty)
      ),
      TransportInformationContainerData.id -> Json.toJson(
        cacheMap.getEntry[Seq[InternalAgencyGoodsItem]](ExportsItemsCacheIds.itemsId).getOrElse(Seq.empty)
      ),
      Seal.formId -> Json.toJson(cacheMap.getEntry[Seq[Seal]](Seal.formId)),
      Document.formId -> Json.toJson(
        cacheMap.getEntry[PreviousDocumentsData](Document.formId).map(_.documents).getOrElse(Seq.empty)
      ),
      TotalNumberOfItems.formId -> Json.toJson(cacheMap.getEntry[TotalNumberOfItems](TotalNumberOfItems.formId)),
      WarehouseIdentification.formId -> Json.toJson(
        cacheMap.getEntry[WarehouseIdentification](WarehouseIdentification.formId)
      )
    )
    Json.toJson(userInput).as[JsObject]
  }

  private def getDestinationCountries(cacheMap: CacheMap)(implicit request: JourneyRequest[_]) =
    if (request.choice.value == AllowedChoiceValues.SupplementaryDec)
      Json.toJson(cacheMap.getEntry[DestinationCountriesSupplementary](DestinationCountries.formId))
    else
      Json.toJson(cacheMap.getEntry[DestinationCountriesStandard](DestinationCountries.formId))

  private def getOfficeOfExit(cacheMap: CacheMap)(implicit request: JourneyRequest[_]) =
    if (request.choice.value == AllowedChoiceValues.SupplementaryDec)
      Json.toJson(cacheMap.getEntry[OfficeOfExitSupplementary](OfficeOfExitForms.formId))
    else Json.toJson(cacheMap.getEntry[OfficeOfExitStandard](OfficeOfExitForms.formId))

  protected case class FormattedData(lrn: Option[String], ducr: Option[String], payload: String)
}
