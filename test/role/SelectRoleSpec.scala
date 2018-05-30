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

package role

import acceptance.URLContext
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerSuite, OneServerPerSuite, PlaySpec}

class SelectRoleSpec extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with HtmlUnitFactory with URLContext {

  "Select Role" should {

    "Display a selectable radio button for each choice of role" in {

      go to url("/select-role")

      pageTitle mustBe "Select role"
    }
  }
}
