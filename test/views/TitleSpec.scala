/*
 * Copyright 2019 HM Revenue & Customs
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

package views

import org.scalatest.MustMatchers
import views.declaration.spec.UnitViewSpec

class TitleSpec extends UnitViewSpec with MustMatchers {

  override val messages = realMessagesApi.preferred(request)
  val serviceName = messages("service.name")

  "Title" should {

    "format title without section" in {
      Title("some.title").toString(messages) must equal(s"some.title - $serviceName - GOV.UK")
    }

    "format title with section" in {
      Title("some.title", "some.section").toString(messages) must equal(
        s"some.title - some.section - $serviceName - GOV.UK"
      )
    }

  }
}
