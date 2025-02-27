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

import connectors.{CodeLinkConnector, CodeListConnector}
import models.codes.Country
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import services.Countries

import javax.inject.{Inject, Singleton}

@Singleton
class CountryHelper @Inject() (codeLinkConnector: CodeLinkConnector)(implicit codeListConnector: CodeListConnector) {

  def countryNameFromDestinationCountry(implicit messages: Messages, request: JourneyRequest[_]): String =
    request.cacheModel.locations.destinationCountry
      .flatMap(_.code)
      .flatMap(Countries.findByCode(_))
      .map(getShortNameForCountry)
      .getOrElse("")

  def generateAutocompleteEnhancementJson(countryKey: Country => String)(implicit messages: Messages): JsValue = {
    val jsObjects = for {
      country <- codeListConnector.getCountryCodes(messages.lang.toLocale).values
    } yield {
      val aliases = codeLinkConnector.getAliasesForCountryCode(country.countryCode).getOrElse(Seq.empty)
      Json.obj("code" -> countryKey(country), "displayName" -> country.asString(), "synonyms" -> aliases)
    }

    Json.toJson(jsObjects)
  }

  def getListOfAllCountries()(implicit messages: Messages): List[Country] = Countries.getListOfAllCountries()(messages, codeListConnector)

  def getShortNameForCountry(country: Country): String =
    codeLinkConnector
      .getShortNamesForCountryCode(country.countryCode)
      .flatMap(_.headOption)
      .getOrElse(country.countryName)

  def getShortNameForCountryCode(code: String)(implicit messages: Messages): Option[String] = {
    val country = Countries.findByCode(code)
    country.map(country =>
      codeLinkConnector
        .getShortNamesForCountryCode(country.countryCode)
        .flatMap(_.headOption)
        .getOrElse(country.countryName)
    )
  }

  def listOfRoutingCountries(implicit messages: Messages, request: JourneyRequest[_]): Seq[models.codes.Country] =
    request.cacheModel.locations.routingCountries
      .flatMap(_.country.code)
      .flatMap(Countries.findByCode(_))
}
