/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.data.validation.Constraint
import play.api.data.{FormError, Forms, Mapping}
import uk.gov.voa.play.form.Condition

case class ConditionalMapping[T](
  conditionalMapping: Mapping[Option[T]],
  defaultMapping: Mapping[Option[T]],
  condition: Condition,
  key: String = "",
  constraints: Seq[Constraint[Option[T]]] = Nil
) extends Mapping[Option[T]] {

  def verifying(addConstraints: Constraint[Option[T]]*): Mapping[Option[T]] =
    this.copy(constraints = constraints ++ addConstraints.toSeq)

  def bind(data: Map[String, String]): Either[Seq[FormError], Option[T]] = {
    val mapping = if (condition(data)) conditionalMapping else defaultMapping
    Forms.single(key -> mapping).bind(data)
  }

  def unbind(value: Option[T]): Map[String, String] = Map(key -> value.map(_.toString).getOrElse(""))

  def unbindAndValidate(value: Option[T]): (Map[String, String], Seq[FormError]) =
    this.unbind(value) -> collectErrors(value)

  def withPrefix(prefix: String): Mapping[Option[T]] =
    addPrefix(prefix).map(newKey => this.copy(key = newKey)).getOrElse(this)

  val mappings: Seq[Mapping[_]] = Seq(this)
}
