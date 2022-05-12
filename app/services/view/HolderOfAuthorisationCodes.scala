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

import config.AppConfig
import config.featureFlags.MerchandiseInBagConfig

import java.util.Locale
import connectors.CodeListConnector
import forms.declaration.declarationHolder.AuthorizationTypeCodes.codesFilteredFromView
import forms.declaration.declarationHolder.DeclarationHolder

import javax.inject.{Inject, Singleton}
import models.codes.HolderOfAuthorisationCode

@Singleton
class HolderOfAuthorisationCodes @Inject()(codeListConnector: CodeListConnector, merchandiseInBagConfig: MerchandiseInBagConfig) {

  def asListOfAutoCompleteItems(locale: Locale): List[AutoCompleteItem] =
    codeListConnector
      .getHolderOfAuthorisationCodes(locale)
      .values
      .filterNot(authCode => codesFilteredFromView(merchandiseInBagConfig).contains(authCode.code))
      .map(h => AutoCompleteItem(description(h), h.code))
      .toList

  def codeDescription(locale: Locale, code: String): String =
    codeListConnector.getHolderOfAuthorisationCodes(locale).get(code).fold("")(description)

  def codeDescriptions(locale: Locale, holders: Seq[DeclarationHolder]): Seq[String] =
    holders.flatMap {
      _.authorisationTypeCode.map { code =>
        codeListConnector.getHolderOfAuthorisationCodes(locale).get(code).fold("")(description)
      }
    }

  private def description(h: HolderOfAuthorisationCode): String = s"${h.code} - ${h.description}"
}
