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
import org.mockito.Mockito.{reset, when}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.inject.bind
import play.twirl.api.{Html, HtmlFormat}
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.unauthorised
import views.tags.ViewTest

@ViewTest
class UnauthorisedViewSpec extends UnitViewSpec with Stubs with Injector with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockTdrUnauthorisedMsgConfig)
  }

  val injector = new OverridableInjector(bind[TdrUnauthorisedMsgConfig].toInstance(mockTdrUnauthorisedMsgConfig))
  val unauthorisedPage = injector.instanceOf[unauthorised]

  "Unauthorised Page view" when {

    "TdrUnauthorisedMessage is Disabled and the user has insufficient enrollments" should {
      val view: Html = getUnauthorisedPageView(false)

      "display the expected page header" in {
        view.getElementsByTag("h1").first must containMessage("unauthorised.heading")
      }

      "display the expected get EORI link" in {
        val link = view.getElementById("get_eori_link")

        link must containMessage("unauthorised.paragraph.1.bullet.1.link")
        link must haveHref("https://www.gov.uk/eori")
        link.attr("target") mustBe "_self"
      }

      "display the expected access CDS link" in {
        val link = view.getElementById("access_cds_link")

        link must containMessage("unauthorised.paragraph.1.bullet.2.link")
        link must haveHref("https://www.gov.uk/guidance/get-access-to-the-customs-declaration-service")
        link.attr("target") mustBe "_self"
      }

      "display the expected check CDS application status link" in {
        val link = view.getElementById("check_cds_application_status_link")

        link must containMessage("unauthorised.paragraph.2.link")
        link must haveHref("https://www.tax.service.gov.uk/customs/register-for-cds/are-you-based-in-uk")
        link.attr("target") mustBe "_self"
      }
    }

    "TdrUnauthorisedMessage is Disabled and the user's EORI in not in the allow list" should {
      val view: Html = getUnauthorisedPageView(false, true)

      "display the expected page header" in {
        view.getElementsByTag("h1").first must containMessage("unauthorised.heading.eori.not.allowed")
      }

      "display the expected contact email address link" in {
        checkContactEmailAddress(view)
      }
    }

    "TdrUnauthorisedMessage is Enabled" should {
      val view: Html = getUnauthorisedPageView(true)

      "display the expected page header" in {
        view.getElementsByTag("h1").first must containMessage("unauthorised.tdr.heading")
      }

      "display the expected contact email address link" in {
        checkContactEmailAddress(view)
      }
    }
  }

  def checkContactEmailAddress(view: Html): Assertion = {
    val link = view.getElementById("contact_support_link")

    link must containMessage("unauthorised.tdr.body.link")
    link must haveHref(s"mailto:${messages("unauthorised.tdr.body.link")}")
    link.attr("target") mustBe "_blank"
  }

  private def getUnauthorisedPageView(tdrEnabled: Boolean, unauthorizedDueToEoriNotAllowed: Boolean = false): HtmlFormat.Appendable = {
    when(mockTdrUnauthorisedMsgConfig.isTdrUnauthorisedMessageEnabled).thenReturn(tdrEnabled)
    unauthorisedPage(unauthorizedDueToEoriNotAllowed)(request, messages)
  }
}
