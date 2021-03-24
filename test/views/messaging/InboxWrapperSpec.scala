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

package views.messaging

import base.Injector
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.declaration.spec.UnitViewSpec
import views.html.messaging.inbox_wrapper

class InboxWrapperSpec extends UnitViewSpec with Injector {

  private val inboxWrapperPage = instanceOf[inbox_wrapper]
  private val partialContent = "Partial Content"

  private val view: Document = inboxWrapperPage(HtmlFormat.raw(partialContent))(FakeRequest(), messages)

  "Inbox Wrapper page" should {

    "display page title" in {
      view.getElementsByTag("title").first must containMessage("inbox.heading")
    }

    "display partial contents" in {
      view must containText(partialContent)
    }
  }
}
