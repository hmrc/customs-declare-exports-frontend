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

import forms.mappings.CrossFieldFormatter.CrossFieldConstraint
import play.api.data.FormError
import play.api.data.format.Formatter

object CrossFieldFormatter {
  type CrossFieldConstraint = (String, String) => Boolean
}

case class CrossFieldFormatter(secondaryKey: String, constraints: Seq[(String, CrossFieldConstraint)]) extends Formatter[String] {
  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
    val firstKeyValue = data.getOrElse(key, "")
    val secondaryKeyValue = data.getOrElse(secondaryKey, "")

    val formErrors = constraints.foldLeft(Seq.empty[FormError]) { (errors, verification) =>
      if (verification._2(firstKeyValue, secondaryKeyValue)) errors
      else errors :+ FormError(key, verification._1)
    }

    if (formErrors.isEmpty) Right(firstKeyValue)
    else Left(formErrors)
  }

  override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
}
