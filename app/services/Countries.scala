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

package services

import connectors.CodeListConnector
import models.codes.Country
import play.api.i18n.Messages

object Countries {

  def findByCode(code: String)(implicit messages: Messages, codeListConnector: CodeListConnector): Option[Country] =
    codeListConnector.getCountryCodes(messages.lang.toLocale).get(code)

  def findByCodes(codes: Seq[String])(implicit messages: Messages, codeListConnector: CodeListConnector): Seq[Country] = {
    val codeToCountryMap = codeListConnector
      .getCountryCodes(messages.lang.toLocale)

    codes.map(codeToCountryMap(_))
  }

  def isValidCountryCode(countryCode: String)(implicit messages: Messages, codeListConnector: CodeListConnector): Boolean =
    codeListConnector
      .getCountryCodes(messages.lang.toLocale)
      .exists(codeCountryPair => codeCountryPair._2.countryCode == countryCode)

  def isValidCountryName(countryName: String)(implicit messages: Messages, codeListConnector: CodeListConnector): Boolean =
    codeListConnector
      .getCountryCodes(messages.lang.toLocale)
      .exists(codeCountryPair => codeCountryPair._2.countryName == countryName)

  def getListOfAllCountries()(implicit messages: Messages, codeListConnector: CodeListConnector): List[Country] =
    codeListConnector.getCountryCodes(messages.lang.toLocale).values.toList.sortBy(_.countryName)
}
