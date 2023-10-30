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

package models.viewmodels.errors

import connectors.CodeListConnector
import controllers.declaration.routes.DucrEntryController
import models.declaration.errors.ErrorInstance
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import views.html.components.gds.link

import scala.collection.immutable

object CDS12062Interpreter extends ErrorInterpreter {
  def generateHtmlFor(error: ErrorInstance)(implicit messages: Messages, codeListConnector: CodeListConnector, link: link): Option[Html] = {

    // TODO: populate isAmendment flag properly
    val changeLink = errorChangeAction(DucrEntryController.displayPage, error.errorCode, None, error.sourceDec.id, false)

    Some(
      HtmlContent(
        HtmlFormat.fill(
          immutable.Seq(
            errorHeader,
            errorTitle(error),
            contentHeader,
            HtmlFormat.fill(formattedErrorDescription(error.errorCode) :+ changeLink),
            errorFooter
          )
        )
      ).asHtml
    )
  }
}
