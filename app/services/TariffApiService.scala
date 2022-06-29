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

package services

import scala.concurrent.{ExecutionContext, Future}

import config.featureFlags.TariffApiConfig
import connectors.TariffApiConnector
import javax.inject.{Inject, Singleton}
import models.ExportsDeclaration
import play.api.Logging
import play.api.libs.json._
import services.TariffApiService._

case class CommodityInfo(code: String, description: String, units: String)

@Singleton
class TariffApiService @Inject() (tariffApiConfig: TariffApiConfig, tariffApiConnector: TariffApiConnector)(implicit ec: ExecutionContext)
    extends Logging {

  def retrieveCommodityInfoIfAny(declaration: ExportsDeclaration, itemId: String): Future[TariffApiResult] =
    if (!tariffApiConfig.isCommoditiesEnabled) Future.successful(Left(CommodityCodeNotFound))
    else
      declaration
        .commodityCodeOfItem(itemId)
        .map(retrieveCommodityInfoIfAny)
        .getOrElse(Future.successful(Left(CommodityCodeNotFound)))

  private def retrieveCommodityInfoIfAny(commodityCode: String): Future[TariffApiResult] =
    if (commodityCode.length != 10) Future.successful(Left(CommodityCodeNotFound))
    else
      tariffApiConnector.getCommodityOnCondition(commodityCode).map {
        case Some(json) => extractIncludedObj(commodityCode, json)
        case _          => Left(CommodityCodeNotFound)
      }

  private def extractIncludedObj(commodityCode: String, json: JsValue): TariffApiResult =
    parseJsValue[JsArray](json, "included") match {
      case Some(included) => extractCommodityInfoFromIncludedObjIfAny(commodityCode, included)
      case _              => Left(CommodityCodeNotFound)
    }

  private def extractCommodityInfoFromIncludedObjIfAny(commodityCode: String, included: JsArray): TariffApiResult = {
    val objs: Seq[JsValue] = included.value

    val maybeCommodityInfo = (
      for {
        idOfDutyExpressionObjToFind <- extractIdOfDutyExpressionObj(objs)
        dutyExpressionObj <- objs.find(parseJsValue[String](_, "id").exists(_ == idOfDutyExpressionObjToFind))
        attrsObjOfDutyExpressionObj <- (dutyExpressionObj \ "attributes").toOption
        abbrTag <- parseJsValue[String](attrsObjOfDutyExpressionObj, "formatted_base")
      } yield extractCommodityInfo(commodityCode, abbrTag)
    ).flatten

    def nonEmpty(commodityInfo: CommodityInfo): Boolean = commodityInfo.description.nonEmpty && commodityInfo.units.nonEmpty

    maybeCommodityInfo match {
      case Some(commodityInfo) if nonEmpty(commodityInfo) => Right(commodityInfo)
      case _                                              => Left(SupplementaryUnitsNotRequired)
    }
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

object TariffApiService {

  sealed trait NoInfoForCommodity

  case object CommodityCodeNotFound extends NoInfoForCommodity
  case object SupplementaryUnitsNotRequired extends NoInfoForCommodity

  type TariffApiResult = Either[NoInfoForCommodity, CommodityInfo]
}
