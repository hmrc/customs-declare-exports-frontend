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

package views.helpers

import forms.section2.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.section1.AdditionalDeclarationType._
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukInsetText
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import views.html.components.gds.{bulletList, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AuthorisationHolderRequiredHelper @Inject() (bulletList: bulletList, govukInsetText: GovukInsetText, paragraphBody: paragraphBody) {

  private val key = "declaration.authorisationHolderRequired"
  private val bodyKey = "declaration.authorisationHolderRequired.body"

  def body(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val model = request.cacheModel
    val body = (model.`type`, model.additionalDeclarationType, model.parties.authorisationProcedureCodeChoice) match {
      case (STANDARD, Some(STANDARD_PRE_LODGED), Choice1040)   => List(paragraph(s"$bodyKey.standard.prelodged.1040"))
      case (STANDARD, Some(STANDARD_PRE_LODGED), ChoiceOthers) => List(paragraph(s"$bodyKey.standard.prelodged.others"))
      case (STANDARD, Some(STANDARD_FRONTIER), _)              => List(paragraph(s"$bodyKey.standard.arrived"))

      case (OCCASIONAL, Some(OCCASIONAL_PRE_LODGED), _) =>
        List(paragraph(s"$bodyKey.occasional.1"), paragraph(s"$bodyKey.occasional.2"))

      case (OCCASIONAL, Some(OCCASIONAL_FRONTIER), _) =>
        val bullets = bulletList(List(row(s"$bodyKey.occasional.bullet.1"), row(s"$bodyKey.occasional.bullet.2")))
        List(paragraph(s"$bodyKey.occasional.1"), paragraph(s"$bodyKey.occasional.2"), bullets)

      case _ => List(paragraph(s"$bodyKey.default"))
    }

    HtmlFormat.fill(body)
  }

  def insetText(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val model = request.cacheModel
    (model.`type`, model.additionalDeclarationType, model.parties.authorisationProcedureCodeChoice) match {
      case (STANDARD, Some(STANDARD_PRE_LODGED), Choice1040) => HtmlFormat.empty
      case (OCCASIONAL, _, _)                                => HtmlFormat.empty

      case _ =>
        val content = HtmlContent(
          new Html(
            List(
              paragraphBody(messages(s"$key.inset.para1")),
              bulletList(List(Html(messages(s"$key.inset.bullet1.text")), Html(messages(s"$key.inset.bullet2.text")))),
              paragraphBody(messages(s"$key.inset.para2"))
            )
          )
        )

        govukInsetText(InsetText(content = content))
    }
  }

  def title(implicit request: JourneyRequest[_]): String = {
    val model = request.cacheModel
    (model.`type`, model.additionalDeclarationType, model.parties.authorisationProcedureCodeChoice) match {
      case (STANDARD, Some(STANDARD_PRE_LODGED), Choice1040) => s"$key.title.standard.prelodged.1040"
      case _                                                 => s"$key.title"
    }
  }

  private def paragraph(key: String)(implicit messages: Messages): Html = paragraphBody(message = messages(key))

  private def row(key: String)(implicit messages: Messages): Html = Html(messages(key))
}
