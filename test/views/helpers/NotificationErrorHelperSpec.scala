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

package views.helpers

import base.{Injector, OverridableInjector}
import config.featureFlags.ChangeErrorLinkConfig
import connectors.CodeListConnector
import models.codes.DmsErrorCode
import models.declaration.notifications.NotificationError
import models.Pointer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.twirl.api.Html
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.{heading, paragraphBody}

import java.util.Locale
import scala.collection.immutable.ListMap

class NotificationErrorHelperSpec extends UnitViewSpec with ExportsTestData with BeforeAndAfterEach with Injector {

  private val injector = new OverridableInjector()
  private val heading = injector.instanceOf[heading]
  val paragraphBody = injector.instanceOf[paragraphBody]

  val codeListConnector = mock[CodeListConnector]
  val changeErrorLinkConfig = mock[ChangeErrorLinkConfig]

  val defaultLocale = Locale.UK
  val errorCode = "CDS12345"
  val errorDescription = "Oh no! Something is horribly wrong."
  val pointer = Pointer("")
  val notificationError = new NotificationError(errorCode, Some(pointer))

  val notificationErrorHelper = new NotificationErrorHelper(codeListConnector, changeErrorLinkConfig, heading, paragraphBody)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(codeListConnector.getDmsErrorCodesMap(any()))
      .thenReturn(ListMap(errorCode -> DmsErrorCode(errorCode, errorDescription)))
  }

  override def afterEach(): Unit =
    reset(codeListConnector)

  "NotificationErrorHelper.formattedErrorDescription" should {
    "return default description if error has not specific description" in {
      notificationErrorHelper
        .formattedErrorDescription(new NotificationError("unknown", Some(pointer)))
        .head
        .body mustBe s"${messages("error.unknown")}."
    }

    "return the given error description for a code" in {
      notificationErrorHelper.formattedErrorDescription(notificationError).head.body mustBe errorDescription
    }

    "add a full stop char to the end of an error description" when {
      "description is missing one" in {
        when(codeListConnector.getDmsErrorCodesMap(any()))
          .thenReturn(ListMap(errorCode -> DmsErrorCode(errorCode, errorDescription.dropRight(1))))

        notificationErrorHelper.formattedErrorDescription(notificationError).head.body mustBe errorDescription
      }
    }

    "not add a full stop char to the end of an error description" when {
      "description has an ending full stop" in {
        notificationErrorHelper.formattedErrorDescription(notificationError).head.body mustBe errorDescription
      }

      "description has an ending question mark" in {
        val correctlyTerminatedDescription = s"${errorDescription.dropRight(1)}?"
        when(codeListConnector.getDmsErrorCodesMap(any()))
          .thenReturn(ListMap(errorCode -> DmsErrorCode(errorCode, correctlyTerminatedDescription)))

        notificationErrorHelper.formattedErrorDescription(notificationError).head.body mustBe correctlyTerminatedDescription
      }

      "description has an ending exclamation mark" in {
        val correctlyTerminatedDescription = s"${errorDescription.dropRight(1)}!"
        when(codeListConnector.getDmsErrorCodesMap(any()))
          .thenReturn(ListMap(errorCode -> DmsErrorCode(errorCode, correctlyTerminatedDescription)))

        notificationErrorHelper.formattedErrorDescription(notificationError).head.body mustBe correctlyTerminatedDescription
      }

      "description is empty" in {
        val correctlyTerminatedDescription = ""
        when(codeListConnector.getDmsErrorCodesMap(any()))
          .thenReturn(ListMap(errorCode -> DmsErrorCode(errorCode, correctlyTerminatedDescription)))

        notificationErrorHelper.formattedErrorDescription(notificationError).head.body mustBe correctlyTerminatedDescription
      }
    }

    "not add a trailing <br> html element to a single line error description" in {
      val htmlOutput = notificationErrorHelper.formattedErrorDescription(notificationError)

      htmlOutput.size mustBe 1
      htmlOutput.head.body mustBe errorDescription
    }

    "convert newline characters in a description to <br> html elements" when {
      val breakLine = Html("<br>")

      "description contains one newline characters" in {
        val errorDescription = "This description has\ntwo lines."
        when(codeListConnector.getDmsErrorCodesMap(any()))
          .thenReturn(ListMap(errorCode -> DmsErrorCode(errorCode, errorDescription)))

        val htmlOutput = notificationErrorHelper.formattedErrorDescription(notificationError)

        htmlOutput.size mustBe 3
        htmlOutput(0).body mustBe "This description has"
        htmlOutput(1) mustBe breakLine
        htmlOutput(2).body mustBe "two lines."
      }

      "description contains many newline characters" in {
        val errorDescription = "This description has\n\n\nthree\nlines."
        when(codeListConnector.getDmsErrorCodesMap(any()))
          .thenReturn(ListMap(errorCode -> DmsErrorCode(errorCode, errorDescription)))

        val htmlOutput = notificationErrorHelper.formattedErrorDescription(notificationError)
        htmlOutput.size mustBe 7
        htmlOutput(0).body mustBe "This description has"
        htmlOutput(1) mustBe breakLine
        htmlOutput(2) mustBe breakLine
        htmlOutput(3) mustBe breakLine
        htmlOutput(4).body mustBe "three"
        htmlOutput(5) mustBe breakLine
        htmlOutput(6).body mustBe "lines."
      }
    }
  }
}
