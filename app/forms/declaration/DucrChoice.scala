/*
 * Copyright 2022 HM Revenue & Customs
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
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey

object DucrChoice extends DeclarationPage {

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        List(
          TariffContentKey("tariff.declaration.ducrChoice.1.clearance"),
          TariffContentKey("tariff.declaration.ducrChoice.2.clearance"),
          TariffContentKey("tariff.declaration.ducrChoice.3.clearance")
        )
      case _ => List(TariffContentKey("tariff.declaration.ducr.1.common"))
    }
}
