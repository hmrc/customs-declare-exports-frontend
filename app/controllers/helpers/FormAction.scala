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

import play.api.mvc.{AnyContent, Request}

sealed trait FormAction {
  def label: String = this.getClass.getSimpleName.replace("$", "")
}

object FormAction {
  private val addLabel = "Add"
  private val addFieldLabel = "AddField"
  private val saveAndContinueLabel = "SaveAndContinue"
  private val saveAndReturnToSummaryLabel = "SaveAndReturnToSummary"
  private val saveAndReturnToErrorsLabel = "SaveAndReturnToErrors"
  private val continueLabel = "Continue"
  private val removeLabel = "Remove"

  def bindFromRequest()(implicit request: Request[AnyContent]): FormAction =
    request.body.asFormUrlEncoded.flatMap { body =>
      body.flatMap {
        case (`addFieldLabel`, values)          => Some(AddField(values.headOption.getOrElse("")))
        case (`addLabel`, _)                    => Some(Add)
        case (`saveAndContinueLabel`, _)        => Some(SaveAndContinue)
        case (`saveAndReturnToSummaryLabel`, _) => Some(SaveAndReturnToSummary)
        case (`saveAndReturnToErrorsLabel`, _)  => Some(SaveAndReturnToErrors)
        case (`continueLabel`, _)               => Some(Continue)
        case (`removeLabel`, values)            => Some(Remove(values))
        case _                                  => None
      }.headOption
    }.getOrElse(Unknown)
}

case object Add extends FormAction
case object Unknown extends FormAction
case object SaveAndContinue extends FormAction
case object SaveAndReturnToSummary extends FormAction
case object SaveAndReturnToErrors extends FormAction
case object Continue extends FormAction
case class Remove(keys: Seq[String]) extends FormAction
case class AddField(field: String) extends FormAction
