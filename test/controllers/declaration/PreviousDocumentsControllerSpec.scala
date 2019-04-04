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

package controllers.declaration

import base.{CustomExportsBaseSpec, TestHelper, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.Document._
import forms.declaration.{Document, PreviousDocumentsData}
import helpers.views.declaration.{CommonMessages, PreviousDocumentsMessages}
import play.api.test.Helpers._

class PreviousDocumentsControllerSpec
    extends CustomExportsBaseSpec with PreviousDocumentsMessages with CommonMessages with ViewValidator {
  import PreviousDocumentsControllerSpec._

  private val uri = uriWithContextPath("/declaration/previous-documents")
  private val addActionURLEncoded = (Add.toString, "")
  private val saveAndContinueActionURLEncoded = (SaveAndContinue.toString, "")
  private val removeActionURLEncoded: String => (String, String) = (value: String) => (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[PreviousDocumentsData](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Previous Documents Controller on GET" should {

    "return 200 status code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      withCaching[PreviousDocumentsData](
        Some(PreviousDocumentsData(Seq(Document("X", "HX", "XH", Some("UX"))))),
        formId
      )

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)

      page must include("X")
      page must include("HX")
      page must include("XH")
      page must include("UX")
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
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentCategoryEmpty, "#documentCategory")
        checkErrorLink(page, 2, documentTypeEmpty, "#documentType")
        checkErrorLink(page, 3, documentReferenceEmpty, "#documentReference")

        getElementByCss(page, "#error-message-documentCategory-input").text() must be(messages(documentCategoryEmpty))
        getElementByCss(page, "#error-message-documentType-input").text() must be(messages(documentTypeEmpty))
        getElementByCss(page, "#error-message-documentReference-input").text() must be(messages(documentReferenceEmpty))
      }

      "item doesn't contain document category" in {

        val body = documentWithoutCategory :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentCategoryEmpty, "#documentCategory")

        getElementByCss(page, "#error-message-documentCategory-input").text() must be(messages(documentCategoryEmpty))
      }

      "item doesn't contain document type" in {

        val body = documentWithoutType :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentTypeEmpty, "#documentType")

        getElementByCss(page, "#error-message-documentType-input").text() must be(messages(documentTypeEmpty))
      }

      "item doesn't contain document reference" in {

        val body = documentWithoutReference :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentReferenceEmpty, "#documentReference")

        getElementByCss(page, "#error-message-documentReference-input").text() must be(messages(documentReferenceEmpty))
      }

      "item contains incorrect document category" in {

        val body = documentWithIncorrectCategory :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentCategoryError, "#documentCategory")

        getElementByCss(page, "#error-message-documentCategory-input").text() must be(messages(documentCategoryError))
      }

      "item contains incorrect document type" in {

        val body = documentWithIncorrectType :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentTypeError, "#documentType")

        getElementByCss(page, "#error-message-documentType-input").text() must be(messages(documentTypeError))
      }

      "item contains incorrect document reference" in {

        val body = documentWithIncorrectReference :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentReferenceError, "#documentReference")

        getElementByCss(page, "#error-message-documentReference-input").text() must be(messages(documentReferenceError))
      }

      "item contains incorrect goods item identifier" in {

        val body = documentWithIncorrectIdentifier :+ addActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentGoodsIdentifierError, "#goodsItemIdentifier")

        getElementByCss(page, "#error-message-goodsItemIdentifier-input").text() must be(
          messages(documentGoodsIdentifierError)
        )
      }

      "item duplication in cache" in {

        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val body = correctDocument :+ addActionURLEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, duplication, "#")
      }

      "limit of items reached" in {

        withCaching[PreviousDocumentsData](Some(fullCache), formId)

        val body = Seq(
          ("documentCategory", "Y"),
          ("documentType", "1"),
          ("documentReference", "A"),
          ("goodsItemIdentifier", "1")
        ) :+ addActionURLEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, limit, "#")
      }
    }

    "display page with error during save and continue" when {

      "item doesn't contain any data and with empty cache - screen is mandatory" in {

        val body = emptyDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, continueMandatory, "#")
      }

      "item doesn't contain document category" in {

        val body = documentWithoutCategory :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentCategoryEmpty, "#documentCategory")

        getElementByCss(page, "#error-message-documentCategory-input").text() must be(messages(documentCategoryEmpty))
      }

      "item doesn't contain document type" in {

        val body = documentWithoutType :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentTypeEmpty, "#documentType")

        getElementByCss(page, "#error-message-documentType-input").text() must be(messages(documentTypeEmpty))
      }

      "item doesn't contain document reference" in {

        val body = documentWithoutReference :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentReferenceEmpty, "#documentReference")

        getElementByCss(page, "#error-message-documentReference-input").text() must be(messages(documentReferenceEmpty))
      }

      "item contains incorrect document category" in {

        val body = documentWithIncorrectCategory :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentCategoryError, "#documentCategory")

        getElementByCss(page, "#error-message-documentCategory-input").text() must be(messages(documentCategoryError))
      }

      "item contains incorrect document type" in {

        val body = documentWithIncorrectType :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentTypeError, "#documentType")

        getElementByCss(page, "#error-message-documentType-input").text() must be(messages(documentTypeError))
      }

      "item contains incorrect document reference" in {

        val body = documentWithIncorrectReference :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentReferenceError, "#documentReference")

        getElementByCss(page, "#error-message-documentReference-input").text() must be(messages(documentReferenceError))
      }

      "item contains incorrect goods item identifier" in {

        val body = documentWithIncorrectIdentifier :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, documentGoodsIdentifierError, "#goodsItemIdentifier")

        getElementByCss(page, "#error-message-goodsItemIdentifier-input").text() must be(
          messages(documentGoodsIdentifierError)
        )
      }

      "item duplication in cache" in {

        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val body = correctDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, duplication, "#")
      }

      "limit of items reached" in {

        withCaching[PreviousDocumentsData](Some(fullCache), formId)

        val body = Seq(
          ("documentCategory", "Y"),
          ("documentType", "1"),
          ("documentReference", "A"),
          ("goodsItemIdentifier", "1")
        ) :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val page = contentAsString(result)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, limit, "#")
      }
    }

    "redirect to the next page" when {

      "user provide correct item with empty cache" in {
        val body = correctDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supervising-office"))
      }

      "user has empty form but cache contains some item" in {
        withCaching[PreviousDocumentsData](Some(cachedData), formId)

        val body = emptyDocument :+ saveAndContinueActionURLEncoded

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supervising-office"))
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
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supervising-office"))
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
  private val incorrectReference = TestHelper.createRandomAlphanumericString(referenceMaxLength + 1)

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
