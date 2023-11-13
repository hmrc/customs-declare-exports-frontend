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

package services

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.Ducr
import models.declaration.DeclarationStatus.DeclarationStatus
import models.ExportsDeclaration
import play.api.libs.json.{JsObject, Json}
import services.audit.AuditTypes.CreateDraftDeclatation
import services.audit.{AuditService, EventData}
import uk.gov.hmrc.http.HeaderCarrier

trait AuditCreateDraftDec {
  def audit(
    eori: String,
    newDecId: String,
    additionalDecType: Option[AdditionalDeclarationType],
    ducr: Option[Ducr],
    newDecStatus: DeclarationStatus,
    srcDecId: Option[String],
    auditService: AuditService
  )(implicit hc: HeaderCarrier): Unit = {
    val auditData: Map[String, String] = Map(
      EventData.eori.toString -> eori,
      EventData.decType.toString -> additionalDecType.map(_.toString).getOrElse(""),
      EventData.ducr.toString -> ducr.map(_.ducr).getOrElse(""),
      EventData.declarationStatus.toString -> newDecStatus.toString,
      EventData.declarationId.toString -> newDecId
    )

    val optionalKeyValues = Seq(
      (EventData.decType.toString, additionalDecType.map(_.toString)),
      (EventData.ducr.toString, ducr.map(_.ducr)),
      (EventData.parentDeclarationId.toString, srcDecId)
    )

    val optionalDataElements = optionalKeyValues.foldLeft(Map.empty[String, String]) { (acc, item) =>
      item._2.map(v => acc + (item._1 -> v)).getOrElse(acc)
    }

    val auditPayload = Json.toJson(auditData ++ optionalDataElements).as[JsObject]

    auditService.auditDraftDecCreated(CreateDraftDeclatation, auditPayload)
  }

  def audit(eori: String, declaration: ExportsDeclaration, auditService: AuditService)(implicit hc: HeaderCarrier): Unit =
    audit(
      eori,
      declaration.id,
      declaration.additionalDeclarationType,
      declaration.ducr,
      declaration.declarationMeta.status,
      declaration.declarationMeta.parentDeclarationId,
      auditService
    )
}
