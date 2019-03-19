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

package controllers.supplementary

import base.CustomExportsBaseSpec
import forms.supplementary.Document
import forms.supplementary.DocumentSpec._
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class PreviousDocumentsControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/declaration/supplementary/previous-documents")

  before {
    authorizedUser()
    withCaching[Document](None)
  }

  "Previous Documents Controller on GET" should {

    "return 200 status code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    // TODO: move to views
    "display previous documents form" in {

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.previousDocuments.title"))
      stringResult must include(messages("supplementary.previousDocuments.hint"))
      stringResult must include(messages("supplementary.previousDocuments.temporaryStorage"))
      stringResult must include(messages("supplementary.previousDocuments.simplifiedDeclaration"))
      stringResult must include(messages("supplementary.previousDocuments.previousDocument"))
      stringResult must include(messages("supplementary.previousDocuments.documentType"))
      stringResult must include(messages("supplementary.previousDocuments.documentReference"))
      stringResult must include(messages("supplementary.previousDocuments.goodsItemIdentifier"))
    }

    "display \"Back\" button that links to \"Transaction type\" page" in {

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/transaction-type"))
    }

    "display \"Save and continue\" button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }
  }

  "Previous Document Controller on POST" should {

    "validate form - empty values" in {

      val result = route(app, postRequest(uri, emptyPreviousDocumentsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.previousDocuments.documentCategory.empty"))
      stringResult must include(messages("supplementary.previousDocuments.documentType.empty"))
      stringResult must include(messages("supplementary.previousDocuments.documentReference.empty"))
    }

    "validate form - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectPreviousDocumentsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.previousDocuments.documentCategory.error"))
      stringResult must include(messages("supplementary.previousDocuments.documentType.error"))
      stringResult must include(messages("supplementary.previousDocuments.documentReference.error"))
      stringResult must include(messages("supplementary.previousDocuments.goodsItemIdentifier.error"))
    }

    "validate form and redirect - only mandatory fields" in {

      val result = route(app, postRequest(uri, mandatoryPreviousDocumentsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/supervising-office")
      )
    }

    "validate form and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctPreviousDocumentsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/supervising-office")
      )
    }
  }
}
