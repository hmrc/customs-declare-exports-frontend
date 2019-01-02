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

package services

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import forms._
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
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

  def fetchMovementRequest(cacheId: String, eori: String)(implicit hc: HeaderCarrier,
    executionContext: ExecutionContext): Future[Option[InventoryLinkingMovementRequest]] = {
    fetch(cacheId).map {
      case Some(cacheMap) =>
        Some(Movement.createMovementRequest(cacheMap, eori))
      case _ => None
    }
  }
}