/*
 * Copyright 2021 HM Revenue & Customs
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

package views.pdf

import views.declaration.spec.UnitViewSpec

class DeclarationTypeSpec extends UnitViewSpec {

  val messagesValidated = messages(request)

  private def messageKey(decType: String) = s"pdf.template.declarationType.$decType"

  "DeclarationType" should {

    "translate all declaration types" in {
      DeclarationType.values.map(_.toString).foreach(decType => DeclarationType.translate(decType) mustBe messages(messageKey(decType)))
    }

    "have message for all declaration types" in {
      DeclarationType.values.map(_.toString).foreach(decType => messagesValidated(messageKey(decType)))
    }

    "return input value for un-known type" in {
      DeclarationType.translate("ABC") mustBe "ABC"
    }

  }
}
