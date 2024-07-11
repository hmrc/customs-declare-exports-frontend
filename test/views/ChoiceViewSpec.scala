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

package views

import base.OverridableInjector
import config.ExternalServicesConfig
import controllers.journey.routes.StandardOrOtherJourneyController
import controllers.routes.{FileUploadController, SavedDeclarationsController}
import org.mockito.Mockito.when
import play.api.inject.bind
import views.dashboard.DashboardHelper.toDashboard
import views.helpers.CommonMessages
import views.html.choice_page
import views.common.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with CommonMessages {

  val movementsUrl = "customsMovementsFrontendUrl"
  val externalServicesConfig = mock[ExternalServicesConfig]
  when(externalServicesConfig.customsMovementsFrontendUrl).thenReturn(movementsUrl)

  private val injector = new OverridableInjector(bind[ExternalServicesConfig].toInstance(externalServicesConfig))

  private val choicePage = injector.instanceOf[choice_page]

  private val view = choicePage()(request, messages)

  "Choice page" should {

    "display on banner the expected 'service name' (common to all pages)" in {
      val element = view.getElementsByClass("hmrc-header__service-name").first
      element.tagName mustBe "a"
      element.text mustBe messages("service.name")
    }

    "display same page title as header" in {
      view.title must include(view.getElementsByTag("h1").text)
    }

    "display the expected heading" in {
      view.getElementById("title").text mustBe messages("declaration.choice.heading")
    }

    "display the expected hint text" in {
      view.getElementsByClass("govuk-hint").first.text mustBe messages("declaration.choice.hint")
    }

    "display the expected option links" in {
      val options = view.getElementsByClass("govuk-link--no-visited-state")
      options.size mustBe 5

      val createDeclaration = options.get(0)
      createDeclaration.className().contains("focus")
      createDeclaration.text mustBe messages("declaration.choice.link.create.new")
      createDeclaration.attr("href") mustBe StandardOrOtherJourneyController.displayPage.url

      val continueDraftDeclaration = options.get(1)
      continueDraftDeclaration.text mustBe messages("declaration.choice.link.manage.drafts")
      continueDraftDeclaration.attr("href") mustBe SavedDeclarationsController.displayDeclarations().url

      val dashboard = options.get(2)
      dashboard.text mustBe messages("declaration.choice.link.manage.submitted")
      dashboard.attr("href") mustBe toDashboard.url

      val movements = options.get(3)
      movements.text mustBe messages("declaration.choice.link.movements")
      movements.attr("href") mustBe movementsUrl

      val uploadDocuments = options.get(4)
      uploadDocuments.text mustBe messages("declaration.choice.link.sfus")
      uploadDocuments.attr("href") mustBe FileUploadController.startFileUpload("").url
    }

    "display the expected h2 headings" in {
      val headings = view.getElementsByTag("h2")
      headings.get(0).text mustBe messages("declaration.choice.heading.movements")
      headings.get(1).text mustBe messages("declaration.choice.heading.sfus")
    }

    "display the expected paragraphs" in {
      view.getElementById("arrive-or-depart").text mustBe messages("declaration.choice.paragraph.1.movements")
      view.getElementById("consolidate").text mustBe messages("declaration.choice.paragraph.2.movements")
    }
  }
}
