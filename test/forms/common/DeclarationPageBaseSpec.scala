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

package forms.common

import forms.DeclarationPage
import models.DeclarationType.{DeclarationType, _}
import models.viewmodels.TariffContentKey
import models.DeclarationType
import org.scalatest.{MustMatchers, WordSpec}

trait DeclarationPageBaseSpec extends WordSpec with MustMatchers {
  val commonKeyDeclarationTypes = Seq(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL)

  def testTariffContentKeys(page: DeclarationPage, messageKey: String) =
    "defineTariffContentKeys is called" which {
      "is passed a specific DeclarationType" should {
        "return the correct collection of TariffContentKeys" in {

          for (decType: DeclarationType <- commonKeyDeclarationTypes) {
            page.defineTariffContentKeys(decType) mustBe getCommonTariffKeys(messageKey)
          }

          page.defineTariffContentKeys(CLEARANCE) mustBe getClearanceTariffKeys(messageKey)
        }
      }
    }

  def testTariffContentKeysNoSpecialisation(
    page: DeclarationPage,
    messageKey: String,
    getKeys: (String) => Seq[TariffContentKey] = getCommonTariffKeys
  ) =
    "defineTariffContentKeys is called" which {
      "is passed any DeclarationType" should {
        "always return the same collection of TariffContentKeys" in {
          for (decType: DeclarationType <- DeclarationType.values) {
            page.defineTariffContentKeys(decType) mustBe getKeys(messageKey)
          }
        }
      }
    }

  def getCommonTariffKeys(messageKey: String) = Seq(TariffContentKey(s"$messageKey.common"))

  def getClearanceTariffKeys(messageKey: String) = Seq(TariffContentKey(s"$messageKey.clearance"))
}
