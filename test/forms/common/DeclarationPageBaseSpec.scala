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

package forms.common

import forms.DeclarationPage
import models.DeclarationType.{DeclarationType, _}
import models.viewmodels.TariffContentKey
import models.DeclarationType
import base.UnitSpec

trait DeclarationPageBaseSpec extends UnitSpec {

  private val commonKeyDeclarationTypes = List(STANDARD, SIMPLIFIED, OCCASIONAL)

  def testTariffContentKeys(page: DeclarationPage, messageKey: String): Unit =
    "defineTariffContentKeys is called" which {
      "is passed a specific DeclarationType" should {
        "return the correct collection of TariffContentKeys" in {

          for (decType: DeclarationType <- commonKeyDeclarationTypes)
            page.defineTariffContentKeys(decType) mustBe getCommonTariffKeys(messageKey)

          page.defineTariffContentKeys(SUPPLEMENTARY) mustBe getSupplementaryTariffKeys(messageKey)
          page.defineTariffContentKeys(CLEARANCE) mustBe getClearanceTariffKeys(messageKey)
        }
      }
    }

  def testTariffContentKeysNoSpecialisation(
    page: DeclarationPage,
    messageKey: String,
    getKeys: (String) => Seq[TariffContentKey] = getCommonTariffKeys
  ): Unit =
    "defineTariffContentKeys is called" which {
      "is passed any DeclarationType" should {
        "always return the same collection of TariffContentKeys" in {
          for (decType: DeclarationType <- DeclarationType.values)
            page.defineTariffContentKeys(decType) mustBe getKeys(messageKey)
        }
      }
    }

  def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] = List(TariffContentKey(s"$messageKey.common"))

  def getSupplementaryTariffKeys(messageKey: String): Seq[TariffContentKey] = getCommonTariffKeys(messageKey)

  def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] = List(TariffContentKey(s"$messageKey.clearance"))
}
