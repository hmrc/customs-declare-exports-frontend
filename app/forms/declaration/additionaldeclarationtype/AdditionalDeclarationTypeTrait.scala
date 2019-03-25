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

package forms.declaration.additionaldeclarationtype

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import utils.validators.forms.FieldValidator.isContainedIn

trait AdditionalDeclarationTypeTrait {
  def allowedValues: Set[String]

  val formMapping: Mapping[AdditionalDeclarationType] = Forms.single(
    "additionalDeclarationType" -> optional(
      text(maxLength = 1)
        .verifying("declaration.declarationType.inputText.error.incorrect", isContainedIn(allowedValues))
    ).verifying("declaration.declarationType.inputText.error.empty", _.isDefined)
      .transform[AdditionalDeclarationType](
      value => AdditionalDeclarationType(value.getOrElse("")),
      additionalDeclarationType => Some(additionalDeclarationType.additionalDeclarationType)
    )
  )

  val formId = "AdditionalDeclarationType"

  def form(): Form[AdditionalDeclarationType] = Form(formMapping)
}
