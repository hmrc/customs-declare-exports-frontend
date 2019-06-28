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

package helpers.views.declaration

trait SubmissionsMessages {

  private val prefix: String = "submissions"

  val title: String = prefix + ".title"
  val ducr: String = prefix + ".ducr"
  val lrn: String = prefix + ".lrn"
  val mrn: String = prefix + ".mrn"
  val submittedTimestamp: String = prefix + ".submittedTimestamp"
  val status: String = prefix + ".status"
  val notificationCount: String = prefix + ".noOfNotifications"
  val startNewDeclaration: String = "supplementary.startNewDec"

}
