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

package helpers.views.declaration.summary

trait PartiesMessages {

  private val parties: String = "supplementary.summary.parties"

  val header: String = parties + ".header"
  val exporterId: String = parties + ".exporterId"
  val exporterAddress: String = parties + ".exporterAddress"
  val declarantId: String = parties + ".declarantId"
  val declarantAddress: String = parties + ".declarantAddress"
  val representativeId: String = parties + ".representativeId"
  val representativeAddress: String = parties + ".representativeAddress"
  val representationType: String = parties + ".representationType"
  val idStatusNumberAuthorisationCode: String = parties + ".idStatusNumberAuthorisationCode"
  val authorizedPartyEori: String = parties + ".authorizedPartyEori"

  private val additionalParties: String = parties + ".additionalParties"

  val additionalPartiesHeader: String = additionalParties + ".header"
  val additionalPartiesId: String = additionalParties + ".id"
  val additionalPartiesType: String = additionalParties + ".type"
}
