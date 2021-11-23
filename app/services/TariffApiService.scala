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

import scala.concurrent.{ExecutionContext, Future}

import config.featureFlags.TariffApiConfig
import connectors.TariffApiConnector
import javax.inject.{Inject, Singleton}
import models.ExportsDeclaration
import play.api.Logging
import play.api.libs.json._

case class CommodityInfo(code: String, description: String, units: String)

@Singleton
class TariffApiService @Inject()(tariffApiConfig: TariffApiConfig, tariffApiConnector: TariffApiConnector)(implicit ec: ExecutionContext)
    extends Logging {

  def retrieveCommodityInfoIfAny(declaration: ExportsDeclaration, itemId: String): Future[Option[CommodityInfo]] =
    if (!tariffApiConfig.isCommoditiesEnabled) Future.successful(None)
    else declaration.commodityCodeOfItem(itemId).map(retrieveCommodityInfoIfAny).getOrElse(Future.successful(None))

  private def retrieveCommodityInfoIfAny(commodityCode: String): Future[Option[CommodityInfo]] =
    tariffApiConnector.getCommodity(commodityCode).map {
      case Some(json) => extractIncludedObj(commodityCode, json)
      case _          => None
    }

  private def extractIncludedObj(commodityCode: String, json: JsValue): Option[CommodityInfo] =
    parseJsValue[JsArray](json, "included").flatMap(extractCommodityInfoFromIncludedObjIfAny(commodityCode, _))

  private def extractCommodityInfoFromIncludedObjIfAny(commodityCode: String, included: JsArray): Option[CommodityInfo] = {
    val objs: Seq[JsValue] = included.value

    val commodityInfo = (
      for {
        idOfDutyExpressionObjToFind <- extractIdOfDutyExpressionObj(objs)
        dutyExpressionObj <- objs.find(parseJsValue[String](_, "id").exists(_ == idOfDutyExpressionObjToFind))
        attrsObjOfDutyExpressionObj <- (dutyExpressionObj \ "attributes").toOption
        abbrTag <- parseJsValue[String](attrsObjOfDutyExpressionObj, "formatted_base")
      } yield extractCommodityInfo(commodityCode, abbrTag)
    ).flatten

    if (commodityInfo.exists(info => info.description.nonEmpty && info.units.nonEmpty)) commodityInfo else None
  }

  private val pattern = """<abbr\s+title\s*=\s*'(.+)'\s*>(.+)</abbr>""".r

  private def extractCommodityInfo(commodityCode: String, abbrTag: String): Option[CommodityInfo] =
    abbrTag match {
      case pattern(description, units) => Some(CommodityInfo(commodityCode, description.trim.toLowerCase, units.trim))
      case _                           => None
    }

  private def extractIdOfDutyExpressionObjFromMeasureObjWithId109(obj: JsValue): Option[String] =
    (obj \ "relationships" \ "duty_expression" \ "data").toOption.flatMap(parseJsValue[String](_, "id"))

  private def extractIdOfDutyExpressionObj(objs: Seq[JsValue]): Option[String] =
    objs
      .filter(obj => isMeasureObj(obj) && isMeasureObjWithId109(obj))
      .map(extractIdOfDutyExpressionObjFromMeasureObjWithId109)
      .headOption
      .flatten

  private def isMeasureObj(obj: JsValue): Boolean =
    parseJsValue[String](obj, "type").exists(_ == "measure")

  private def isMeasureObjWithId109(obj: JsValue): Boolean =
    (obj \ "relationships" \ "measure_type" \ "data").toOption.exists {
      parseJsValue[String](_, "id").exists(_ == "109")
    }

  private def parseJsValue[T](json: JsValue, name: String)(implicit rds: Reads[T]): Option[T] =
    (json \ name).validate[T] match {
      case JsSuccess(value, _) => Some(value)
      case JsError(_)          => None
    }
}
