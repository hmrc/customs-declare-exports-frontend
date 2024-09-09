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

package views.helpers

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage

object InputTextHelper {

  def defineInputClasses(defaultInputClasses: String, inputClasses: Option[String]): String =
    inputClasses.map(clazz => s" $clazz").getOrElse(defaultInputClasses)

  def fieldErrorMessages(field: Field)(implicit messages: Messages): Option[ErrorMessage] =
    field.error.map(err => ErrorMessage(content = HtmlContent(messages(err.message))))
}
