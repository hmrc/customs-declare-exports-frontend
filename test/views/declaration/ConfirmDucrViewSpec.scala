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

import base.{Injector, MockAuthAction}
import controllers.declaration.routes
import forms.Ducr
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.STANDARD
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.confirm_ducr
import views.tags.ViewTest

@ViewTest
class ConfirmDucrViewSpec extends PageWithButtonsSpec with Injector with MockAuthAction {

  private val page = instanceOf[confirm_ducr]

  private val dummyGeneratedDucr = Ducr("dummyGeneratedDucr")
  private val view = page(YesNoAnswer.form(), dummyGeneratedDucr)(request, messages)
  override val typeAndViewInstance = (STANDARD, page(YesNoAnswer.form(), dummyGeneratedDucr)(_, _))

  "Confirm DUCR view" should {

    "not have View declaration summary link" in {
      Option(view.getElementById("view_declaration_summary")) mustBe None
    }

    "display title" in {
      view.getElementsByTag("h1").first().text() mustBe messages("declaration.confirmDucr.title")
    }

    "display row with DUCR" in {
      view.getElementsByClass("govuk-table__cell").first must containMessage("declaration.confirmDucr.row.ducr")
      view.getElementsByClass("govuk-table__cell").get(1).text mustBe dummyGeneratedDucr.ducr
    }

    "display body text" in {
      view.getElementsByClass("govuk-body").first.text() mustBe messages("declaration.confirmDucr.body.p1")
      view.getElementsByClass("govuk-body").get(1).text() mustBe messages("declaration.confirmDucr.body.p2")
    }

    "display radio button with Yes option" in {
      view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
      view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
    }

    "display radio button with No option" in {
      view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
      view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
    }

    "display first expander" in {
      val tariffText = view.getElementById("expander-ducr")
      val content = tariffText.child(1)

      tariffText.child(0).text mustBe messages("declaration.confirmDucr.expander.title")
      content must containMessage("declaration.confirmDucr.expander.content.p1")
      content must containMessage("declaration.confirmDucr.expander.content.p2")
      content must containMessage("declaration.confirmDucr.expander.content.list.1")
      content must containMessage("declaration.confirmDucr.expander.content.list.2")
      content must containMessage("declaration.confirmDucr.expander.content.list.3")
      content must containMessage("declaration.confirmDucr.expander.content.list.4")
      content must containMessage("declaration.confirmDucr.expander.content.list.5")
    }

    "display tariff expander" in {
      val tariffText = view.getElementsByClass("govuk-details__text").get(1)

      removeBlanksIfAnyBeforeDot(tariffText.text) mustBe messages(
        "tariff.declaration.confirmDucr.common.text",
        messages("tariff.declaration.confirmDucr.common.linkText.0")
      )
      tariffText.child(0) must haveHref(appConfig.tariffGuideUrl("urls.tariff.declaration.confirmDucr.common.0"))
    }

    "display back button to Trader Referemce page" in {
      val backLink = view.getElementById("back-link")
      backLink must containMessage(backToPreviousQuestionCaption)
      backLink must haveHref(routes.TraderReferenceController.displayPage)
    }

    checkAllSaveButtonsAreDisplayed(view)
  }
}
