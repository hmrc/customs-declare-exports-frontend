/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.Inject
import config.AppConfig
import javax.inject.Singleton
import play.api.libs.json._

import scala.io.Source

case class Country(countryName: String, countryCode: String)

case object Country {
  implicit val formats = Json.format[Country]
}

@Singleton
class Countries @Inject()(appConfig: AppConfig) {

  private val mdgCountryCodes: List[String] =
    Source.fromInputStream(getClass.getResourceAsStream("/mdg-country-codes.csv"))
      .getLines()
      .mkString
      .split(',')
      .map(_.replace("\"", ""))
      .toList

  private val countries: List[Country] = {
    val jsonFile = getClass.getResourceAsStream("/location-autocomplete-canonical-list.json")

    def fromJsonFile: List[Country] = {
      Json.parse(jsonFile) match {
        case JsArray(cs) =>
          cs.toList.collect {
            case JsArray(Seq(c: JsString, cc: JsString)) =>
              Country(c.value, countryCode(cc.value))
          }
        case _ =>
          throw new IllegalArgumentException("Could not read JSON array of countries from : " + jsonFile)
      }
    }

    fromJsonFile.sortBy(_.countryName)
  }

  private def countryCode: String => String = cc => cc.split(":")(1).trim

  val all: List[Country] = countries.filter(c => mdgCountryCodes contains c.countryCode)
}
