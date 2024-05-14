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

package views.cancellation

import base.ExportsTestData.mrn
import base.Injector
import controllers.routes.CancellationResultController
import views.declaration.spec.UnitViewSpec
import views.html.cancellation_holding
import views.tags.ViewTest
import org.jsoup.nodes.Document
import org.scalatest.GivenWhenThen

import scala.jdk.CollectionConverters.IteratorHasAsScala

@ViewTest
class CancellationHoldingViewSpec extends UnitViewSpec with Injector with GivenWhenThen {

  private val cancellationHoldingPage = instanceOf[cancellation_holding]

  private val confirmationPageUrl = CancellationResultController.displayResultPage.url
  private val holdingPageUrl = CancellationResultController.displayHoldingPage.url

  private val expectedPollingTimeInSeconds = 5

  private def createView(): Document =
    cancellationHoldingPage(holdingPageUrl, confirmationPageUrl, mrn)(request, messages)

  "Cancel DeclarationView on empty page" should {
    val view = createView()

    "display page title" in {
      view.getElementById("title").text() mustBe messages("cancellation.holding.title")
    }

    "display the 'MRN' hint" in {
      view.getElementsByClass("submission-mrn").first.text mustBe messages("mrn.heading", mrn)
    }

    "display paragraph" in {
      view.getElementsByClass("govuk-label--m").text mustBe (messages("cancellation.holding.paragraph"))
    }

    "display the expected Spinner widget" in {
      view.getElementsByClass("ccms-loader").size mustBe 1
      view.getElementsByTag("script").iterator.asScala.toList.filter(_.text.contains("window.location.href"))
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

      val jsScript = scripts.head.toString
      assert(jsScript.contains(confirmationPageUrl))

      And("and which should include the expected 'polling' time")
      val pattern = s"(.*)[Tt]imeout[:(](.*)${expectedPollingTimeInSeconds * 1000}(.*)"
      val linesWithThePollingTime = jsScript.split("\n").filter(_.matches(pattern))
      linesWithThePollingTime.length mustBe 3
    }
  }
}
