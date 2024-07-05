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

package views.section2

import base.Injector
import controllers.section2.routes.AdditionalActorsSummaryController
import forms.common.{Eori, YesNoAnswer}
import forms.section2.AdditionalActor
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import utils.ListItem
import views.html.section2.additionalActors.additional_actors_remove
import views.common.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class AdditionalActorsRemoveViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  val additionalActor = AdditionalActor(Some(Eori("GB123456789000")), Some("MF"))
  private val page = instanceOf[additional_actors_remove]

  private def createView(form: Form[YesNoAnswer] = YesNoAnswer.form(), actor: AdditionalActor = additionalActor)(
    implicit request: JourneyRequest[_]
  ): Document = page(ListItem.createId(0, additionalActor), actor, form)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.additionalActors.remove.title")
    messages must haveTranslationFor("declaration.additionalActors.table.party")
    messages must haveTranslationFor("declaration.additionalActors.table.eori")
  }

  "AdditionalActors Remove View back link" should {
    onEveryDeclarationJourney() { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(AdditionalActorsSummaryController.displayPage)
      }
    }
  }

  "AdditionalActors Remove View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in table" in {
        val view = createView()

        view.select("dl>div:nth-child(1)>dt").text() mustBe messages("declaration.additionalActors.table.party")
        view.select("dl>div:nth-child(1)>dd").text() mustBe messages("declaration.partyType.MF")
        view.select("dl>div:nth-child(2)>dt").text() mustBe messages("declaration.additionalActors.table.eori")
        view.select("dl>div:nth-child(2)>dd").text() mustBe "GB123456789000"
      }
    }
  }
}
