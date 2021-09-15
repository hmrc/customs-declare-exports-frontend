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

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ErrorMessage, HtmlContent, Label}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import views.html.components.gds.headingContent

import javax.inject.{Inject, Singleton}

@Singleton
class InputTextHelper @Inject()(headingContent: headingContent) {

  def getLabelForField(
    isPageHeading: Boolean,
    labelKey: String,
    labelArg: String,
    headingClasses: String,
    labelClasses: String,
    sectionHeaderKey: Option[String]
  )(implicit messages: Messages): Label =
    if (isPageHeading) {
      Label(content = HtmlContent(headingContent(messages(labelKey, labelArg), sectionHeaderKey.map(messages(_)))), classes = headingClasses)
    } else {
      Label(content = Text(messages(labelKey, labelArg)), classes = labelClasses)
    }

  def getAnyErrorMessages(field: Field)(implicit messages: Messages): Option[ErrorMessage] =
    field.error.map(err => ErrorMessage(content = Text(messages(err.message))))

  def defineInputClasses(defaultInputClasses: String, inputClasses: Option[String]) =
    inputClasses.map(clazz => s" $clazz").getOrElse(defaultInputClasses)
}