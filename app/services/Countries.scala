/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json._
import services.model.Country
import utils.FileReader

object Countries {

  private val countries: List[Country] = {
    val jsonFile = getClass.getResourceAsStream("/code-lists/location-autocomplete-canonical-list.json")

    def fromJsonFile: List[Country] =
      Json.parse(jsonFile) match {
        case JsArray(cs) =>
          cs.toList.collect {
            case JsArray(Seq(c: JsString, cc: JsString)) =>
              Country(c.value, countryCode(cc.value))
          }
        case _ =>
          throw new IllegalArgumentException("Could not read JSON array of countries from : " + jsonFile)
      }

    fromJsonFile.sortBy(_.countryName)
  }

  private def countryCode: String => String = cc => cc.split(":")(1).trim

  val allCountries: List[Country] = countries

  val countryCodeMap: Map[String, Country] = countries.map(country => (country.countryCode, country)).toMap

  val countryNameMap: Map[String, Country] = countries.map(country => (country.countryName, country)).toMap

  def findByCode(code: String): Country = countryCodeMap(code)

  def findByName(name: String): Country = countryNameMap(name)

  def findByCodes(codes: Seq[String]): Seq[Country] = codes.map(countryCodeMap(_))

  lazy val euCountries: List[String] =
    FileReader("code-lists/eu-countries.csv")

  lazy val euSpecialFiscalTerritories: List[String] =
    FileReader("code-lists/eu-special-fiscal-territories.csv")
}
