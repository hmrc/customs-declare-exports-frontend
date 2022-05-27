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

import models.Mode.ErrorFix
import play.api.mvc.{JavascriptLiteral, QueryStringBindable}

sealed trait Mode {
  val name: String

  def next: Mode

  val isErrorFix: Boolean = this == ErrorFix
}
object Mode {

  case object Normal extends Mode {
    override val name: String = "Normal"

    override val next: Mode = this
  }

  case object Change extends Mode {
    override val name: String = "Change"

    override val next: Mode = Normal
  }

  case object Amend extends Mode {
    override val name: String = "Amend"

    override val next: Mode = this
  }

  case object ChangeAmend extends Mode {
    override val name: String = "Change-Amend"

    override val next: Mode = Amend
  }

  case object ErrorFix extends Mode {
    override val name: String = "Error-Fix"

    override val next: Mode = this

    val queryParameter: String = s"?mode=$name"
  }

  case object Draft extends Mode {
    override val name: String = "Draft"

    override val next: Mode = Normal
  }

  val modes: Set[Mode] = Set[Mode](Normal, Amend, Draft, Change, ChangeAmend, ErrorFix)

  def withName(str: String): Option[Mode] =
    modes.find(_.name == str)

  implicit val binder: QueryStringBindable[Mode] = new QueryStringBindable[Mode] {
    private val strBinder: QueryStringBindable[String] = implicitly[QueryStringBindable[String]]

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Mode]] =
      Some(
        Right(
          params
            .get(key)
            .flatMap(_.headOption)
            .flatMap(mode => Mode.withName(mode))
            .getOrElse(Mode.Normal)
        )
      )

    override def unbind(key: String, value: Mode): String = strBinder.unbind(key, value.name)
  }

  implicit val jsLiteral: JavascriptLiteral[Mode] = new JavascriptLiteral[Mode] {
    override def to(value: Mode): String = value.toString
  }
}
