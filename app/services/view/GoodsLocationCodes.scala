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

package services.view

import connectors.CodeListConnector
import models.codes.GoodsLocationCode

import java.util.Locale
import javax.inject.{Inject, Singleton}

@Singleton
class GoodsLocationCodes @Inject() (codeListConnector: CodeListConnector) {

  def asListOfAutoCompleteItems(locale: Locale): List[AutoCompleteItem] =
    codeListConnector
      .getCseCodes(locale)
      .values
      .map(h => AutoCompleteItem(description(h), h.code))
      .toList

  private def description(h: GoodsLocationCode): String = s"${h.code} - ${h.description}"
}
