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

package views.declaration

import base.Injector
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.declarationHolder.DeclarationHolder
import models.Mode
import models.declaration.EoriSource
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationHolder.declaration_holder_remove
import views.tags.ViewTest

@ViewTest
class DeclarationHolderRemoveViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[declaration_holder_remove]
  val declarationHolder: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB123456543")), Some(EoriSource.OtherEori))

  private def createView(mode: Mode = Mode.Normal, form: Form[YesNoAnswer] = YesNoAnswer.form(), holder: DeclarationHolder = declarationHolder)(
    implicit request: JourneyRequest[_]
  ): Document = page(mode, holder, form)(request, messages)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.declarationHolder.remove.title")
    messages must haveTranslationFor("declaration.declarationHolders.table.type")
    messages must haveTranslationFor("declaration.declarationHolders.table.eori")
  }

  "DeclarationHolder Remove View back link" should {
    onEveryDeclarationJourney() { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.DeclarationHolderSummaryController.displayPage(Mode.Normal))
      }
    }
  }

  "DeclarationHolder Remove View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in table" in {
        val view = createView()
        view.select("dl>div:nth-child(1)>dt").text() mustBe messages("declaration.declarationHolders.table.type")
        view.select("dl>div:nth-child(1)>dd").text() mustBe "ACE"
        view.select("dl>div:nth-child(2)>dt").text() mustBe messages("declaration.declarationHolders.table.eori")
        view.select("dl>div:nth-child(2)>dd").text() mustBe "GB123456543"
      }
    }
  }
}
