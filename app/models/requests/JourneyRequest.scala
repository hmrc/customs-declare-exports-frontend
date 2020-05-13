/*
 * Copyright 2020 HM Revenue & Customs
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

package models.requests

import models.DeclarationType.DeclarationType
import models.ExportsDeclaration

class JourneyRequest[+A](val authenticatedRequest: AuthenticatedRequest[A], val cacheModel: ExportsDeclaration)
    extends AuthenticatedRequest[A](authenticatedRequest, authenticatedRequest.user) {
  val declarationType: DeclarationType = cacheModel.`type`
  val sourceDecId: Option[String] = cacheModel.sourceId
  def isType(`type`: DeclarationType*): Boolean = `type`.contains(declarationType)
  def eori: String = authenticatedRequest.user.eori

  def isDeclarantExporter: Boolean = cacheModel.parties.declarantIsExporter.exists(_.isExporter)
}
