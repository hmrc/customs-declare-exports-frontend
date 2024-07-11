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

import play.api.data.validation.Constraint
import play.api.data.{FormError, Mapping}
import uk.gov.voa.play.form.Condition

case class ConditionalConstraint[T](shouldConstraintBeApplied: Condition, errorKey: String, constraint: (T => Boolean))

/**
 * A mapping that extends another mapping with additional constraints that are only added if their
 * conditions are satisfied.
 *
 * @param baseMappings the wrapped mapping that should always be applied.
 * @param conditionalAdditionalConstraints a Seq of additional constraints to be applied. Only the first constraints in
 *                                         the sequence who's condition is satisfied is added (the others are ignored).
 */
case class AdditionalConstraintsMapping[T](
  baseMappings: Mapping[T],
  conditionalAdditionalConstraints: Seq[ConditionalConstraint[T]],
  constraints: Seq[Constraint[T]] = Nil
) extends Mapping[T] {

  override val format: Option[(String, Seq[Any])] = baseMappings.format

  val key = baseMappings.key

  def verifying(addConstraints: Constraint[T]*): Mapping[T] =
    this.copy(constraints = constraints ++ addConstraints.toSeq)

  def bind(data: Map[String, String]): Either[Seq[FormError], T] =
    conditionalAdditionalConstraints
      .find(_.shouldConstraintBeApplied(data))
      .fold(baseMappings.bind(data)) { additionalCondition =>
        baseMappings.verifying(additionalCondition.errorKey, additionalCondition.constraint).bind(data)
      }

  def unbind(value: T): Map[String, String] = baseMappings.unbind(value)

  def unbindAndValidate(value: T): (Map[String, String], Seq[FormError]) = {
    val data = unbind(value)
    val validateResults = bind(data)

    (data, validateResults.left.toOption.getOrElse(Seq.empty))
  }

  def withPrefix(prefix: String): Mapping[T] = copy(baseMappings = baseMappings.withPrefix(prefix))

  val mappings: Seq[Mapping[_]] = baseMappings.mappings :+ this
}
