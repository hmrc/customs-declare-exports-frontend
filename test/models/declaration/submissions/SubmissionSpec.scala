/*
 * Copyright 2022 HM Revenue & Customs
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

import base.UnitSpec
import testdata.SubmissionsTestData.{action, actionCancellation, actionCancellation_2, action_2, action_3, submission}

class SubmissionSpec extends UnitSpec {

  "latestAction value" should {
    val actions = Seq(action, action_2, action_3, actionCancellation, actionCancellation_2)
    val submissisionWithActions = submission.copy(actions = actions)
    val submissionWithNoActions = submission.copy(actions = Seq.empty)

    "return most recent action of any type" in {
      submissisionWithActions.latestAction mustBe Some(action_2)
    }

    "return most recent action for a cancellation" in {
      submissisionWithActions.latestCancellationAction mustBe Some(actionCancellation_2)
    }

    "return nothing if no actions on submission" in {
      submissionWithNoActions.latestAction mustBe None
      submissionWithNoActions.latestCancellationAction mustBe None
    }
  }

}
