/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.common

import forms.common.LightFormMatchers.ErrorHasMessage
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher, MatchResult, Matcher}
import play.api.data.{Form, FormError}

trait LightFormMatchers {
  def haveMessage(right: String) = new ErrorHasMessage(right)

  val errorless: BePropertyMatcher[Form[_]] = (form: Form[_]) => BePropertyMatchResult(!form.hasErrors, "errorless")
}

object LightFormMatchers {

  class ErrorHasMessage(right: String) extends Matcher[Option[FormError]] {

    override def apply(left: Option[FormError]): MatchResult =
      MatchResult(left.exists(_.message == right), s""""$left" does not contains message "$right"""", s""""$left contains message "$right"""")
  }
}
