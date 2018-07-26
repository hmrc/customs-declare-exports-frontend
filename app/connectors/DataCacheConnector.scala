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

import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.json.Format
import repositories.SessionRepository
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.CascadeUpsert

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataCacheConnectorImpl @Inject()(val sessionRepository: SessionRepository, val cascadeUpsert: CascadeUpsert)
  extends DataCacheConnector {

  def save[A](cacheId: String, key: String, value: A)(implicit fmt: Format[A]): Future[CacheMap] =
    sessionRepository().get(cacheId).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert(key, value, optionalCacheMap.getOrElse(new CacheMap(cacheId, Map())))
      sessionRepository().upsert(updatedCacheMap).map (_ => updatedCacheMap)
    }

  def remove(cacheId: String, key: String): Future[Boolean] =
    sessionRepository().get(cacheId).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(Future.successful(false)) { cacheMap =>
        val newCacheMap = cacheMap copy (data = cacheMap.data - key)
        sessionRepository().upsert(newCacheMap)
      }
    }

  def removeAndRetrieveEntries(cacheMap: CacheMap, keys: Seq[String]): Future[CacheMap] = {
    val newCacheMap = cacheMap copy (data = cacheMap.data.filterKeys(!keys.contains(_)))
    sessionRepository().upsert(newCacheMap).flatMap { updated =>
      if(updated) Future.successful(newCacheMap)
      else Future.successful(cacheMap)
    }
  }

  def fetch(cacheId: String): Future[Option[CacheMap]] = sessionRepository().get(cacheId)

  def getEntry[A](cacheId: String, key: String)(implicit fmt: Format[A]): Future[Option[A]] =
    fetch(cacheId).map { optionalCacheMap =>
      optionalCacheMap.flatMap (cacheMap => cacheMap.getEntry(key))
    }

  def clearAndRetrieveCache(cacheId: String): Future[CacheMap] =
    sessionRepository().get(cacheId).flatMap {
      case Some(cacheMap) =>
        val newCacheMap = cacheMap copy (data = Map())
        sessionRepository().upsert(newCacheMap).map(_ => newCacheMap)
      case None => Future.successful(CacheMap(cacheId, Map()))
    }
}

@ImplementedBy(classOf[DataCacheConnectorImpl])
trait DataCacheConnector {
  def save[A](cacheId: String, key: String, value: A)(implicit fmt: Format[A]): Future[CacheMap]

  def remove(cacheId: String, key: String): Future[Boolean]

  def removeAndRetrieveEntries(cacheMap: CacheMap, keys: Seq[String]): Future[CacheMap]

  def fetch(cacheId: String): Future[Option[CacheMap]]

  def getEntry[A](cacheId: String, key: String)(implicit fmt: Format[A]): Future[Option[A]]

  def clearAndRetrieveCache(cacheId: String): Future[CacheMap]
}
