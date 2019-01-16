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

package controllers.helpers

import play.api.libs.json._
import play.api.mvc.Request

object ControllersHelper {

  def trimFormData(request: Request[_]): Map[String, Seq[String]] = transformBodyIntoFormUrlEncoded(request).map {
    case (key, values) => (key, values.map(_.trim))
  }

  private def transformBodyIntoFormUrlEncoded(request: Request[_]): Map[String, Seq[String]] = request.body match {
    case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
    case body: play.api.mvc.AnyContent if body.asMultipartFormData.isDefined =>
      body.asMultipartFormData.get.asFormUrlEncoded
    case body: play.api.mvc.AnyContent if body.asJson.isDefined => fromJson(js = body.asJson.get).mapValues(Seq(_))
    case body: Map[_, _]                                        => body.asInstanceOf[Map[String, Seq[String]]]
    case body: play.api.mvc.MultipartFormData[_]                => body.asFormUrlEncoded
    case body: play.api.libs.json.JsValue                       => fromJson(js = body).mapValues(Seq(_))
    case _                                                      => Map.empty[String, Seq[String]]
  }

  private def fromJson(prefix: String = "", js: JsValue): Map[String, String] = js match {
    case JsObject(fields) => {
      fields.map {
        case (key, value) => fromJson(Option(prefix).filterNot(_.isEmpty).map(_ + ".").getOrElse("") + key, value)
      }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JsArray(values) => {
      values.zipWithIndex.map {
        case (value, i) => fromJson(prefix + "[" + i + "]", value)
      }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JsNull           => Map.empty
    case JsUndefined()    => Map.empty
    case JsBoolean(value) => Map(prefix -> value.toString)
    case JsNumber(value)  => Map(prefix -> value.toString)
    case JsString(value)  => Map(prefix -> value.toString)
  }

}
