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

import base.{CustomExportsBaseSpec, TestHelper}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.supplementary.Document._
import forms.supplementary.{Document, PreviousDocumentsData}
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class PreviousDocumentsControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  import PreviousDocumentsControllerSpec._

  val uri = uriWithContextPath("/declaration/supplementary/previous-documents")
  private val addActionURLEncoded = (Add.toString, "")
  private val saveAndContinueActionURLEncoded = (SaveAndContinue.toString, "")
  private val removeActionURLEncoded: String => (String, String) = (value: String) => (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[PreviousDocumentsData](None)
  }

  "Previous Documents Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "display previous documents form with added items" in {
      withCaching[PreviousDocumentsData](Some(cachedData), formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.previousDocuments." + document.documentCategory))
      stringResult must include(document.documentType)
      stringResult must include(document.documentReference)
      stringResult must include(document.goodsItemIdentifier.get)
    }
  }

  "Previous Documents Controller on POST" should {

    "add an item successfully" when {

      "cache is empty" in {
        val body = correctDocument :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "item is not duplicated" in {
        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val document =
          Seq(
            ("documentCategory", "Y"),
            ("documentType", "2"),
            ("documentReference", "B"),
            ("goodsItemIdentifier", "2")
          )
        val body = document :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove an item successfully" when {

      "exists in cache" in {
        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val body = removeActionURLEncoded("0")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "display page with an error during add" when {

      "item doesn't contain any data" in {
        val body = emptyDocument :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("supplementary.previousDocuments.documentCategory.error.empty"))
        stringResult must include(messages("supplementary.previousDocuments.documentType.empty"))
        stringResult must include(messages("supplementary.previousDocuments.documentReference.empty"))
      }

      "item doesn't contain document category" in {
        val body = documentWithoutCategory :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentCategory.error.empty"))
      }

      "item doesn't contain document type" in {
        val body = documentWithoutType :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentType.empty"))
      }

      "item doesn't contain document reference" in {
        val body = documentWithoutReference :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentReference.empty"))
      }

      "item contains incorrect document category" in {
        val body = documentWithIncorrectCategory :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.previousDocuments.documentCategory.error.incorrect")
        )
      }

      "item contains incorrect document type" in {
        val body = documentWithIncorrectType :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentType.error"))
      }

      "item contains incorrect document reference" in {
        val body = documentWithIncorrectReference :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentReference.error"))
      }

      "item contains incorrect goods item identifier" in {
        val body = documentWithIncorrectIdentifier :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.goodsItemIdentifier.error"))
      }

      "item duplication in cache" in {
        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val body = correctDocument :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.duplication"))
      }

      "limit of items reached" in {
        withCaching[PreviousDocumentsData](Some(fullCache), formId)

        val body = correctDocument :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.limit"))
      }
    }

    "display page with error during save and continue" when {

      "item doesn't contain any data and with empty cache - screen is mandatory" in {
        val body = emptyDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.continue.mandatory"))
      }

      "item doesn't contain document category" in {
        val body = documentWithoutCategory :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentCategory.error.empty"))
      }

      "item doesn't contain document type" in {
        val body = documentWithoutType :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentType.empty"))
      }

      "item doesn't contain document reference" in {
        val body = documentWithoutReference :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentReference.empty"))
      }

      "item contains incorrect document category" in {
        val body = documentWithIncorrectCategory :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.previousDocuments.documentCategory.error.incorrect")
        )
      }

      "item contains incorrect document type" in {
        val body = documentWithIncorrectType :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentType.error"))
      }

      "item contains incorrect document reference" in {
        val body = documentWithIncorrectReference :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.documentReference.error"))
      }

      "item contains incorrect goods item identifier" in {
        val body = documentWithIncorrectIdentifier :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.previousDocuments.goodsItemIdentifier.error"))
      }

      "item duplication in cache" in {
        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val body = correctDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.duplication"))
      }

      "limit of items reached" in {
        withCaching[PreviousDocumentsData](Some(fullCache), formId)

        val body = correctDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.limit"))
      }
    }

    "redirect to the next page" when {

      "user provide correct item with empty cache" in {
        val body = correctDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/supervising-office")
        )
      }

      "user has empty form but cache contains some item" in {
        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val body = emptyDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/supervising-office")
        )
      }

      "user provide correct item with different item in cache" in {
        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val document =
          Seq(
            ("documentCategory", "Y"),
            ("documentType", "2"),
            ("documentReference", "B"),
            ("goodsItemIdentifier", "2")
          )
        val body = document :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/supervising-office")
        )
      }
    }
  }
}

object PreviousDocumentsControllerSpec {
  val correctDocument =
    Seq(("documentCategory", "X"), ("documentType", "1"), ("documentReference", "A"), ("goodsItemIdentifier", "1"))

  val emptyDocument =
    Seq(("documentCategory", ""), ("documentType", ""), ("documentReference", ""), ("goodsItemIdentifier", ""))

  val documentWithoutCategory =
    Seq(("documentCategory", ""), ("documentType", "1"), ("documentReference", "A"), ("goodsItemIdentifier", "1"))

  val documentWithoutType =
    Seq(("documentCategory", "X"), ("documentType", ""), ("documentReference", "A"), ("goodsItemIdentifier", "1"))

  val documentWithoutReference =
    Seq(("documentCategory", "X"), ("documentType", "1"), ("documentReference", ""), ("goodsItemIdentifier", "1"))

  val documentWithIncorrectCategory =
    Seq(("documentCategory", "A"), ("documentType", "1"), ("documentReference", "A"), ("goodsItemIdentifier", "1"))

  val documentWithIncorrectType =
    Seq(("documentCategory", "X"), ("documentType", "1234"), ("documentReference", "A"), ("goodsItemIdentifier", "1"))

  private val referenceMaxLength = 35
  private val incorrectReference = TestHelper.createRandomString(referenceMaxLength + 1)

  val documentWithIncorrectReference =
    Seq(
      ("documentCategory", "X"),
      ("documentType", "1"),
      ("documentReference", incorrectReference),
      ("goodsItemIdentifier", "1")
    )

  val documentWithIncorrectIdentifier =
    Seq(("documentCategory", "X"), ("documentType", "1"), ("documentReference", "A"), ("goodsItemIdentifier", "1234"))

  val document = Document("X", "1", "A", Some("1"))

  val cachedData = PreviousDocumentsData(Seq(document))

  val fullCache = PreviousDocumentsData(Seq.fill(PreviousDocumentsData.maxAmountOfItems)(document))
}
