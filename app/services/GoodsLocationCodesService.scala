/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.CodeListConnector
import models.codes.GoodsLocationCode
import play.api.i18n.Messages

import javax.inject.Inject

class GoodsLocationCodesService @Inject() (codeListConnector: CodeListConnector) {

  def all(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.allGoodsLocationCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def depCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getDepCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def airportsCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getAirportsCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def coaAirportsCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getCoaAirportsCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def maritimeAndWharvesCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getMaritimeAndWharvesCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def itsfCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getItsfCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def remoteItsfCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getRemoteItsfCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def externalItsfCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getExternalItsfCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def borderInspectionPostsCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getBorderInspectionPostsCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def approvedDipositoriesCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getApprovedDipositoriesCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def placeNamesGBCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getPlaceNamesGBCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def otherLocationCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getOtherLocationCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def cseCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getCseCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def railCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getRailCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def actsCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getActsCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def roroCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getRoroCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

  def gvmsCodes(implicit messages: Messages): List[GoodsLocationCode] =
    codeListConnector.getGvmsCodes(messages.lang.toLocale).values.toList.sortBy(_.description)

}
