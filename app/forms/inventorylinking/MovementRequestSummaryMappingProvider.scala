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

package forms.inventorylinking

import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.data.Mapping
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

//noinspection ConvertibleToMethodValue
object MovementRequestSummaryMappingProvider {

  def provideMappingForMovementSummaryPage(): Mapping[InventoryLinkingMovementRequest] = buildMapping()

  private val ucrTypeAllowedValues = Set("D", "M")
  private val masterOptAllowedValues = Set("A", "F", "R", "X")
  private val ucrValidationPattern =
    "[0-9][A-Z][A-Z][0-9A-Z\\(\\)\\-/]{6,32}|GB/[0-9A-Z]{3,4}-[0-9A-Z]{5,28}|GB/[0-9A-Z]{9,12}-[0-9A-Z]{1,23}|A:[0-9A-Z]{3}[0-9]{8}|C:[AZ]{3}[0-9A-Z]{3,30}"

  private val eoriMaxLength = 17
  private val agentLocationMaxLength = 12
  private val agentRoleMaxLength = 3

  private val ucrMaxLength = 35
  private val transportIDMaxLength = 35
  private val goodsLocationMaxLength = 17
  private val shedOPIDMaxLength = 3
  private val masterUCRMaxLength = 35
  private val movementReferenceMaxLength = 25

  private val agentDetailsMapping = mapping(
    "EORI" -> optional(text(maxLength = eoriMaxLength)),
    "agentLocation" -> optional(text(maxLength = agentLocationMaxLength)),
    "agentRole" -> optional(text(maxLength = agentRoleMaxLength))
  )(AgentDetails.apply)(AgentDetails.unapply)

  private val ucrBlockMapping = mapping(
    "ucr" -> nonEmptyText(maxLength = ucrMaxLength)
      .verifying("Please, provide valid UCR", _.matches(ucrValidationPattern)),
    "ucrType" -> nonEmptyText(maxLength = 1)
      .verifying("Allowed values are: \"D\", \"M\"", ucrTypeAllowedValues.contains(_))
  )(UcrBlock.apply)(UcrBlock.unapply)

  private val transportDetailsMapping = mapping(
    "transportID" -> optional(text(maxLength = transportIDMaxLength)),
    "transportMode" -> optional(text(maxLength = 1)),
    "transportNationality" -> optional(text(maxLength = 2))
  )(TransportDetails.apply)(TransportDetails.unapply)

  private def buildMapping(): Mapping[InventoryLinkingMovementRequest] =
    mapping(
      "messageCode" -> nonEmptyText,
      "agentDetails" -> optional(agentDetailsMapping),
      "ucrBlock" -> ucrBlockMapping,
      "goodsLocation" -> nonEmptyText(maxLength = goodsLocationMaxLength),
      "goodsArrivalDateTime" -> optional(text()),
      "goodsDepartureDateTime" -> optional(text()),
      "shedOPID" -> optional(text(maxLength = shedOPIDMaxLength)),
      "masterUCR" -> optional(
        text(maxLength = masterUCRMaxLength)
          .verifying("Please, provide valid UCR", ucr => ucr.matches(ucrValidationPattern))
      ),
      "masterOpt" -> optional(
        text(maxLength = 1)
          .verifying("Allowed values are: \"A\", \"F\", \"R\", \"X\"", s => masterOptAllowedValues.contains(s))
      ),
      "movementReference" -> optional(text(maxLength = movementReferenceMaxLength)),
      "transportDetails" -> optional(transportDetailsMapping)
    )(InventoryLinkingMovementRequest.apply)(InventoryLinkingMovementRequest.unapply)

}
