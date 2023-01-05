/*
 * Copyright 2023 HM Revenue & Customs
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

package views.helpers

import models.declaration.submissions.EnhancedStatus.{CUSTOMS_POSITION_DENIED, CUSTOMS_POSITION_GRANTED, EnhancedStatus}

object CancellationResultHelper {

  def getTitleForStatus(status: Option[EnhancedStatus]): String =
    status match {
      case Some(CUSTOMS_POSITION_GRANTED) => "cancellation.result.cancelled.title"
      case Some(CUSTOMS_POSITION_DENIED)  => "cancellation.result.denied.title"
      case _                              => "cancellation.result.unprocessed.title"
    }

  def getParagraphsForStatus(status: Option[EnhancedStatus]): Seq[String] =
    status match {
      case Some(CUSTOMS_POSITION_GRANTED) => Seq("cancellation.result.cancelled.p1", "cancellation.result.cancelled.p2")
      case Some(CUSTOMS_POSITION_DENIED)  => Seq("cancellation.result.denied.p1")
      case _                              => Seq("cancellation.result.unprocessed.p1")
    }
}
