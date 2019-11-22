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
import forms.DeclarationPage
import forms.Mapping.requiredRadio
import forms.declaration.ContainerAdd.maxContainerIdLength
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator.{isAlphanumeric, noLongerThan, nonEmpty}

case class ContainerYesNo(id: Option[String])

object ContainerYesNo extends DeclarationPage {
  implicit val format = Json.format[ContainerYesNo]

  val hasContainerKey = "hasContainer"
  val containerIdKey = "id"

  object HasContainerAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import HasContainerAnswers._

  private def form2Model: (String, Option[String]) => ContainerYesNo = {
    case (hasContainer, containerId) =>
      hasContainer match {
        case HasContainerAnswers.yes => ContainerYesNo(containerId)
        case HasContainerAnswers.no  => ContainerYesNo(None)
      }
  }

  private def model2Form: ContainerYesNo => Option[(String, Option[String])] =
    model =>
      model.id match {
        case Some(id) => Some((yes, Some(id)))
        case None       => Some((no, None))
    }

  val mapping = Forms.mapping(
    hasContainerKey -> requiredRadio("error.yesNo.required"),
    containerIdKey -> mandatoryIfEqual(
      hasContainerKey,
      yes,
      text()
        .verifying("declaration.transportInfo.containerId.empty", nonEmpty)
        .verifying("declaration.transportInfo.containerId.error.alphanumeric", isAlphanumeric)
        .verifying("declaration.transportInfo.containerId.error.length", noLongerThan(maxContainerIdLength))
    )
  )(form2Model)(model2Form)

  def form(): Form[ContainerYesNo] = Form(mapping)
}
