/*
 * Copyright 2020 HM Revenue & Customs
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
  val default = Value

  implicit object featurePathStringBinder
      extends PathBindable.Parsing[Feature.Feature](
        withName(_),
        _.toString,
        (k: String, e: Exception) => "Cannot parse %s as Feature: %s".format(k, e.getMessage())
      )

}

object FeatureStatus extends Enumeration {
  type FeatureStatus = Value
  val enabled, disabled, suspended = Value

  implicit object featureStatusPathStringBinder
      extends PathBindable.Parsing[FeatureStatus.FeatureStatus](
        withName(_),
        _.toString,
        (k: String, e: Exception) => "Cannot parse %s as FeatureStatus: %s".format(k, e.getMessage())
      )

}
