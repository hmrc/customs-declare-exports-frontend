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

package forms.section5

import forms.DeclarationPage
import forms.common.YesNoAnswer
import forms.section4.NatureOfTransaction
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Form
import play.api.libs.json.{Json, OFormat}

object IsLicenceRequired extends DeclarationPage {
  implicit val format: OFormat[NatureOfTransaction] = Json.format[NatureOfTransaction]

  val form: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.item.isLicenceRequired.error")

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.isLicenceRequired.common"))
}
