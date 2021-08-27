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

import scala.collection.JavaConverters.asScalaIteratorConverter

import base.Injector
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.guidance.send_by_roro
import views.tags.ViewTest

@ViewTest
class SendByRoroViewSpec extends UnitViewSpec with Stubs with Injector {

  private val sendByRoroPage = instanceOf[send_by_roro]
  private val view = sendByRoroPage()(request, messages)

  "'Send By Roro' view" should {

    "display page header" in {
      view.getElementsByTag("h1").first must containMessage("guidance.roro.title")
    }

    "ordered list exist" in {
      val orderedList = view.getElementsByTag("ol")
      orderedList.size mustBe 1
    }
  }
}
