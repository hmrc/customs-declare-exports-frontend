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

package views.helpers

import javax.inject.{Inject, Singleton}
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukInsetText
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import views.html.components.gds.paragraphBody

@Singleton
class AdditionalDocument @Inject()(insetTextPartial: GovukInsetText, paragraphBody: paragraphBody) {

  def documentCodeText(implicit request: JourneyRequest[_]): String =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments) "declaration.additionalDocument.documentTypeCode.text.fromAuthCode"
    else "declaration.additionalDocument.documentTypeCode.text"

  def documentCodeHint(implicit request: JourneyRequest[_]): String =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments) "declaration.additionalDocument.documentTypeCode.hint.fromAuthCode"
    else "declaration.additionalDocument.documentTypeCode.hint"

  def documentIdentifierInsets(implicit messages: Messages, request: JourneyRequest[_]): Option[HtmlFormat.Appendable] =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments) {
      val html = new Html(
        List(
          paragraphBody(messages("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph1")),
          paragraphBody(messages("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph2"))
        )
      )

      Some(insetTextPartial(InsetText(content = HtmlContent(html))))
    } else None

  def pageBody(implicit messages: Messages, request: JourneyRequest[_]): Html =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments)
      new Html(
        List(
          paragraphBody(messages("declaration.additionalDocument.text.fromAuthCode.paragraph1")),
          paragraphBody(messages("declaration.additionalDocument.text.fromAuthCode.paragraph2"))
        )
      )
    else
      new Html(List(paragraphBody(messages("declaration.additionalDocument.text"))))
}

object AdditionalDocument {

  def title(implicit request: JourneyRequest[_]): String =
    if (request.cacheModel.isAuthCodeRequiringAdditionalDocuments) "declaration.additionalDocument.title.fromAuthCode"
    else "declaration.additionalDocument.title"
}
