/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.declaration.Seal
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.seal
import views.tags.ViewTest

@ViewTest
class SealViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {
  private val form: Form[Seal] = Seal.form()
  private def createView(mode: Mode = Mode.Normal, form: Form[Seal] = form, container: Boolean = false): Document =
    new seal(mainTemplate)(mode, form, Seq.empty, container)(journeyRequest, stubMessages())

  "Seal View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest)
      messages("standard.seal.title") mustBe "Transport details - seals"
      messages("standard.seal.id") mustBe "Seal identification number"
    }

    "display page title" in {
      createView().getElementById("title").text() must be("standard.seal.title")
    }

    "display header" in {
      createView().select("legend>h1").text() must be("standard.seal.title")
    }

    "display 'Back' button that links to 'add-transport-containers'  or 'transport-details' page" in {

      val backLinkContainer = createView(container = true).getElementById("link-back")

      backLinkContainer.text() must be("site.back")
      backLinkContainer.getElementById("link-back") must haveHref(
        controllers.declaration.routes.TransportContainerController.displayPage(Mode.Normal)
      )

      val backLinkTrader = createView().getElementById("link-back")

      backLinkTrader.text() must be("site.back")
      backLinkTrader.getElementById("link-back") must haveHref(
        controllers.declaration.routes.TransportDetailsController.displayPage(Mode.Normal)
      )
    }

    "display 'Save and continue' button on page" in {
      createView().getElementById("submit").text() must be("site.save_and_continue")
    }

    "display 'Save and return' button on page" in {
      createView().getElementById("submit_and_return").text() must be("site.save_and_come_back_later")
    }
  }
}
