/*
 * Copyright 2019 HM Revenue & Customs
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

package services.countries

import com.google.inject.Inject
import config.AppConfig
import javax.inject.Singleton
import play.api._
import play.api.libs.json._
import services.Country

import scala.io.Source

@Singleton
class Countries @Inject()(appConfig: AppConfig, environment: Environment) {
  private val countriesFilename: String = appConfig.countryCodesJsonFilename

  private def mdgCountryCodes(fileName: String): List[String] =
    Source
      .fromInputStream(environment.classLoader.getResourceAsStream(fileName))
      .getLines()
      .mkString
      .split(',')
      .map(_.replace("\"", ""))
      .toList

  private val countries: List[Country] = {
    def fromJsonFile: List[Country] =
      Json.parse(environment.classLoader.getResourceAsStream(countriesFilename)) match {
        case JsArray(cs) =>
          cs.toList.collect {
            case JsArray(Seq(c: JsString, cc: JsString)) => Country(c.value, countryCode(cc.value))
          }
        case _ =>
          throw new IllegalArgumentException("Could not read JSON array of countries from : " + countriesFilename)
      }

    fromJsonFile.sortBy(_.countryName)
  }

  private def countryCode: String => String = cc => cc.split(":")(1).trim

  val all: List[Country] = countries

}
