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

package views.helpers

import base.{Injector, OverridableInjector}
import connectors.CodeListConnector
import models.Pointer
import models.codes.DmsErrorCode
import models.declaration.notifications.NotificationError
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.twirl.api.Html
import services.cache.ExportsTestData
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Text}
import views.declaration.spec.UnitViewSpec
import views.helpers.NotificationErrorHelper.ErrorRow
import views.html.components.gds.paragraphBody

import java.util.Locale
import scala.collection.immutable.ListMap

class NotificationErrorHelperSpec extends UnitViewSpec with ExportsTestData with BeforeAndAfterEach with Injector {

  private val injector = new OverridableInjector()
  val paragraphBody = injector.instanceOf[paragraphBody]

  val codeListConnector = mock[CodeListConnector]

  val defaultLocale = Locale.UK
  val errorCode = "CDS12345"
  val errorDescription = "Oh no! Something is horribly wrong."
  val pointer = Pointer("")
  val notificationError = new NotificationError(errorCode, Some(pointer))

  val notificationErrorHelper = new NotificationErrorHelper(codeListConnector, paragraphBody)

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

  val code1 = "code1"
  val code2 = "code2"
  val code3 = "code3"

  "NotificationErrorHelper.groupRowsByErrorCode" should {
    val errors = List(
      ErrorRow(code1, None, "", List.empty, None),
      ErrorRow(code1, None, "", List.empty, None),
      ErrorRow(code2, None, "", List.empty, None),
      ErrorRow(code2, None, "", List.empty, None),
      ErrorRow(code3, None, "", List.empty, None),
      ErrorRow(code2, None, "", List.empty, None)
    )

    "preserve the order in which the errors appear" in {
      val result = notificationErrorHelper.groupRowsByErrorCode(errors)

      result.flatten mustBe errors
    }

    "group consecutive errors with the same code together" in {
      val result = notificationErrorHelper.groupRowsByErrorCode(errors)

      result.size mustBe 4
      result(0).forall(_.code == code1) mustBe true
      result(1).forall(_.code == code2) mustBe true
      result(2).forall(_.code == code3) mustBe true
      result(3).forall(_.code == code2) mustBe true
    }

    "handle no errors passed" in {
      val result = notificationErrorHelper.groupRowsByErrorCode(List.empty)

      result mustBe List.empty
    }
  }

  "NotificationErrorHelper.removeRepeatFieldNameAndDescriptions" should {
    val fieldName = "field X"
    val title = "An Error"
    val description = List(Text("description").asHtml)

    "for a group of errors that has no actions defined" should {
      val groupedErrors = List(
        List(
          ErrorRow(code1, Some(fieldName), title, description, None),
          ErrorRow(code1, Some(fieldName), title, description, None),
          ErrorRow(code1, Some(fieldName), title, description, None)
        )
      )

      "preserve the number of errors" in {
        val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

        result(0).size mustBe groupedErrors(0).size
      }

      "remove all fieldName and Description values from every error except for the first error in the group" in {
        val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

        hasFieldNameAndDescription(result(0).head) mustBe true
        result(0).tail.forall(!hasFieldNameAndDescription(_)) mustBe true
      }
    }

    val action = Some(Actions())

    "for a group of errors that has one action defined" that {
      "has an action defined just in the first position" should {
        "remove the fieldName and Description values from every error except for the first" in {
          val groupedErrors = List(
            List(
              ErrorRow(code1, Some(fieldName), title, description, action),
              ErrorRow(code1, Some(fieldName), title, description, None),
              ErrorRow(code1, Some(fieldName), title, description, None)
            )
          )

          val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

          hasFieldNameAndDescription(result(0).head) mustBe true
          result(0).tail.forall(!hasFieldNameAndDescription(_)) mustBe true
        }
      }

      "has an action defined just in the second position" should {
        "remove the fieldName and Description values from every error except for the second" in {
          val groupedErrors = List(
            List(
              ErrorRow(code1, Some(fieldName), title, description, None),
              ErrorRow(code1, Some(fieldName), title, description, action),
              ErrorRow(code1, Some(fieldName), title, description, None)
            )
          )

          val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

          hasFieldNameAndDescription(result(0)(1)) mustBe true
          (result(0).take(1) ++: result(0).takeRight(1)).forall(!hasFieldNameAndDescription(_)) mustBe true
        }
      }

      "has an action defined just in the last position" should {
        "remove the fieldName and Description values from every error except for the last" in {
          val groupedErrors = List(
            List(
              ErrorRow(code1, Some(fieldName), title, description, None),
              ErrorRow(code1, Some(fieldName), title, description, None),
              ErrorRow(code1, Some(fieldName), title, description, action)
            )
          )

          val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

          hasFieldNameAndDescription(result(0).last) mustBe true
          result(0).dropRight(1).forall(!hasFieldNameAndDescription(_)) mustBe true
        }
      }
    }

    "for a group of errors that has more one action defined" that {
      "has an action defined just in the first and second positions" should {
        "remove the fieldName and Description values from every error except for the first" in {
          val groupedErrors = List(
            List(
              ErrorRow(code1, Some(fieldName), title, description, action),
              ErrorRow(code1, Some(fieldName), title, description, action),
              ErrorRow(code1, Some(fieldName), title, description, None)
            )
          )

          val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

          hasFieldNameAndDescription(result(0).head) mustBe true
          result(0).tail.forall(!hasFieldNameAndDescription(_)) mustBe true
        }
      }

      "has an action defined just in the second and third positions" should {
        "remove the fieldName and Description values from every error except for the second" in {
          val groupedErrors = List(
            List(
              ErrorRow(code1, Some(fieldName), title, description, None),
              ErrorRow(code1, Some(fieldName), title, description, action),
              ErrorRow(code1, Some(fieldName), title, description, action)
            )
          )

          val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

          hasFieldNameAndDescription(result(0)(1)) mustBe true
          (result(0).take(1) ++: result(0).takeRight(1)).forall(!hasFieldNameAndDescription(_)) mustBe true
        }
      }
    }

    "for a group of errors that all have actions defined" should {
      "remove the fieldName and Description values from every error except for the first" in {
        val groupedErrors = List(
          List(
            ErrorRow(code1, Some(fieldName), title, description, action),
            ErrorRow(code1, Some(fieldName), title, description, action),
            ErrorRow(code1, Some(fieldName), title, description, action)
          )
        )

        val result = notificationErrorHelper.removeRepeatFieldNameAndDescriptions(groupedErrors)

        hasFieldNameAndDescription(result(0).head) mustBe true
        result(0).tail.forall(!hasFieldNameAndDescription(_)) mustBe true
      }
    }
  }

  private def hasFieldNameAndDescription(errorRow: ErrorRow): Boolean =
    errorRow.fieldName.isDefined && !errorRow.description.isEmpty
}
