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

package forms.mappings

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError}

object MappingHelper {

  def requiredRadioWithArgs(requiredKey: String = "error.required", args: Seq[Any]): FieldMapping[String] =
    of(radioFormatter(requiredKey, List.empty, args))

  def requiredRadio(requiredKey: String = "error.required"): FieldMapping[String] =
    of(radioFormatter(requiredKey, List.empty))

  def requiredRadio(requiredKey: String, choices: Seq[String]): FieldMapping[String] =
    of(radioFormatter(requiredKey, choices))

  def optionalRadio(requiredKey: String = "error.required", choices: Seq[String]): FieldMapping[String] =
    of(optionalRadioFormatter(requiredKey, NoneOfTheAbove.value +: choices))

  private def optionalRadioFormatter(requiredKey: String, allowedKeys: Seq[String]): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case Some(s) if !allowedKeys.contains(s) => Left(List(FormError(key, requiredKey)))
        case s                                   => Right(s.getOrElse(""))
      }
    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  private def radioFormatter(requiredKey: String, choices: Seq[String], args: Seq[Any] = List.empty): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case Some("")                       => Left(List(FormError(key, requiredKey, args)))
        case Some(s) if choices.isEmpty     => Right(s)
        case Some(s) if choices.contains(s) => Right(s)
        case _                              => Left(List(FormError(key, requiredKey, args)))
      }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }
}

case object NoneOfTheAbove {
  val value: String = "no"
}
