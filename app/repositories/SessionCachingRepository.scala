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

package repositories

import javax.inject.{Inject, Singleton}
import models.UserSession
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.{BSONDocument, BSONObjectID, _}
import reactivemongo.api.indexes.{Index, IndexType}
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.JSONSerializationPack.Writer
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionCachingRepository @Inject()(implicit mc: ReactiveMongoComponent, ec: ExecutionContext)
  extends ReactiveRepository[UserSession, BSONObjectID]("sessionCaching", mc.mongoConnector.db,
    UserSession.formats, objectIdFormats) {
  implicit val objectIdFormats = ReactiveMongoFormats.objectIdFormats

  val EXPORTS_SESSION_ID = "sessionId"

  override def indexes: Seq[Index] = Seq(
    Index(Seq("loggedInDateTime" -> IndexType.Ascending), name = Some("loggedInDateTimeIdx"),
      options = BSONDocument("expireAfterSeconds" -> 1200)),
    Index(Seq("sessionId" -> IndexType.Ascending), name = Some("sessionIdx"), unique = true)
  )

  def getSession()(implicit hc: HeaderCarrier): Future[Option[UserSession]] = {
    val query = BSONDocument(EXPORTS_SESSION_ID -> hc.sessionId.get.value)
    collection.find(query).one[UserSession].map { res => res }
  }

  def getForm[A](formName : String)(implicit format: Format[A], hc: HeaderCarrier): Future[Option[A]] = {
    val query = BSONDocument(EXPORTS_SESSION_ID -> hc.sessionId.get.value)
    val projection =  BSONDocument(EXPORTS_SESSION_ID -> 1,"loggedInDateTime" -> 2 ,formName -> 3)
    collection.find(query, projection).one[A].map { res => res }
  }

  def saveSession(userSession: UserSession)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Boolean] =
    insert(userSession).map(res => res.ok)


  def updateSession[A](entity: A, formName: String)
    (implicit ec: ExecutionContext, writes: Writer[A], hc: HeaderCarrier): Future[Boolean] = {

    val selector = BSONDocument(EXPORTS_SESSION_ID -> hc.sessionId.get.value)

    val modifier = collection.updateModifier(
      BSONDocument("$set" -> BSONDocument(formName -> Json.toJson[A](entity))),
      upsert = true
    )

    collection.findAndModify(selector, modifier).map { _ => true }
  }
}