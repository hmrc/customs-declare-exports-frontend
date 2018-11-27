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

import java.time.LocalDateTime

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import forms._
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsHttpCaching @Inject()(cfg: AppConfig, httpClient: HttpClient) extends ShortLivedHttpCaching {

  override def defaultSource: String = cfg.keyStoreSource

  override def baseUri: String = cfg.keyStoreUrl

  override def domain: String = cfg.sessionCacheDomain

  override def http: HttpClient = httpClient
}

@Singleton
class CustomsCacheService @Inject()(
  caching: CustomsHttpCaching,
  applicationCrypto: ApplicationCrypto
) extends ShortLivedCache {

  override implicit val crypto: CompositeSymmetricCrypto = applicationCrypto.JsonCrypto

  override def shortLiveCache: ShortLivedHttpCaching = caching

  def fetchMovementRequest(cacheId: String)(implicit hc: HeaderCarrier,
    executionContext: ExecutionContext): Future[Option[InventoryLinkingMovementRequest]] = {
      fetch(cacheId).map {
        case Some(cacheMap) =>
          Some(fun(cacheMap))
        case _ => None
      }
  }

  private def fun(cacheMap: CacheMap): InventoryLinkingMovementRequest = {
    val choice = cacheMap.getEntry[ChoiceForm](MovementFormsAndIds.choiceId)
    val ducr = cacheMap.getEntry[EnterDucrForm](MovementFormsAndIds.enterDucrId)
    val goodsDate = cacheMap.getEntry[GoodsDateForm](MovementFormsAndIds.goodsDateId)
    val location = cacheMap.getEntry[LocationForm](MovementFormsAndIds.locationId)
    val transport = cacheMap.getEntry[TransportForm](MovementFormsAndIds.transportId)

    // TODO: Provide default values or some kind of validation for mandatory fields
    InventoryLinkingMovementRequest(
      messageCode = choice.map(_.choice).getOrElse(""),
      agentDetails = Some(AgentDetails(
        eori = Some("1234567"),
        agentLocation = location.flatMap(_.agentLocation),
        agentRole = location.flatMap(_.agentRole)
      )),
      ucrBlock = UcrBlock(
        ucr = ducr.map(_.ducr).getOrElse(""),
        ucrType = "M"
      ),
      goodsLocation = location.map(_.goodsLocation.get).getOrElse(""),

      goodsArrivalDateTime = if (goodsDate.isDefined) Some(extractDateTime(goodsDate.get)) else None,
      goodsDepartureDateTime = if (goodsDate.isDefined) Some(extractDateTime(goodsDate.get)) else None,
      shedOPID = location.flatMap(_.shed),
      masterUCR = None,
      masterOpt = None,
      movementReference = None,
      transportDetails = Some(TransportDetails(
        transportID = transport.flatMap(_.transportId),
        transportMode = transport.flatMap(_.transportMode),
        transportNationality = transport.flatMap(_.transportNationality)
      ))
    )
  }

  private def extractDateTime(form: GoodsDateForm): String =
    LocalDateTime.of(form.year.toInt, form.month.toInt, form.day.toInt,
        form.hour.getOrElse("00").toInt, form.minute.getOrElse("00").toInt).toString + ":00"

}