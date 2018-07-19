/*
 * Copyright 2018 HM Revenue & Customs
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

package utils

import forms.{ConsignmentData, NameAndAddress, OwnDescriptionData}
import uk.gov.hmrc.http.cache.client.CacheMap
import identifiers._
import models._

class UserAnswers(val cacheMap: CacheMap) extends Enumerable.Implicits {
  def nameAndAddress: Option[NameAndAddress] = cacheMap.getEntry[NameAndAddress](NameAndAddressId.toString)

  def enterEORI: Option[String] = cacheMap.getEntry[String](EnterEORIId.toString)

  def haveRepresentative: Option[HaveRepresentative] = cacheMap.getEntry[HaveRepresentative](HaveRepresentativeId.toString)

  def ownDescription: Option[OwnDescriptionData] = cacheMap.getEntry[OwnDescriptionData](OwnDescriptionId.toString)

  def declarationForYourselfOrSomeoneElse: Option[DeclarationForYourselfOrSomeoneElse] = cacheMap.getEntry[DeclarationForYourselfOrSomeoneElse](DeclarationForYourselfOrSomeoneElseId.toString)

  def submitPage: Option[String] = cacheMap.getEntry[String](DeclarationSummaryId.toString)

  def consignment: Option[ConsignmentData] = cacheMap.getEntry[ConsignmentData](ConsignmentId.toString)

  def selectRole: Option[SelectRole] = cacheMap.getEntry[SelectRole](SelectRoleId.toString)
}
