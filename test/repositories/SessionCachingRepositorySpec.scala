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

import base.{CustomExportsBaseSpec}
import forms.{GoodsPackage, SimpleAddress, SimpleDeclarationForm}
import models.{UserSession}
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfterAll
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, SessionId}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.reflect.ClassTag
class SessionCachingRepositorySpec extends CustomExportsBaseSpec with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    repositories.foreach { repo =>
      repo.removeAll().futureValue
    }
  }

  override lazy val app: Application = GuiceApplicationBuilder().build()

  protected def component[T: ClassTag]: T = app.injector.instanceOf[T]

  val sessionCaching = component[SessionCachingRepository]
  val repositories: Seq[ReactiveRepository[_, _]] = Seq(sessionCaching)

  val form = SimpleDeclarationForm("DUCR1",false,None,true,
    false,
    true,
    true,
    SimpleAddress(None,None,None,None,None,None),
    true,
    GoodsPackage("wew",true,true,true,"3","Packagetype",false,false),
    true,
    "customsProcedure",
    false,
    "additionalCustomsProcedure",
    true,
    false,
    "office1",
    true)

  val updatedForm = SimpleDeclarationForm("DUCR2",false,None,true,
    false,
    true,
    true,
    SimpleAddress(None,None,None,None,None,None),
    true,
    GoodsPackage("wew",true,true,true,"4","Packagetype-4",false,false),
    true,
    "customsProcedure1",
    false,
    "additionalCustomsProcedure1",
    true,
    false,
    "office1",
    true)

  implicit val hc = HeaderCarrier(authorization = Some(Authorization("token1")), sessionId=Some(SessionId(randomString(3))))

  "SessionCaching" should {
    val userSession: UserSession = UserSession(hc.sessionId.get.value, DateTime.now, Some(form))

    "save User session " in {
      val res = sessionCaching.saveSession(userSession)
      res.futureValue must be(true)
    }

    "get the user session saved " in {
      val res = sessionCaching.getSession()(hc)
      res.futureValue.get must be (userSession)
    }
    "get the form saved " in {
      val res = sessionCaching.getForm[UserSession]("simpleDeclarationForm")(UserSession.formats,hc)
      res.futureValue.get.simpleDeclarationForm must be (userSession.simpleDeclarationForm)
    }
    "update the user session saved " in {
      val res = sessionCaching.updateSession(updatedForm,"simpleDeclarationForm")(ec, SimpleDeclarationForm.formats, hc)
      res.futureValue must be (true)
      val res1 = sessionCaching.getSession()(hc)
      res1.futureValue.get.simpleDeclarationForm.get must be (updatedForm)
    }
  }
}