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

import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.AllowedChoiceValues
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.libs.json.{Json, Writes}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.util.Random

object TestHelper {

  def createRandomAlphanumericString(length: Int): String = Random.alphanumeric.take(length).mkString

  val maxStringLength = 150
  def createRandomString(length: Int = maxStringLength): String = Random.nextString(length)

  def getDataSeq[A](size: Int, elementBuilder: () => A): Seq[A] = (1 to size).map(_ => elementBuilder())
  def getDataSeq[A](size: Int, elementBuilder: Int => A, builderParam: Int): Seq[A] =
    (1 to size).map(_ => elementBuilder(builderParam))

  def getCacheMap[A](data: A, formId: String)(implicit writes: Writes[A]): CacheMap =
    CacheMap(formId, Map(formId -> Json.toJson(data)))

  val addActionUrlEncoded = (Add.toString, "")
  val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  def journeyRequest(fakeRequest: FakeRequest[_], choice:String) : JourneyRequest[_] =
    JourneyRequest(AuthenticatedRequest(fakeRequest, ExportsTestData.newUser(Random.nextString(10), Random.nextString(5))),
      Choice(choice))
}
