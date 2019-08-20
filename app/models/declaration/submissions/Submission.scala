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

package models.declaration.submissions

import java.time.LocalDateTime
import java.util.UUID

import play.api.libs.json.Json

case class Submission(
  uuid: String = UUID.randomUUID().toString,
  eori: String,
  lrn: String,
  mrn: Option[String] = None,
  ducr: Option[String] = None,
  actions: Seq[Action] = Seq.empty
) {
  require(actions.nonEmpty, "Submission must have at least one action")

  val latestAction: Action = actions.minBy(_.requestTimestamp)(Submission.localDateTimeOrdering)
}

object Submission {
  val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan[LocalDateTime]((a, b) => a.isBefore(b))

  implicit val formats = Json.format[Submission]

  implicit val ordering: Ordering[Submission] = Ordering.by[Submission, LocalDateTime](
    submission => submission.latestAction.requestTimestamp
  )(localDateTimeOrdering)

  val newestEarlierOrdering: Ordering[Submission] = ordering.reverse
}
