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

package forms.drafts

import play.api.data.Forms.{boolean, optional}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}

case class RemoveDraftDeclaration(remove: Boolean)

object RemoveDraftDeclaration {
  implicit val format: OFormat[RemoveDraftDeclaration] = Json.format[RemoveDraftDeclaration]

  val formMapping: Mapping[RemoveDraftDeclaration] = Forms.mapping(
    "remove" -> optional(boolean)
      .verifying("draft.declarations.remove.option.error.empty", _.isDefined)
      .transform(_.get, (b: Boolean) => Some(b))
  )(RemoveDraftDeclaration.apply)(RemoveDraftDeclaration.unapply)

  def form: Form[RemoveDraftDeclaration] = Form(formMapping)
}
