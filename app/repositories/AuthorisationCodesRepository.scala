/*
 * Copyright 2020 HM Revenue & Customs
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

package repositories

import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.Index
import reactivemongo.bson.BSONObjectID
import services.HolderOfAuthorisationCode
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthorisationCodesRepository @Inject()(implicit mc: ReactiveMongoComponent, ec: ExecutionContext)
    extends ReactiveRepository[HolderOfAuthorisationCode, BSONObjectID](
      "authorisationCodes",
      mc.mongoConnector.db,
      HolderOfAuthorisationCode.format,
      objectIdFormats
    ) {

  def findAll(): Future[List[HolderOfAuthorisationCode]] = find().map(_.sortBy(_.value))

  def updateAllCodes(codes: List[HolderOfAuthorisationCode]): Future[Unit] = collection.drop(false).andThen {
    case _ => codes.map(collection.insert(true).one(_))
  }.map(_ => ())
}
