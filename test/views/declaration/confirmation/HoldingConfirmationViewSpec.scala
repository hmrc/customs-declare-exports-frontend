/*
 * Copyright 2021 HM Revenue & Customs
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

package views.declaration.confirmation

import scala.collection.JavaConverters.asScalaIteratorConverter

import base.Injector
import controllers.declaration.routes.ConfirmationController
import views.declaration.spec.UnitViewSpec
import views.html.declaration.confirmation.holding_confirmation_page
import views.tags.ViewTest

@ViewTest
class HoldingConfirmationViewSpec extends UnitViewSpec with Injector {

  private val holdingConfirmationPage = instanceOf[holding_confirmation_page]

  "Declaration Holder View" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = holdingConfirmationPage()(request, messages)

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

      "include the expected redirection (no)script when javascript is disabled" in {
        val meta = s"""<meta http-equiv="refresh" content="5; url=${ConfirmationController.displaySubmissionConfirmation.url}">"""
        val noscripts = view.getElementsByTag("noscript").iterator.asScala.toList
        noscripts.filter(_.child(0).toString == meta).size mustBe 1
      }

      "include the expected redirection script when javascript is enabled" in {
        val allScripts = view.getElementsByTag("script").iterator.asScala.toList
        val scripts = allScripts.filter(_.toString.contains("window.location.href"))
        scripts.size mustBe 1
        assert(scripts(0).toString.contains(ConfirmationController.displaySubmissionConfirmation.url))
      }
    }
  }
}
