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

package base

import org.scalatest.enablers.Containing
import play.api.data.FormError

trait FormSpec extends UnitSpec {
  implicit val formErrorsContaining: Containing[Seq[FormError]] = ErrorListContaining
}

object ErrorListContaining extends Containing[Seq[FormError]] {
  override def contains(container: Seq[FormError], element: Any): Boolean =
    element match {
      case error: FormError   => container.contains(error)
      case messageKey: String => container.exists(_.message == messageKey)
    }

  override def containsOneOf(container: Seq[FormError], elements: Seq[Any]): Boolean = ???

  override def containsNoneOf(container: Seq[FormError], elements: Seq[Any]): Boolean = ???
}
