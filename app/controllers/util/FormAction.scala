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

package controllers.util

sealed trait FormAction

object FormAction {
  private val addLabel = Add.toString
  private val saveAndContinueLabel = SaveAndContinue.toString
  private val removeLabel = Remove.toString

  def fromUrlEncoded(input: Map[String, Seq[String]]): Option[FormAction] =
    input.flatMap {
      case (`addLabel`, _)             => Some(Add)
      case (`saveAndContinueLabel`, _) => Some(SaveAndContinue)
      case (`removeLabel`, values)     => Some(Remove(values))
      case _                           => None
    }.headOption
}

case object Add extends FormAction
case object SaveAndContinue extends FormAction

case class Remove(keys: Seq[String]) extends FormAction
