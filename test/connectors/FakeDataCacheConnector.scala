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

package connectors

import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

object FakeDataCacheConnector extends DataCacheConnector {
  override def save[A](cacheId: String, key: String, value: A)(implicit fmt: Format[A]): Future[CacheMap] = Future(CacheMap(cacheId, Map()))

  override def remove(cacheId: String, key: String): Future[Boolean] = ???

  override def fetch(cacheId: String): Future[Option[CacheMap]] = Future(Some(CacheMap(cacheId, Map())))

  override def getEntry[A](cacheId: String, key: String)(implicit fmt: Format[A]): Future[Option[A]] = ???

  override def addToCollection[A](cacheId: String, collectionKey: String, value: A)(implicit fmt: Format[A]): Future[CacheMap] = Future(CacheMap(cacheId, Map()))

  override def removeFromCollection[A](cacheId: String, collectionKey: String, item: A)(implicit fmt: Format[A]): Future[CacheMap] = Future(CacheMap(cacheId, Map()))

  override def replaceInCollection[A](cacheId: String, collectionKey: String, index: Int, item: A)(implicit fmt: Format[A]): Future[CacheMap] = Future(CacheMap(cacheId, Map()))
}
