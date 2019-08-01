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

import base.{CustomExportsBaseSpec, ViewValidator}
import controllers.declaration.DeclarationAdditionalActorsControllerSpec.cacheWithMaximumAmountOfActors
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.DeclarationAdditionalActors
import forms.declaration.DeclarationAdditionalActorsSpec._
import helpers.views.declaration.{CommonMessages, DeclarationAdditionalActorsMessages}
import models.declaration.DeclarationAdditionalActorsData
import models.declaration.DeclarationAdditionalActorsDataSpec._
import org.mockito.Mockito.reset
import play.api.test.Helpers._

class DeclarationAdditionalActorsControllerSpec
    extends CustomExportsBaseSpec with DeclarationAdditionalActorsMessages with CommonMessages with ViewValidator {

  private val uri: String = uriWithContextPath("/declaration/additional-actors")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  val exampleModel = aCacheModel(withChoice(SupplementaryDec))

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    withNewCaching(exampleModel)
  }

  override def afterEach() {
    super.afterEach()
    reset(mockExportsCacheService)
  }

  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  "Declaration Additional Actors Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri, sessionId = exampleModel.sessionId)).get

      status(result) must be(OK)
      verifyTheCacheIsUnchanged()
    }

    "read item from cache and display it" in {
      val sessionId = withCache(DeclarationAdditionalActorsData(Seq(DeclarationAdditionalActors(Some("112233"), Some("CS")))))

      val result = route(app, getRequest(uri, sessionId = sessionId)).get
      val page = contentAsString(result)

      status(result) must be(OK)

      page must include("112233")
      page must include("CS")
    }
  }

  "Declaration Additional Actors Controller on POST" should {

    "handle save and continue action" should {

      "when validate request - optional data allowed" in {

        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/holder-of-authorisation",
          actorsMap = Map("eori" -> "", "partyType" -> ""),
          action = saveAndContinueActionUrlEncoded
        )
      }

      "when validate request - correct values and an empty cache" in {

        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/holder-of-authorisation",
          actorsMap = correctAdditionalActorsMap,
          action = saveAndContinueActionUrlEncoded
        )
      }

      "when validate request - correct values and items in cache" in {

        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/holder-of-authorisation",
          actorsMap = Map("eori" -> "eori2", "partyType" -> "CS"),
          action = saveAndContinueActionUrlEncoded
        )
      }

      "not add an item and return BAD_REQUEST" should {

        "when adding actor with incorrect EORI and party not selected" in {

          val body = Map("eori" -> "12345678901234567890", "partyType" -> "").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get

          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(eoriError))
          page must include(messages(partyTypeEmpty))
          verifyTheCacheIsUnchanged()
        }

        "when adding actor with correct EORI and party not selected" in {

          val body = Map("eori" -> "1234", "partyType" -> "").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeEmpty))
          verifyTheCacheIsUnchanged()
        }

        "when adding actor with correct EORI and incorrect party" in {

          val body = Map("eori" -> "1234", "partyType" -> "Incorrect").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeError))
          verifyTheCacheIsUnchanged()
        }

        "when adding more than 99 items" in {
          withCache(cacheWithMaximumAmountOfActors)

          val body = Map("eori" -> "eori1", "partyType" -> "CS").toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, maximumActorsError, "#")
          verifyTheCacheIsUnchanged()
        }

        "when adding duplicate item" in {
          withCache(correctAdditionalActorsData)

          val body = correctAdditionalActorsMap.toSeq :+ saveAndContinueActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplicatedActorsError, "#")
          verifyTheCacheIsUnchanged()
        }
      }
    }

    "handle remove action" should {

      "remove an actor successfully " when {

        "exists in the cache" in {
          val sessionId = withCache(correctAdditionalActorsData)

          val body = removeActionUrlEncoded(correctAdditionalActorsData.actors.head.toJson.toString())

          val result = route(app, postRequestFormUrlEncoded(uri, sessionId)(body)).get

          status(result) must be(OK)
        }
      }

      "return an error" when {

        "does not exists in the cache" in {
          val body = removeActionUrlEncoded(correctAdditionalActorsData.actors.head.toJson.toString())

          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body)).get

          status(result) must be(BAD_REQUEST)
          verifyTheCacheIsUnchanged()
        }
      }
    }

    "handle add action" should {

      "when validate request - optional data allowed" in {
        val undefinedDocument: Map[String, String] = Map("eori" -> "", "partyType" -> "")
        testErrorScenario(
          addActionUrlEncoded,
          undefinedDocument,
          Some("supplementary.additionalActors.eori.isNotDefined")
        )
      }

      "when validate request - correct values" in {
        testHappyPathsScenarios(
          expectedPath = "/customs-declare-exports/declaration/additional-actors",
          actorsMap = correctAdditionalActorsMap,
          action = addActionUrlEncoded
        )
      }

      "not add an item and return BAD_REQUEST" should {

        "when adding actor with incorrect EORI and party not selected" in {

          val body = Map("eori" -> "12345678901234567890", "partyType" -> "").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get

          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(eoriError))
          page must include(messages(partyTypeEmpty))
          verifyTheCacheIsUnchanged()
        }

        "when adding actor with correct EORI and party not selected" in {

          val body = Map("eori" -> "1234", "partyType" -> "").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeEmpty))
          verifyTheCacheIsUnchanged()
        }

        "when adding actor with correct EORI and incorrect party" in {

          val body = Map("eori" -> "1234", "partyType" -> "Incorrect").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)
          page must include(messages(partyTypeError))
          verifyTheCacheIsUnchanged()
        }

        "when adding more than 99 items" in {
          val sessionId = withCache(cacheWithMaximumAmountOfActors)

          val body = Map("eori" -> "eori1", "partyType" -> "CS").toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, maximumActorsError, "#")
          verifyTheCacheIsUnchanged()
        }

        "when adding duplicate item" in {
          val sessionId = withCache(correctAdditionalActorsData)

          val body = correctAdditionalActorsMap.toSeq :+ addActionUrlEncoded
          val result = route(app, postRequestFormUrlEncoded(uri, sessionId)(body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplicatedActorsError, "#")
          verifyTheCacheIsUnchanged()
        }
      }

      "add an item successfully and return SEE_OTHER" when {

        "with an empty cache" in {
          testHappyPathsScenarios(
            expectedPath = "/customs-declare-exports/declaration/additional-actors",
            actorsMap = correctAdditionalActorsMap,
            action = addActionUrlEncoded
          )
        }

        "that does not exist in cache" in {
          testHappyPathsScenarios(
            expectedPath = "/customs-declare-exports/declaration/additional-actors",
            actorsMap = Map("eori" -> "eori2", "partyType" -> "CS"),
            action = addActionUrlEncoded
          )
        }
      }
    }
  }

  private def withCache(data: DeclarationAdditionalActorsData) = {
    val model = aCacheModel(withChoice("SMP"), withDeclarationAdditionalActors(data))
    withNewCaching(model)
    model.sessionId
  }

  private def testHappyPathsScenarios(
    expectedPath: String,
    actorsMap: Map[String, String],
    action: (String, String)
  ): Unit = {
    val body = actorsMap.toSeq :+ action
    val result = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*)).get

    status(result) must be(SEE_OTHER)
    redirectLocation(result) must be(Some(expectedPath))
  }

  private def testErrorScenario(
    action: (String, String),
    data: Map[String, String],
    maybeExpectedErrorMessagePath: Option[String],
    sessionId: String = exampleModel.sessionId
  ): Unit = {

    val body = data.toSeq :+ action
    val result = route(app, postRequestFormUrlEncoded(uri, sessionId)(body: _*)).get

    status(result) must be(BAD_REQUEST)

    maybeExpectedErrorMessagePath.fold() { expectedErrorMessagePath =>
      val stringResult = contentAsString(result)
      stringResult must include(messages(expectedErrorMessagePath))
    }
    verifyTheCacheIsUnchanged()
  }
}

object DeclarationAdditionalActorsControllerSpec {
  val cacheWithMaximumAmountOfActors = DeclarationAdditionalActorsData(
    Seq
      .range[Int](100, 200, 1)
      .map(elem => DeclarationAdditionalActors(Some(elem.toString), Some(elem.toString)))
  )
}
