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

package base

import java.time.LocalDateTime

import controllers.util.{Add, Remove, SaveAndContinue}
import models.ExportsDeclaration
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.mvc.Request
import play.api.test.FakeRequest

import scala.util.Random

object TestHelper {

  def createRandomAlphanumericString(length: Int): String = Random.alphanumeric.take(length).mkString

  val maxStringLength = 150
  def createRandomString(length: Int = maxStringLength): String = Random.nextString(length)

  def getDataSeq[A](size: Int, elementBuilder: () => A): Seq[A] = (1 to size).map(_ => elementBuilder())
  def getDataSeq[A](size: Int, elementBuilder: Int => A, builderParam: Int): Seq[A] =
    (1 to size).map(_ => elementBuilder(builderParam))

  val addActionUrlEncoded = (Add.toString, "")
  val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  def journeyRequest(fakeRequest: FakeRequest[_], choice: String): JourneyRequest[_] = {
    val cache = ExportsDeclaration.apply("sessionId", "draftId", LocalDateTime.now(), LocalDateTime.now(), choice)
    JourneyRequest(
      AuthenticatedRequest(fakeRequest, ExportsTestData.newUser(Random.nextString(10), Random.nextString(5))),
      cache
    )
  }

  def journeyRequest(fakeRequest: Request[_], choice: String): JourneyRequest[_] = {
    val cache = ExportsDeclaration.apply("sessionId", "draftId", LocalDateTime.now(), LocalDateTime.now(), choice)
    JourneyRequest(
      AuthenticatedRequest(fakeRequest, ExportsTestData.newUser(Random.nextString(10), Random.nextString(5))),
      cache
    )
  }

}
