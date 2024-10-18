/*
 * Copyright 2024 HM Revenue & Customs
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

import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.Submission

object DeclarationDetailsHelper {

  def displayViewDeclarationLink(submission: Submission): Boolean = !(submission.latestEnhancedStatus contains ERRORS)

  def isDeclarationRejected(submission: Submission): Boolean =
    submission.latestEnhancedStatus.fold(false)(rejectedStatuses.contains)

  def mrnIfAccepted(submission: Submission): Option[String] =
    if (submission.isStatusAcceptedOrReceived) submission.mrn else None

  def mrnIfEadStatus(submission: Submission): Option[String] = {
    val hasEadAcceptableStatus = submission.allSubmissionRequestStatuses.exists(_ in eadAcceptableStatuses)
    if (hasEadAcceptableStatus) submission.mrn else None
  }
}
