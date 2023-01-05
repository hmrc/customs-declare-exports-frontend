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

object SecureMessagingFeatureStatus extends Enumeration {
  type SecureMessagingFeatureStatus = Value

  val disabled, sfus, exports = Value

  implicit object SecureMessagingFeatureStatusPathStringBinder
      extends PathBindable.Parsing[SecureMessagingFeatureStatus.SecureMessagingFeatureStatus](
        withName,
        _.toString,
        (k: String, e: Exception) => "Cannot parse %s as FeatureStatus: %s".format(k, e.getMessage)
      )

}
