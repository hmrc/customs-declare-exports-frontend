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

package views.declaration

import base.Injector
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.authorisationHolder.AuthorisationHolder
import models.declaration.EoriSource
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.authorisationHolder.authorisation_holder_remove
import views.tags.ViewTest

@ViewTest
class AuthorisationHolderRemoveViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[authorisation_holder_remove]
  val authorisationHolder: AuthorisationHolder = AuthorisationHolder(Some("ACE"), Some(Eori("GB123456543")), Some(EoriSource.OtherEori))

  private def createView(form: Form[YesNoAnswer] = YesNoAnswer.form(), holder: AuthorisationHolder = authorisationHolder)(
    implicit request: JourneyRequest[_]
  ): Document = page(holder, form)(request, messages)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.authorisationHolder.remove.title")
    messages must haveTranslationFor("declaration.authorisationHolder.table.type")
    messages must haveTranslationFor("declaration.authorisationHolder.table.eori")
  }

  "AuthorisationHolder Remove View back link" should {
    onEveryDeclarationJourney() { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.AuthorisationHolderSummaryController.displayPage)
      }
    }
  }

  "AuthorisationHolder Remove View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in table" in {
        val view = createView()
        view.select("dl>div:nth-child(1)>dt").text() mustBe messages("declaration.authorisationHolder.table.type")
        view.select("dl>div:nth-child(1)>dd").text() mustBe "ACE"
        view.select("dl>div:nth-child(2)>dt").text() mustBe messages("declaration.authorisationHolder.table.eori")
        view.select("dl>div:nth-child(2)>dd").text() mustBe "GB123456543"
      }
    }
  }
}
