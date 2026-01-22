/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Empty
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import views.components.gds.Styles.gdsPageLegend

object DescribedByHelper {
  def apply(form: Form[_], fieldName: String, titleKey: Option[String], content: List[Html])(implicit messages: Messages): Fieldset =
    Fieldset(
      describedBy = Option.when(form.hasErrors)(s"$fieldName-error"),
      legend = Some(
        Legend(
          content = titleKey.fold[Content](Empty)(key => Text(messages(key))),
          classes = gdsPageLegend,
          isPageHeading = titleKey.fold(false)(_ => true)
        )
      ),
      html = HtmlFormat.fill(content)
    )
}
