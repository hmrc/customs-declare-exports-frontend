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

package views.helpers

import forms.section1.AdditionalDeclarationType._
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import javax.inject.Singleton

@Singleton
class AdditionalDeclarationTypeHelper {

  def radioGroupErrors(form: Form[_])(implicit request: JourneyRequest[_]): Seq[FormError] =
    ErrorMapper.radioGroupErrors("additionalDeclarationType", if (request.isType(SUPPLEMENTARY)) "simplified" else "prelodged", form.errors)

  def radioItems(form: Form[_])(implicit messages: Messages, request: JourneyRequest[_]): Seq[RadioItem] =
    request.declarationType match {
      case STANDARD      => List(radioItem(form, STANDARD_FRONTIER), radioItem(form, STANDARD_PRE_LODGED))
      case SUPPLEMENTARY => List(radioItemForSuppl(form, SUPPLEMENTARY_SIMPLIFIED), radioItemForSuppl(form, SUPPLEMENTARY_EIDR))
      case SIMPLIFIED    => List(radioItem(form, SIMPLIFIED_FRONTIER), radioItem(form, SIMPLIFIED_PRE_LODGED))
      case OCCASIONAL    => List(radioItem(form, OCCASIONAL_FRONTIER), radioItem(form, OCCASIONAL_PRE_LODGED))
      case CLEARANCE     => List(radioItem(form, CLEARANCE_FRONTIER), radioItem(form, CLEARANCE_PRE_LODGED))
    }

  private def radioItem(form: Form[_], adt: AdditionalDeclarationType)(implicit messages: Messages): RadioItem = {
    val adtType = if (isArrived(adt)) "arrived" else "prelodged"

    RadioItem(
      id = Some(adtType),
      value = Some(adt.toString),
      content = Text(messages(s"declaration.declarationType.radio.$adtType", adt.toString)),
      checked = form("additionalDeclarationType").value.contains(adt.toString)
    )
  }

  private def radioItemForSuppl(form: Form[_], adt: AdditionalDeclarationType)(implicit messages: Messages): RadioItem = {
    val adtType = if (adt == SUPPLEMENTARY_SIMPLIFIED) "simplified" else "eidr"

    RadioItem(
      id = Some(adtType),
      value = Some(adt.toString),
      content = Text(messages(s"declaration.declarationType.radio.supplementary.$adtType")),
      checked = form("additionalDeclarationType").value.contains(adt.toString)
    )
  }
}
