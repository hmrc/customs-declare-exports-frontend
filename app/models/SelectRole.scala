/*
 * Copyright 2018 HM Revenue & Customs
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

package models

import utils.{Enumerable, RadioOption, WithName}

sealed trait SelectRole

object SelectRole {

  case object Webloaderarrivinggoods extends WithName("webLoaderArrivingGoods") with SelectRole
  case object Webloaderdepartinggoods extends WithName("webLoaderDepartingGoods") with SelectRole

  val values: Set[SelectRole] = Set(
    Webloaderarrivinggoods, Webloaderdepartinggoods
  )

  val options: Set[RadioOption] = values.map {
    value =>
      RadioOption("selectRole", value.toString)
  }

  implicit val enumerable: Enumerable[SelectRole] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
