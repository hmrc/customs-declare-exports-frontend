/*
 * Copyright 2024 HM Revenue & Customs
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

package models.requests

import forms.section1.Lrn
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.{Request, Session}
import play.twirl.api.HtmlFormat.Appendable
import uk.gov.hmrc.govukfrontend.views.html.components.GovukErrorSummary
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.{ErrorLink, ErrorSummary}

object SessionHelper extends Logging {

  val errorKey = "errors-to-notify"
  val errorSeparator = '|'

  val declarationUuid = "declarationUuid"

  val errorFixModeSessionKey = "in-error-fix-mode"

  val submissionActionId = "submission.actionId"
  val submissionDucr = "submission.ducr"
  val submissionUuid = "submission.uuid"
  val submissionLrn = "submission.lrn"
  val submissionMrn = "submission.mrn"

  def getValue(key: String)(implicit request: Request[_]): Option[String] =
    request.session.data.get(key)

  def getOrElse(key: String, default: String = "")(implicit request: Request[_]): String =
    request.session.data.getOrElse(key, default)

  def removeValue(key: String)(implicit request: Request[_]): Session =
    request.session - key

  def print(implicit request: Request[_]): Unit =
    logger.debug(s"\n======\n${request.session.data.toList.sortBy(_._1).map(t => s"${t._1} -> ${t._2}").mkString("\n")}\n======\n")

  def getDataForCancelDeclaration(implicit request: Request[_]): Option[CancelDeclarationData] =
    for {
      submissionId <- getValue(submissionUuid)
      mrn <- getValue(submissionMrn)
      lrn <- getValue(submissionLrn).map(Lrn(_))
      ducr <- getValue(submissionDucr)
    } yield CancelDeclarationData(submissionId, mrn, lrn, ducr)

  def showErrorsIfAny(implicit request: Request[_], messages: Messages): Option[Appendable] =
    getValue(errorKey).map { errorMessageKeys =>
      def content(key: String): Content = HtmlContent(s"""<span class="error-message">${messages(key)}</span>""")
      val errorLinks = errorMessageKeys.split(errorSeparator).toList.map(key => ErrorLink(content = content(key)))
      new GovukErrorSummary().apply(ErrorSummary(errorLinks, title = content("error.root.redirect.title")))
    }
}

case class CancelDeclarationData(submissionId: String, mrn: String, lrn: Lrn, ducr: String)
