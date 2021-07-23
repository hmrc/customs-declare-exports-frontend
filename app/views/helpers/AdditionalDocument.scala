/*
 * Copyright 2021 HM Revenue & Customs
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

import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{govukHint, govukInsetText}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import views.html.components.gds.paragraphBody

object AdditionalDocument {

  def documentCodeHint(implicit request: JourneyRequest[_]): String =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments) "declaration.additionalDocument.documentTypeCode.hint.fromAuthCode"
    else "declaration.additionalDocument.documentTypeCode.hint"

  def documentIdentifierInsets(implicit messages: Messages, request: JourneyRequest[_]): Option[HtmlFormat.Appendable] =
    if (!request.cacheModel.isAuthCodeRequiringAdditionalDocuments) None
    else {
      val html = new Html(
        List(
          new paragraphBody().apply(messages("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph1")),
          new paragraphBody().apply(messages("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph2"))
        )
      )

      val insets = new govukInsetText().apply(InsetText(content = HtmlContent(html)))
      Some(insets)
    }

  def pageHint(implicit messages: Messages, request: JourneyRequest[_]): Html =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments)
      new Html(
        List(
          new govukHint().apply(Hint(content = HtmlContent(messages("declaration.additionalDocument.hint.fromAuthCode.paragraph1")))),
          new govukHint().apply(Hint(content = HtmlContent(messages("declaration.additionalDocument.hint.fromAuthCode.paragraph2"))))
        )
      )
    else
      new Html(List(new govukHint().apply(Hint(content = HtmlContent(messages("declaration.additionalDocument.hint"))))))

  def title(implicit request: JourneyRequest[_]): String =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments) "declaration.additionalDocument.title.fromAuthCode"
    else "declaration.additionalDocument.title"
}
