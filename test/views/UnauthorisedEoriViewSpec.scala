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

import base.{Injector, OverridableInjector}
import config.featureFlags.TdrUnauthorisedMsgConfig
import controllers.routes
import models.SignOutReason.UserAction
import org.mockito.Mockito.{reset, when}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.inject.bind
import play.twirl.api.Html
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.unauthorisedEori
import views.tags.ViewTest

@ViewTest
class UnauthorisedEoriViewSpec extends UnitViewSpec with Stubs with Injector with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockTdrUnauthorisedMsgConfig)
  }

  val injector = new OverridableInjector(bind[TdrUnauthorisedMsgConfig].toInstance(mockTdrUnauthorisedMsgConfig))
  val unauthorisedEoriPage = injector.instanceOf[unauthorisedEori]

  val view: Html = unauthorisedEoriPage(true)(request, messages)

  "UnauthorisedEori Page view" when {

    "display the expected page header" in {
      view.getElementsByTag("h1").first must containMessage("unauthorised.tdr.heading")
    }

    "display the expected contact email address link" in {
      checkContactEmailAddress(view)
    }

    "display the expected sign out link" in {
      val link = view.getElementsByClass("hmrc-sign-out-nav__link").first()

      link must haveHref(routes.SignOutController.signOut(UserAction))
    }

  }

  def checkContactEmailAddress(view: Html): Assertion = {
    val link = view.getElementById("contact_support_link")

    link must containMessage("unauthorised.tdr.body.link")
    link must haveHref(s"mailto:${messages("unauthorised.tdr.body.link")}")
    link.attr("target") mustBe "_blank"
  }

}
