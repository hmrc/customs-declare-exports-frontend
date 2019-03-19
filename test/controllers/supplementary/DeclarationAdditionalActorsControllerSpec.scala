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

import base.{CustomExportsBaseSpec, ViewValidator}
import controllers.supplementary.DeclarationAdditionalActorsControllerSpec.cacheWithMaximumAmountOfActors
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.supplementary.DeclarationAdditionalActors
import forms.supplementary.DeclarationAdditionalActorsSpec._
import helpers.views.supplementary.{CommonMessages, DeclarationAdditionalActorsMessages}
import models.declaration.supplementary.DeclarationAdditionalActorsData
import models.declaration.supplementary.DeclarationAdditionalActorsData.formId
import models.declaration.supplementary.DeclarationAdditionalActorsDataSpec._
import org.mockito.Mockito.reset
import play.api.test.Helpers._

class DeclarationAdditionalActorsControllerSpec
    extends CustomExportsBaseSpec with DeclarationAdditionalActorsMessages with CommonMessages with ViewValidator {

  val uri: String = uriWithContextPath("/declaration/supplementary/additional-actors")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[DeclarationAdditionalActorsData](None)
  }

  after {
    reset(mockCustomsCacheService)
  }

  "Declaration Additional Actors Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }
  }

  "Declaration Additional Actors Controller on POST" should {

    "handle save and continue action" should {

      "when validate form - optional data allowed" in {

        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/supplementary/holder-of-authorisation",
          actorsMap = Map("eori" -> "", "partyType" -> ""),
          action = saveAndContinueActionUrlEncoded
        )
      }

      "when validate form - correct values and an empty cache" in {

        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/supplementary/holder-of-authorisation",
          actorsMap = correctAdditionalActorsMap,
          action = saveAndContinueActionUrlEncoded
        )
      }

      "when validate form - correct values and items in cache" in {
        withCaching[DeclarationAdditionalActorsData](Some(correctAdditionalActorsData), formId)

        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/supplementary/holder-of-authorisation",
          actorsMap = Map("eori" -> "eori2", "partyType" -> "CS"),
          action = saveAndContinueActionUrlEncoded
        )
      }

      "not add an item and return BAD_REQUEST" should {

        "when adding actor with incorrect EORI and party not selected" in {

          val body = Map("eori" -> "12345678901234567890", "partyType" -> "").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(eoriError))
          page must include(messages(partyTypeEmpty))
        }

        "when adding actor with correct EORI and party not selected" in {

          val body = Map("eori" -> "1234", "partyType" -> "").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeEmpty))
        }

        "when adding actor with correct EORI and incorrect party" in {

          val body = Map("eori" -> "1234", "partyType" -> "Incorrect").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeError))
        }

        "when adding more than 99 items" in {
          withCaching[DeclarationAdditionalActorsData](Some(cacheWithMaximumAmountOfActors), formId)

          val body = Map("eori" -> "eori1", "partyType" -> "CS").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, maximumActorsError, "#")
        }

        "when adding duplicate item" in {
          withCaching[DeclarationAdditionalActorsData](Some(correctAdditionalActorsData), formId)

          val body = correctAdditionalActorsMap.toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplicatedActorsError, "#")
        }
      }
    }

    "handle remove action" should {

      "remove an actor successfully " when {

        "exists in the cache" in {
          withCaching[DeclarationAdditionalActorsData](Some(correctAdditionalActorsData), formId)

          val body = removeActionUrlEncoded(correctAdditionalActorsData.actors.head.toJson.toString())

          val result = route(app, postRequestFormUrlEncoded(uri, body)).get

          status(result) must be(SEE_OTHER)
        }
      }

      "return an error" when {

        "does not exists in the cache" in {

          val body = removeActionUrlEncoded(correctAdditionalActorsData.actors.head.toJson.toString())

          val result = route(app, postRequestFormUrlEncoded(uri, body)).get

          status(result) must be(BAD_REQUEST)
        }
      }
    }

    "handle add action" should {

      "when validate form - optional data allowed" in {

        val undefinedDocument: Map[String, String] = Map("eori" -> "", "partyType" -> "")
        testErrorScenario(
          addActionUrlEncoded,
          undefinedDocument,
          Some("supplementary.additionalActors.eori.isNotDefined")
        )
      }

      "when validate form - correct values" in {

        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/supplementary/additional-actors",
          actorsMap = correctAdditionalActorsMap,
          action = addActionUrlEncoded
        )
      }

      "not add an item and return BAD_REQUEST" should {

        "when adding actor with incorrect EORI and party not selected" in {

          val body = Map("eori" -> "12345678901234567890", "partyType" -> "").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(eoriError))
          page must include(messages(partyTypeEmpty))
        }

        "when adding actor with correct EORI and party not selected" in {

          val body = Map("eori" -> "1234", "partyType" -> "").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeEmpty))
        }

        "when adding actor with correct EORI and incorrect party" in {

          val body = Map("eori" -> "1234", "partyType" -> "Incorrect").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeError))
        }

        "when adding more than 99 items" in {
          withCaching[DeclarationAdditionalActorsData](Some(cacheWithMaximumAmountOfActors), formId)

          val body = Map("eori" -> "eori1", "partyType" -> "CS").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, maximumActorsError, "#")
        }

        "when adding duplicate item" in {
          withCaching[DeclarationAdditionalActorsData](Some(correctAdditionalActorsData), formId)

          val body = correctAdditionalActorsMap.toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplicatedActorsError, "#")
        }
      }

      "add an item successfully and return SEE_OTHER" when {

        "with an empty cache" in {
          withCaching[DeclarationAdditionalActorsData](None, formId)

          testHappyPathsScenarios(
            expectedPath = "/customs-declare-exports/declaration/supplementary/additional-actors",
            actorsMap = correctAdditionalActorsMap,
            action = addActionUrlEncoded
          )
        }

        "that does not exist in cache" in {
          withCaching[DeclarationAdditionalActorsData](Some(correctAdditionalActorsData), formId)

          testHappyPathsScenarios(
            expectedPath = "/customs-declare-exports/declaration/supplementary/additional-actors",
            actorsMap = Map("eori" -> "eori2", "partyType" -> "CS"),
            action = addActionUrlEncoded
          )
        }
      }
    }
  }

  private def testHappyPathsScenarios(
    expectedPath: String,
    actorsMap: Map[String, String],
    action: (String, String)
  ): Unit = {
    val body = actorsMap.toSeq :+ action
    val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

    val header = result.futureValue.header

    status(result) must be(SEE_OTHER)
    header.headers.get("Location") must be(Some(expectedPath))
  }

  private def testErrorScenario(
    action: (String, String),
    data: Map[String, String],
    maybeExpectedErrorMessagePath: Option[String]
  ): Unit = {

    val body = data.toSeq :+ action
    val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

    status(result) must be(BAD_REQUEST)

    maybeExpectedErrorMessagePath.fold() { expectedErrorMessagePath =>
      val stringResult = contentAsString(result)
      stringResult must include(messages(expectedErrorMessagePath))
    }
  }
}

object DeclarationAdditionalActorsControllerSpec {
  val cacheWithMaximumAmountOfActors = DeclarationAdditionalActorsData(
    Seq
      .range[Int](100, 200, 1)
      .map(elem => DeclarationAdditionalActors(Some(elem.toString), Some(elem.toString)))
  )
}
