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

package views.messaging

import base.OverridableInjector
import config.featureFlags.SecureMessagingConfig
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.dashboard.DashboardHelper.toDashboard
import views.declaration.spec.UnitViewSpec
import views.html.messaging.inbox_wrapper

import scala.jdk.CollectionConverters.IteratorHasAsScala

class InboxWrapperSpec extends UnitViewSpec with BeforeAndAfterEach {

  private val injector = new OverridableInjector(bind[SecureMessagingConfig].toInstance(mockSecureMessagingConfig))

  private val inboxWrapperPage = injector.instanceOf[inbox_wrapper]
  private val partialContent = "Partial Content"

  private val view: Document = inboxWrapperPage(HtmlFormat.raw(partialContent))(FakeRequest(), messages)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)
  }

  "Inbox Wrapper page" should {

    "display page title" in {
      view.getElementsByTag("title").first must containMessage("inbox.heading")
    }

    "contain the navigation banner" when {
      "the Secure Messaging flag is set to 'true'" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
        val view = inboxWrapperPage(HtmlFormat.raw(partialContent))(FakeRequest(), messages)
        val banner = view.getElementById("navigation-banner")
        assert(Option(banner).isDefined && banner.childrenSize == 2)

        val elements = banner.children.iterator.asScala.toList

        assert(elements.head.tagName.toLowerCase == "a")
        elements.head must haveHref(toDashboard)

        assert(elements.last.tagName.toLowerCase == "span")
      }
    }

    "not contain the navigation banner" when {
      "the Secure Messaging flag is set to 'false'" in {
        Option(view.getElementById("navigation-banner")) mustBe None
      }
    }

    "display partial contents" in {
      view must containText(partialContent)
    }
  }
}
