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

package forms.declaration
import models.DeclarationType
import models.DeclarationType.DeclarationType
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import utils.validators.forms.FieldValidator.isContainedIn

case class DeclarationChoice(value: DeclarationType)

object DeclarationChoice {

  val mapping = Forms.single(
    "type" ->
      text()
        .verifying("declaration.type.error", isContainedIn(DeclarationType.values.map(_.toString)))
        .transform[DeclarationChoice](
          choice => DeclarationChoice(DeclarationType.values.find(_.toString.equals(choice)).get),
          choice => choice.value.toString
        )
  )

  def form(): Form[DeclarationChoice] = Form(mapping)
}
