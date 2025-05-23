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

package forms.section2

import forms.DeclarationPage
import forms.common.YesNoAnswer
import play.api.data.Form

object EntryIntoDeclarantsRecords extends DeclarationPage {

  val fieldName = "is-entry-into-declarant-records"
  private val errorKey = "declaration.entryIntoDeclarantRecords.error"

  def form: Form[YesNoAnswer] = YesNoAnswer.form(fieldName, errorKey)
}
