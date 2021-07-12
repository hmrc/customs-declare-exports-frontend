/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.declaration

import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import play.api.mvc.Call

trait AdditionalActorsController {
  def nextPage(implicit request: JourneyRequest[_]): Mode => Call =
    if (request.declarationType == DeclarationType.SIMPLIFIED) routes.DeclarationHolderAddController.displayPage
    else routes.AuthorisationProcedureCodeChoiceController.displayPage
}
