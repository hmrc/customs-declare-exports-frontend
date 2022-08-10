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
import org.scalatest.BeforeAndAfterEach
import play.twirl.api.Html
import tools.Stubs
import controllers.routes
import models.SignOutReason.UserAction
import views.declaration.spec.UnitViewSpec
import views.html.unauthorised
import views.tags.ViewTest

@ViewTest
class UnauthorisedViewSpec extends UnitViewSpec with Stubs with Injector with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    super.beforeEach

  val unauthorisedPage = instanceOf[unauthorised]

  val view: Html = unauthorisedPage(true)(request, messages)

  "Unauthorised Page view" when {

    "the user has insufficient enrollments" should {

      "display the expected page header" in {
        view.getElementsByTag("h1").first must containMessage("unauthorised.heading")
      }

      "display the inset text" in {
        val insets = view.getElementsByClass("govuk-inset-text").get(0)
        insets.children.first.text mustBe messages("unauthorised.insetText")
      }

      "display the expected links" in {
        val link = view.getElementById("main-content").getElementsByClass("govuk-link")

        link.get(0) must containMessage("unauthorised.paragraph.1.linkText")
        link.get(0) must haveHref("https://www.gov.uk/guidance/get-access-to-the-customs-declaration-service")

        link.get(1) must containMessage("unauthorised.paragraph.2.linkText")
        link.get(1) must haveHref("https://www.gov.uk/log-in-register-hmrc-online-services/problems-signing-in")

        link.get(2) must containMessage("unauthorised.paragraph.3.linkText")
        link.get(2) must haveHref(
          "https://www.gov.uk/government/publications/use-hmrcs-business-tax-account/use-hmrcs-business-tax-account#adding-a-team-member"
        )
      }

      "display the expected sign out link" in {
        val link = view.getElementsByClass("hmrc-sign-out-nav__link").first()

        link must haveHref(routes.SignOutController.signOut(UserAction))
      }

    }

  }

}
