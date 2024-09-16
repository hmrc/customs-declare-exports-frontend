/*
 * Copyright 2024 HM Revenue & Customs
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

package models.viewmodels.errors

import base.UnitSpec
import connectors.CodeListConnector
import models.Pointer
import models.codes.DmsErrorCode
import models.declaration.errors.{ErrorInstance, FieldInvolved}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.twirl.api.Html
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import views.common.UnitViewSpec
import views.html.components.gds.link

import scala.collection.immutable.ListMap

class DefaultInterpreterSpec extends UnitSpec with UnitViewSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with ExportsItemBuilder {

  implicit val codeListConnector: CodeListConnector = mock[CodeListConnector]
  implicit val linkRef: link = new link()

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(codeListConnector.getDmsErrorCodesMap(any())).thenReturn(ListMap("CDS12056" -> DmsErrorCode("CDS12056", "Two values are incompatible")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(codeListConnector)
  }

  val declaration = aDeclaration(withItems(2))

  val govTable = """<table class="govuk-table">"""
  val colHeader2 = messages("rejected.notification.fieldTable.column.2.title")
  val colHeader3 = messages("rejected.notification.fieldTable.column.3.title")
  val dummyLink = """<a href="">Change</a>"""
  val fieldPointer = "declaration.items.#1.additionalDocument.#2.documentStatus"
  val APCFieldPointer = "declaration.items.#1.procedureCodes.additionalProcedureCodes.#2"

  "DefaultInterpreter.generateHtmlFor" should {

    "displays no errors" when {
      "is passed an ErrorInstance with no fields" in {
        val errorCode = "CDS12056"
        val error = ErrorInstance(declaration, 1, errorCode, Seq.empty[FieldInvolved])

        val html = DefaultInterpreter.generateHtmlFor(error).toString

        val errorDescription = messages(s"dmsError.$errorCode.title")
        html must include("Error 1")
        html must include(errorDescription)
        html must not include govTable
      }
    }

    "displays error fields" when {
      "is passed an ErrorInstance with fields" in {
        val errorCode = "CDS12056"
        val originalVal = "ABC"
        val draftVal = "CBA"
        val field = FieldInvolved(Pointer(fieldPointer), Some(originalVal), Some(draftVal), Some(Html(dummyLink)), None)
        val error = ErrorInstance(declaration, 1, errorCode, Seq(field))

        val html = DefaultInterpreter.generateHtmlFor(error).toString

        val errorDescription = messages(s"dmsError.$errorCode.title")

        html must include("Error 1")
        html must include(errorDescription)
        html must include(govTable)
        html must include(colHeader2)
        html must include(colHeader3)
        html must include(originalVal)
        html must include(draftVal)
        html must include(dummyLink)
        html must include(fieldPointer.replaceAll("\\.#?", "-"))
      }
    }

    "picks a specialised interpreter" when {
      "error is for error code CDS12119" when {
        "Error is an amendment error" in {
          val errorCode = "CDS12119"
          val error = ErrorInstance(declaration, 1, errorCode, Seq.empty[FieldInvolved], true)

          val html = DefaultInterpreter.generateHtmlFor(error).toString

          val errorDescription = messages(s"dmsError.$errorCode.title")

          html must include("Error 1")
          html must include(errorDescription)
          html must include(govTable)
          html must include("isAmendment=true")
        }

        "Error is not an amendment error" in {
          val errorCode = "CDS12119"
          val error = ErrorInstance(declaration, 1, errorCode, Seq.empty[FieldInvolved], false)

          val html = DefaultInterpreter.generateHtmlFor(error).toString

          val errorDescription = messages(s"dmsError.$errorCode.title")

          html must include("Error 1")
          html must include(errorDescription)
          html must include(govTable)
          html must include("isAmendment=false")
        }
      }
    }

    "corrects sequence arg of APC pointer to display a user friendly value" in {
      val errorCode = "CDS12056"
      val originalVal = "000"
      val draftVal = "1CS"
      val field = FieldInvolved(Pointer(APCFieldPointer), Some(originalVal), Some(draftVal), Some(Html(dummyLink)), None)
      val error = ErrorInstance(declaration, 1, errorCode, Seq(field))

      val html = DefaultInterpreter.generateHtmlFor(error).toString

      html must include(messages(field.pointer.messageKey, field.pointer.sequenceArgs: _*))
    }
  }
}
