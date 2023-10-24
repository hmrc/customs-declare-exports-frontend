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

package models.viewmodels.errors

import base.UnitSpec
import connectors.CodeListConnector
import models.codes.DmsErrorCode
import models.declaration.errors.{ErrorInstance, FieldInvolved}
import models.Pointer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.Html
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.link

import scala.collection.immutable.ListMap

class DefaultInterpreterSpec extends UnitSpec with UnitViewSpec with ExportsDeclarationBuilder with ExportsItemBuilder with MockitoSugar {

  implicit val codeListConnector = mock[CodeListConnector]
  implicit val linkRef = new link()

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

  "DefaultInterpreter's generateHtmlFor method is called" that {
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
        val field = FieldInvolved(
          Pointer("declaration.items.#1.additionalDocument.#2.documentStatus"),
          Some(originalVal),
          Some(draftVal),
          Some(Html(dummyLink)),
          None
        )
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
        html must include(dummyLink.toString())
      }
    }

    "picks a specialised interpreter" when {
      "error is for error code CDS12062" in {
        val errorCode = "CDS12062"
        val error = ErrorInstance(declaration, 2, errorCode, Seq.empty[FieldInvolved])

        val html = DefaultInterpreter.generateHtmlFor(error).toString

        val errorDescription = messages(s"dmsError.$errorCode.title")

        html must include("Error 2")
        html must include(errorDescription)
        html must not include govTable
      }

      "error is for error code CDS12119" in {
        val errorCode = "CDS12119"
        val error = ErrorInstance(declaration, 1, errorCode, Seq.empty[FieldInvolved])

        val html = DefaultInterpreter.generateHtmlFor(error).toString

        val errorDescription = messages(s"dmsError.$errorCode.title")

        html must include("Error 1")
        html must include(errorDescription)
        html must include(govTable)
      }
    }
  }
}
