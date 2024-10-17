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

package views.general

import base.Injector
import controllers.routes.ChoiceController
import views.common.UnitViewSpec
import views.html.general.error_template
import views.tags.ViewTest

@ViewTest
class ErrorTemplateViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[error_template]

  private val view = page("global.error.title", messages("global.error.heading"), messages("global.error.message"))

  "error_template" should {

    "display the expected page header" in {
      view.getElementsByTag("h1").text mustBe messages("global.error.heading")
    }

    "display 'Back' button that links to 'Choice' page" in {
      val backButton = view.getElementById("back-link")

      backButton.text mustBe messages("site.backToSelectionPage")
      backButton must haveHref(ChoiceController.displayPage.url)
    }

    "display the expected paragraph" in {
      view.getElementsByClass("govuk-body-m").get(0).text mustBe messages("global.error.message")
    }
  }
}
