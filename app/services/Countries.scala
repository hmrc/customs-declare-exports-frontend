/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.{CodeItem, CodeListConnector}
import models.codes.Country
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsObject, Json, JsString, OFormat}
import utils.JsonFile
import utils.JsonFile.{getClass, throwError}

object Countries {

  def findByCode(code: String)(implicit messages: Messages, codeListConnector: CodeListConnector): Country =
    codeListConnector.getCountryCodes(messages.lang.toLocale)(code)

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


case class Edges(from: Seq[String])

object Edges{
  implicit val format: OFormat[Edges] = Json.format[Edges]
}

case class Meta(canonical: Boolean, canonicalMask: Int, displayName: Boolean, stableName: Boolean)

object Meta {
  implicit val format: OFormat[Edges] = Json.format[Edges]
}

case class Names(cy: Boolean, enGB: String)

object Names {
  implicit val format: OFormat[Edges] = Json.format[Edges]
}

case class CountryGraph(edges: Edges, meta: Meta, names: Names)

object CountryGraph{
  implicit val format: OFormat[Edges] = Json.format[Edges]

  def loadFromFile() = {

    println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    val jsonInputStream = getClass.getResourceAsStream("/code-lists/location-autocomplete-graph.json")

    Json.parse(jsonInputStream) match {
      case JsArray(cs) =>
        cs.toList.collect {
          case _: JsArray =>
            println("Array")
        }
      case obj: JsObject =>
        val fields = obj.value
        println(fields.get("country:AD"))
      case x => println(s"Error ${x.getClass}")
    }
  }
}
