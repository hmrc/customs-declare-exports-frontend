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

package views.guidance

import scala.collection.JavaConverters.asScalaIteratorConverter

import base.Injector
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.guidance.error_explanation
import views.tags.ViewTest

@ViewTest
class ErrorExplanationViewSpec extends UnitViewSpec with Stubs with Injector {

  private val errorExplanationPage = instanceOf[error_explanation]
  private val view = errorExplanationPage()(request, messages)

  "'Error Explanation' view" should {

    "display page header" in {
      view.getElementsByTag("h1").first must containMessage("guidance.error.explanation.title")
    }

    "display the expected links" in {
      val table = view.getElementsByTag("table").get(0)
      val links = table.getElementsByTag("a")
      links.size mustBe 3

      val expectedHref = "https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide"
      links.iterator.asScala.toList.foreach(_ must haveHref(expectedHref))
    }
  }
}
