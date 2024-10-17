/*
 * Copyright 2024 HM Revenue & Customs
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

package views.summary

import base.Injector
import controllers.summary.routes.ConfirmationController
import org.scalatest.GivenWhenThen
import views.common.UnitViewSpec
import views.html.summary.holding_page
import views.tags.ViewTest

import scala.jdk.CollectionConverters.IteratorHasAsScala

@ViewTest
class HoldingViewSpec extends UnitViewSpec with GivenWhenThen with Injector {

  private val holdingPage = instanceOf[holding_page]

  private val confirmationPageUrl = ConfirmationController.displayConfirmationPage.url
  private val holdingPageUrl = ConfirmationController.displayHoldingPage.url

  private val expectedPollingTimeInSeconds = 5

  "Declaration Holder View" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = holdingPage(holdingPageUrl, holdingPageUrl, confirmationPageUrl)(request, messages)

      "display page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.confirmation.holding.title")
      }

      "display the expected paragraph body (h2)" in {
        val processing = messages("declaration.confirmation.holding.paragraph")
        view.getElementsByClass("govuk-label--m").get(0).text mustBe processing
      }

      "display the expected Spinner widget" in {
        view.getElementsByClass("ccms-loader").size mustBe 1
        view.getElementsByTag("script").iterator.asScala.toList.filter(_.text.contains("window.location.href"))
      }

      "not include a 'View Declaration Summary' link" in {
        Option(view.getElementById("view_declaration_summary")) mustBe None
      }

      "include the expected redirection (no)script when javascript is disabled" in {
        val content = s"$expectedPollingTimeInSeconds; url=$holdingPageUrl"
        val meta = s"""<meta http-equiv="refresh" content="$content">"""

        val noscripts = view.getElementsByTag("noscript").iterator.asScala.toList
        noscripts.count(_.child(0).toString == meta) mustBe 1
      }

      "include the expected redirection script when javascript is enabled" in {
        val allScripts = view.getElementsByTag("script").iterator.asScala.toList
        val scripts = allScripts.filter(_.toString.contains("window.location.href"))
        scripts.size mustBe 1

        val jsScript = scripts(0).toString
        assert(jsScript.contains(confirmationPageUrl))

        And("and which should include the expected 'polling' time")
        val pattern = s"(.*)[Tt]imeout[:(](.*)${expectedPollingTimeInSeconds * 1000}(.*)"
        val linesWithThePollingTime = jsScript.split("\n").filter(_.matches(pattern))
        linesWithThePollingTime.size mustBe 3
      }
    }
  }
}
