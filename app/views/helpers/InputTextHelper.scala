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

import javax.inject.Singleton
import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label

@Singleton
class InputTextHelper {

  def getLabelForField(labelKey: String, labelArg: String, labelClasses: String)(implicit messages: Messages): Label =
    if (labelKey.trim.isEmpty) Label()
    else Label(content = Text(messages(labelKey, labelArg)), classes = labelClasses)

  def getAnyErrorMessages(field: Field)(implicit messages: Messages): Option[ErrorMessage] =
    field.error.map(err => ErrorMessage(content = Text(messages(err.message))))

  def defineInputClasses(defaultInputClasses: String, inputClasses: Option[String]): String =
    inputClasses.map(clazz => s" $clazz").getOrElse(defaultInputClasses)
}
