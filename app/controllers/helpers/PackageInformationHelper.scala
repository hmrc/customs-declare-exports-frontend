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

package controllers.helpers

import forms.declaration.PackageInformation
import models.requests.JourneyRequest

object PackageInformationHelper {

  def allCachedPackageInformation(itemId: String)(implicit request: JourneyRequest[_]): Seq[PackageInformation] =
    maybePackageInformation(itemId).getOrElse(List.empty)

  def hasCachedPackageInformation(itemId: String)(implicit request: JourneyRequest[_]): Boolean =
    allCachedPackageInformation(itemId).nonEmpty

  def singleCachedPackageInformation(id: String, itemId: String)(implicit request: JourneyRequest[_]): Option[PackageInformation] =
    maybePackageInformation(itemId).flatMap(_.find(_.id == id))

  private def maybePackageInformation(itemId: String)(implicit request: JourneyRequest[_]): Option[List[PackageInformation]] =
    request.cacheModel.itemBy(itemId).flatMap(_.packageInformation)
}
