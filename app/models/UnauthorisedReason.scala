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

package models

import play.api.mvc.QueryStringBindable

sealed abstract class UnauthorisedReason(val name: String)

object UnauthorisedReason {
  case object UserIsAgent extends UnauthorisedReason("UserIsAgent")
  case object UserEoriNotAllowed extends UnauthorisedReason("UserEoriNotAllowed")
  case object UserIsNotEnrolled extends UnauthorisedReason("UserIsNotEnrolled")
  case object UrlDirect extends UnauthorisedReason("DirectToUrl")

  val allUnauthorisedReasons = Set(UserIsAgent, UserEoriNotAllowed, UserIsNotEnrolled)

  implicit val binder: QueryStringBindable[UnauthorisedReason] = new QueryStringBindable[UnauthorisedReason] {
    private val strBinder: QueryStringBindable[String] = implicitly[QueryStringBindable[String]]

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, UnauthorisedReason]] =
      Some(
        Right(
          params
            .get(key)
            .flatMap(_.headOption)
            .flatMap(reason => allUnauthorisedReasons.find(_.name == reason))
            .getOrElse(UrlDirect)
        )
      )

    override def unbind(key: String, value: UnauthorisedReason): String = strBinder.unbind(key, value.name)
  }
}
