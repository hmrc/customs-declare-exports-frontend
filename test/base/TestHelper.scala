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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.annotation.tailrec
import scala.util.Random

object TestHelper {

  def createRandomAlphanumericString(length: Int): String = Random.alphanumeric.take(length).mkString

  val maxStringLength = 150
  def createRandomString(length: Int = maxStringLength): String = Random.nextString(length)

  def getDataSeq[A](n: Int, elem: A): Seq[A] = {
    @tailrec
    def loop(n: Int, seq: Seq[A]): Seq[A] =
      if (n == 0) {
        seq
      } else {
        loop(n - 1, seq :+ elem)
      }
    loop(n, Seq.empty)
  }

  def getCacheMap[A](data: A, formId: String)(implicit writes: Writes[A]): CacheMap =
    CacheMap(formId, Map(formId -> Json.toJson(data)))

}
