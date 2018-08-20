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

import controllers.routes
import identifiers._
import javax.inject.{Inject, Singleton}
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Results._
import play.api.mvc.{Call, Result}
import uk.gov.hmrc.http.cache.client.CacheMap

@Singleton
class Navigator @Inject()() {

  private val routeMap: Map[Identifier, UserAnswers => Call] = Map(
    ConsignmentId -> (_ => routes.OwnDescriptionController.onPageLoad()),
    OwnDescriptionId -> (_ => routes.WhoseDeclarationController.onPageLoad()),
    WhoseDeclarationId -> (_ => routes.HaveRepresentativeController.onPageLoad()),
    HaveRepresentativeId -> (_ => routes.EnterEORIController.onPageLoad()),
    EnterEORIId -> (_ => routes.RepresentativesAddressController.onPageLoad()),
    RepresentativesAddressId -> (_ => routes.DeclarationSummaryController.onPageLoad()),
    SubmitDeclarationId -> (_ => routes.DashboardController.onPageLoad())
  )

  private val editRouteMap: Map[Identifier, UserAnswers => Call] = Map(
    ConsignmentId -> (_ => routes.DashboardController.onPageLoad()),
    OwnDescriptionId -> (_ => routes.ConsignmentController.onPageLoad()),
    WhoseDeclarationId -> (_ => routes.OwnDescriptionController.onPageLoad()),
    HaveRepresentativeId -> (_ => routes.WhoseDeclarationController.onPageLoad()),
    EnterEORIId -> (_ => routes.HaveRepresentativeController.onPageLoad()),
    RepresentativesAddressId -> (_ => routes.EnterEORIController.onPageLoad())
  )

  private def nextPage(id: Identifier, mode: Mode): UserAnswers => Call = {
    mode match {
      case NormalMode =>
        routeMap.getOrElse(id, _ => routes.IndexController.onPageLoad())
      case CheckMode =>
        editRouteMap.getOrElse(id, _ => routes.DeclarationSummaryController.onPageLoad())
    }
  }

  def redirect(id: Identifier, mode: Mode, cache: CacheMap): Result =
    Redirect(nextPage(id, mode)(new UserAnswers(cache)))
}
