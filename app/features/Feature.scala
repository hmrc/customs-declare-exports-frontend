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

package features

import play.api.mvc.PathBindable

object Feature extends Enumeration {
  type Feature = Value
  val betaBanner, default, ead, sfus, secureMessagingInbox, googleFormFeedbackLink, queryNotificationMessage, commodities, tdrUnauthorisedMessage =
    Value

  implicit object FeaturePathStringBinder
      extends PathBindable.Parsing[Feature.Feature](
        withName,
        _.toString,
        (k: String, e: Exception) => "Cannot parse %s as Feature: %s".format(k, e.getMessage)
      )

}
