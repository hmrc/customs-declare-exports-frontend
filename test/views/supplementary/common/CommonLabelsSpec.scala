/*
 * Copyright 2019 HM Revenue & Customs
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

package views.supplementary.common
import views.helpers.ViewSpec
import views.tags.ViewTest

@ViewTest
class CommonLabelsSpec extends ViewSpec {

  "Common labels on pages" should {

    "include Back" in {
      assertMessage("site.back", "Back")
    }

    "include Remove" in {
      assertMessage("site.remove", "Remove")
    }

    "include Add" in {
      assertMessage("site.add", "Add")
    }

    "include Save and continue" in {
      assertMessage("site.save_and_continue", "Save and continue")
    }
  }

  "Common error labels on pages" should {

    "include Error title" in {
      assertMessage("error.summary.title", "There’s been a problem")
    }

    "include Error text" in {
      assertMessage("error.summary.text", "Check the following")
    }
  }
}
