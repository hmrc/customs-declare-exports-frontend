/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.DeclarationPage
import forms.Mapping.requiredRadio
import forms.common.YesNoAnswer
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class RepresentativeAgent(representingAgent: String)

object RepresentativeAgent extends DeclarationPage {
  implicit val format = Json.format[RepresentativeAgent]

  val formId = "RepresentingAgent"

  val mapping = Forms
    .mapping(
      "representingAgent" -> requiredRadio("error.yesNo.required").verifying("error.yesNo.required", isContainedIn(YesNoAnswer.allowedValues))
    )(RepresentativeAgent.apply)(RepresentativeAgent.unapply)

  def form(): Form[RepresentativeAgent] = Form(mapping)

}
