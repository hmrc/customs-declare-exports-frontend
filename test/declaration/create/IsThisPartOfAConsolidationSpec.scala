/*
 * Copyright 2018 HM Revenue & Customs
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

package declaration.create

import acceptance.URLContext
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerSuite, OneServerPerSuite, PlaySpec}
import im.mange.flakeless._
import org.openqa.selenium.{By, WebDriver, WebElement}

class IsThisPartOfAConsolidationSpec extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with HtmlUnitFactory with URLContext {

  "Is this part of a consolidation" should {

    "Enter DUCR when 'No' is selected" in {

      go to url("/mucr-ref-ducr")

      pageTitle mustBe "Full export declaration"

      Click(webDriver, By.id("is-consolidation-no"))

      SendKeys(webDriver, By.id("ducr"), "GB1234567890001410-0124-411018Aq")

      Click(webDriver, By.id("save-and-continue"))

      pageTitle mustBe "Full export declaration: Consolidation saved"

      AssertElementTextContains(webDriver, By.id("consolidation-saved"), "Consolidation saved. Id :")
    }
  }
}
