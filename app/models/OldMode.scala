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

import models.OldMode.ErrorFix
import play.api.mvc.{JavascriptLiteral, QueryStringBindable}

sealed trait OldMode {
  val name: String

  def next: OldMode

  val isErrorFix: Boolean = this == ErrorFix
}
object OldMode {

  case object Normal extends OldMode {
    override val name: String = "Normal"

    override val next: OldMode = this
  }

  case object ErrorFix extends OldMode {
    override val name: String = "Error-Fix"

    override val next: OldMode = this

    val queryParameter: String = s"?mode=$name"
  }

  case object Draft extends OldMode {
    override val name: String = "Draft"

    override val next: OldMode = Normal
  }

  val modes: Set[OldMode] = Set[OldMode](Draft, ErrorFix)

  def withName(str: String): Option[OldMode] =
    modes.find(_.name == str)

  implicit val binder: QueryStringBindable[OldMode] = new QueryStringBindable[OldMode] {
    private val strBinder: QueryStringBindable[String] = implicitly[QueryStringBindable[String]]

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, OldMode]] =
      Some(
        Right(
          params
            .get(key)
            .flatMap(_.headOption)
            .flatMap(mode => OldMode.withName(mode))
            .getOrElse(OldMode.Normal)
        )
      )

    override def unbind(key: String, value: OldMode): String = strBinder.unbind(key, value.name)
  }

  implicit val jsLiteral: JavascriptLiteral[OldMode] = new JavascriptLiteral[OldMode] {
    override def to(value: OldMode): String = value.toString
  }
}
