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
import forms.supplementary.PreviousDocuments
import forms.supplementary.PreviousDocuments.AllowedValues.TemporaryStorage
import forms.supplementary.PreviousDocumentsSpec._
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class PreviousDocumentsControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/previous-documents")

  "Previous documents controller" should {
    "display previous documents form" in {
      authorizedUser()
      withCaching[PreviousDocuments](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.previousDocuments.title"))
      stringResult must include(messages("supplementary.previousDocuments.documentCategory"))
      stringResult must include(messages("supplementary.previousDocuments.temporaryStorage"))
      stringResult must include(messages("supplementary.previousDocuments.simplifiedDeclaration"))
      stringResult must include(messages("supplementary.previousDocuments.previousDocument"))
      stringResult must include(messages("supplementary.previousDocuments.documentType"))
      stringResult must include(messages("supplementary.previousDocuments.documentReference"))
      stringResult must include(messages("supplementary.previousDocuments.goodsItemIdentifier"))
    }

    "valid form - empty values" in {
      authorizedUser()
      withCaching[PreviousDocuments](None)

      val result = route(app, postRequest(uri, emptyPreviousDocumentsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.previousDocuments.documentCategory.empty"))
      stringResult must include(messages("supplementary.previousDocuments.documentType.empty"))
      stringResult must include(messages("supplementary.previousDocuments.documentReference.empty"))
    }

    "valid form - incorrect values" in {
      authorizedUser()
      withCaching[PreviousDocuments](None)

      val result = route(app, postRequest(uri, incorrectPreviousDocumentsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.previousDocuments.documentCategory.error"))
      stringResult must include(messages("supplementary.previousDocuments.documentType.error"))
      stringResult must include(messages("supplementary.previousDocuments.documentReference.error"))
      stringResult must include(messages("supplementary.previousDocuments.goodsItemIdentifier.error"))
    }

    "valid form - correct values" in {
      authorizedUser()
      withCaching[PreviousDocuments](None)

      val correctPreviousDocuments: JsValue = JsObject(
        Map(
          "documentCategory" -> JsString(TemporaryStorage),
          "documentType" -> JsString("ABC"),
          "documentReference" -> JsString("DocumentReference"),
          "goodsItemIdentifier" -> JsString("123")
        )
      )
      val result = route(app, postRequest(uri, correctPreviousDocumentsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-information")
      )
    }
  }
}
