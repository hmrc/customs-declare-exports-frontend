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
import com.typesafe.config.ConfigFactory
import config.AppConfig
import controllers.declaration.routes
import controllers.helpers.SaveAndReturn
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.declaration.{CommodityDetails, IsLicenceRequired}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.Configuration
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.is_licence_required
import views.tags.ViewTest

@ViewTest
class IsLicenceRequiredViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  override val configuration: Configuration = Configuration(ConfigFactory.parseString("microservice.services.features.waiver999L=enabled"))

  private val appConfig = instanceOf[AppConfig]
  private val isLicenceRequiredPage = instanceOf[is_licence_required]

  private def createView(form: Form[YesNoAnswer] = IsLicenceRequired.form)(implicit request: JourneyRequest[_]): Document =
    isLicenceRequiredPage(Mode.Normal, "itemId", form)(request, messages)

  "IsLicenceReq View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {

        createView().getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.item.isLicenceRequired.title")
      }

      "display section header" in {

        createView().getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display radio button with Yes option" in {
        val view = createView()
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
      }

      "display radio button with No option" in {
        val view = createView()
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView().getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveButton = createView().getElementById("submit_and_return")
        saveButton must containMessage(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }

      "display 'Back' button that links to 'AdditionalInfo' page" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalInformationRequiredController.displayPage(Mode.Normal, "itemId").url)
      }
    }

  }

  "invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error when answer is empty" in {

        val view = createView(IsLicenceRequired.form.fillAndValidate(YesNoAnswer("")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.item.isLicenceRequired.error")
      }

    }

  }

  "IsLicenceRequired View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display answer input" in {

        val view = createView(IsLicenceRequired.form.fill(YesNoAnswer(yes)))

        view.getElementById("code_yes") must beSelected
      }
    }
  }

  "tariffLink should display" when {

    val code = "0987654321"

    onEveryDeclarationJourney(withItem(anItem(withItemId("itemId"), withCommodityDetails(CommodityDetails(Some(code), None))))) { implicit request =>
      "commodity code exists" in {

        val view = createView()
        val inset = view.getElementsByClass("govuk-inset-text").get(0).children.get(1)

        inset.child(0) must haveHref(s"${appConfig.tariffGuideUrl("urls.tariff.declaration.item.isLicenceRequired.inset.1.0")}$code#export")

      }
    }

    onEveryDeclarationJourney() { implicit request =>
      "commodity does not exist" in {

        val view = createView()
        val inset = view.getElementsByClass("govuk-inset-text").get(0).children.get(1)

        inset.child(0) must haveHref(appConfig.tariffBrowseUrl)

      }

    }

  }

}
