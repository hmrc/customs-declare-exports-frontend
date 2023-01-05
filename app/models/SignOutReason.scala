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

sealed abstract class SignOutReason(val name: String)

object SignOutReason {
  case object SessionTimeout extends SignOutReason("SessionTimeout")
  case object UserAction extends SignOutReason("UserAction")

  val allSignOutReasons = Set(SessionTimeout, UserAction)

  implicit val binder: QueryStringBindable[SignOutReason] = new QueryStringBindable[SignOutReason] {
    private val strBinder: QueryStringBindable[String] = implicitly[QueryStringBindable[String]]

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SignOutReason]] =
      Some(
        Right(
          params
            .get(key)
            .flatMap(_.headOption)
            .flatMap(reason => allSignOutReasons.find(_.name == reason))
            .getOrElse(SignOutReason.SessionTimeout)
        )
      )

    override def unbind(key: String, value: SignOutReason): String = strBinder.unbind(key, value.name)
  }
}
