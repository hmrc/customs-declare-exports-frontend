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

package views.helpers

import views.common.UnitViewSpec
import views.helpers.EadHelper.declarationTypes

class EadHelperSpec extends UnitViewSpec {

  private val messagesValidated = messages(request)

  private def messageKey(decType: String) = s"ead.template.declarationType.$decType"

  "EadHelper" should {

    "translate all declaration types" in {
      declarationTypes.foreach(decType => EadHelper.translate(decType) mustBe messages(messageKey(decType)))
    }

    "have message for all declaration types" in {
      declarationTypes.foreach(decType => messagesValidated(messageKey(decType)))
    }

    "return input value for unknown type" in {
      EadHelper.translate("ABC") mustBe "ABC"
    }
  }
}
