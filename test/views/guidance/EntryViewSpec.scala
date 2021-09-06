/*
 * Copyright 2021 HM Revenue & Customs
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

package views.guidance

import base.Injector
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.guidance.entry
import views.tags.ViewTest

@ViewTest
class EntryViewSpec extends UnitViewSpec with Stubs with Injector {

  private val entryPage = instanceOf[entry]
  private val view = entryPage()(request, messages)

  "'Entry' view" should {

    "display page header" in {
      view.getElementsByTag("h1").first must containMessage("guidance.entry.title")
    }
  }
}
