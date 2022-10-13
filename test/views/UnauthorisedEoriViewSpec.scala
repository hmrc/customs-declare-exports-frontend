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

package views

import base.Injector
import views.declaration.spec.UnitViewSpec
import views.html.unauthorisedEori
import views.tags.ViewTest

@ViewTest
class UnauthorisedEoriViewSpec extends UnitViewSpec with Injector {

  val page = instanceOf[unauthorisedEori]

  val view = page()(request, messages)

  "UnauthorisedEori Page view" when {

    "display the expected page header" in {
      view.getElementsByTag("h1").first must containMessage("unauthorised.tdr.heading")
    }

    "display the expected contact email address link" in {
      val link = view.getElementById("contact_support_link")

      link must containMessage("unauthorised.tdr.body.link")
      link must haveHref(s"mailto:${messages("unauthorised.tdr.body.link")}")
      link.attr("target") mustBe "_blank"
    }
  }
}
