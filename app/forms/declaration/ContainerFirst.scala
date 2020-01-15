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
import forms.declaration.ContainerAdd.maxContainerIdLength
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class ContainerFirst(id: Option[String])

object ContainerFirst extends DeclarationPage {
  implicit val format = Json.format[ContainerFirst]

  val hasContainerKey = "hasContainer"
  val containerIdKey = "id"

  object HasContainerAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import HasContainerAnswers._

  private def form2Model: (String, Option[String]) => ContainerFirst = {
    case (hasContainer, containerId) =>
      hasContainer match {
        case HasContainerAnswers.yes => ContainerFirst(containerId)
        case HasContainerAnswers.no  => ContainerFirst(None)
      }
  }

  private def model2Form: ContainerFirst => Option[(String, Option[String])] =
    model =>
      model.id match {
        case Some(id) => Some((yes, Some(id)))
        case None     => Some((no, None))
    }

  val mapping = Forms.mapping(
    hasContainerKey -> requiredRadio("error.yesNo.required"),
    containerIdKey -> mandatoryIfEqual(
      hasContainerKey,
      yes,
      text()
        .verifying("declaration.transportInformation.containerId.empty", nonEmpty)
        .verifying("declaration.transportInformation.containerId.error.invalid", isAlphanumeric and noLongerThan(maxContainerIdLength))
    )
  )(form2Model)(model2Form)

  def form(): Form[ContainerFirst] = Form(mapping)
}
