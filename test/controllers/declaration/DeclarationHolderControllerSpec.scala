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

import base.TestHelper.createRandomAlphanumericString
import base.{CustomExportsBaseSpec, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.DeclarationHolder
import helpers.views.declaration.{CommonMessages, DeclarationHolderMessages}
import models.declaration.DeclarationHoldersData
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{reset, times, verify}
import play.api.test.Helpers._

class DeclarationHolderControllerSpec
    extends CustomExportsBaseSpec with DeclarationHolderMessages with CommonMessages with ViewValidator {
  import DeclarationHolderControllerSpec._

  private val uri = uriWithContextPath("/declaration/holder-of-authorisation")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  val exampleModel = aDeclaration(withChoice(SupplementaryDec))

  override def beforeEach() {
    authorizedUser()
    withNewCaching(exampleModel)
  }

  override def afterEach() {
    reset(mockExportsCacheService)
  }

  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  "Declaration Holder Controller on GET" should {

    "return 200 status code" in {
      val Some(result) = route(app, getRequest(uri, sessionId = exampleModel.sessionId))

      status(result) must be(OK)
      verifyTheCacheIsUnchanged()
    }

    "read item from cache and display it" in {

      val model = aDeclaration(withChoice("SMP"), withDeclarationHolders(Some("8899"), Some("0099887766")))
      withNewCaching(model)

      val Some(result) = route(app, getRequest(uri, sessionId = model.sessionId))
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("8899")
      page must include("0099887766")
    }
  }

  "Declaration Holder Controller on POST" should {

    "validate request and show error" when {

      "adding holder" which {

        "has no EORI number" in {

          val body = Seq(("authorisationTypeCode", "ACE"), addActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "eori-error", eoriEmpty, "#eori")

          getElementById(page, "error-message-eori-input").text() must be(messages(eoriEmpty))
          verifyTheCacheIsUnchanged()
        }

        "has longer EORI" in {
          val body = Seq(("eori", createRandomAlphanumericString(18)), addActionUrlEncoded)

          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
          verifyTheCacheIsUnchanged()
        }

        "has EORI with special characters" in {

          val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "e@#$1"), addActionUrlEncoded)

          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
          verifyTheCacheIsUnchanged()
        }

        "has no Authorisation code" in {

          val body = Seq(("eori", "eori1"), addActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "authorisationTypeCode-error", authorisationCodeEmpty, "#authorisationTypeCode")

          getElementById(page, "error-message-authorisationTypeCode-input").text() must be(
            messages(authorisationCodeEmpty)
          )
          verifyTheCacheIsUnchanged()
        }

        "has invalid Authorisation code" in {

          val body = Seq(("authorisationTypeCode", "1$#4"), ("eori", "eori1"), addActionUrlEncoded)

          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(authorisationCodeError))
          verifyTheCacheIsUnchanged()
        }

        "has both inputs empty" in {

          val body = addActionUrlEncoded

          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "authorisationTypeCode-error", authorisationCodeEmpty, "#authorisationTypeCode")
          checkErrorLink(page, "eori-error", eoriEmpty, "#eori")

          getElementById(page, "error-message-authorisationTypeCode-input").text() must be(
            messages(authorisationCodeEmpty)
          )
          getElementById(page, "error-message-eori-input").text() must be(messages(eoriEmpty))
          verifyTheCacheIsUnchanged()
        }

        "is duplicated" in {

          val model = aDeclaration(withChoice("SMP"), withDeclarationHolders(Some("ACE"), Some("eori")))

          withNewCaching(model)

          val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "eori"), addActionUrlEncoded)

          val Some(result) = route(app, postRequestFormUrlEncoded(uri, sessionId = model.sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "-error", duplicatedItem, "#")
          verifyTheCacheIsUnchanged()
        }

        "has more than 99 holders" in {

          val model = aDeclaration(withChoice("SMP"), withDeclarationHolders(cacheWithMaximumAmountOfHolders))
          withNewCaching(model)

          val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "eori1"), addActionUrlEncoded)

          val Some(result) = route(app, postRequestFormUrlEncoded(uri, model.sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "-error", maximumAmountReached, "#")
          verifyTheCacheIsUnchanged()
        }
      }

      "saving holder" which {

        "has no EORI number" in {

          val body = Seq(("authorisationTypeCode", "ACE"), saveAndContinueActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "eori-error", eoriEmpty, "#eori")

          getElementById(page, "error-message-eori-input").text() must be(messages(eoriEmpty))
          verifyTheCacheIsUnchanged()
        }

        "has longer EORI" in {

          val body = Seq(("eori", createRandomAlphanumericString(18)), addActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
          verifyTheCacheIsUnchanged()
        }

        "has EORI with special characters" in {

          val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "e@#$1"), saveAndContinueActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
          verifyTheCacheIsUnchanged()
        }

        "has no Authorisation code" in {

          val body = Seq(("eori", "eori1"), saveAndContinueActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "authorisationTypeCode-error", authorisationCodeEmpty, "#authorisationTypeCode")

          getElementById(page, "error-message-authorisationTypeCode-input").text() must be(
            messages(authorisationCodeEmpty)
          )
          verifyTheCacheIsUnchanged()
        }

        "has invalid Authorisation code" in {

          val body = Seq(("authorisationTypeCode", "1$#4"), ("eori", "eori1"), saveAndContinueActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(authorisationCodeError))
          verifyTheCacheIsUnchanged()
        }

        "has duplicated holder" in {
          val model = aDeclaration(withChoice("SMP"), withDeclarationHolders(Some("ACE"), Some("eori")))

          withNewCaching(model)

          val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "eori"), saveAndContinueActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, model.sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "-error", duplicatedItem, "#")
          verifyTheCacheIsUnchanged()
        }

        "has more than 99 holders" in {

          val sessionId = withCache(cacheWithMaximumAmountOfHolders)

          val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "eori9"), saveAndContinueActionUrlEncoded)
          val Some(result) = route(app, postRequestFormUrlEncoded(uri, sessionId)(body: _*))
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, "-error", maximumAmountReached, "#")
          verifyTheCacheIsUnchanged()
        }
      }
    }

    "add holder without error" when {

      "user provide holder with empty cache" in {

        val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "eori1"), addActionUrlEncoded)
        val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

        status(result) must be(SEE_OTHER)
        theCacheModelUpdated.parties.declarationHoldersData must be(
          Some(DeclarationHoldersData(Seq(DeclarationHolder(Some("ACE"), Some("eori1")))))
        )
      }

      "user provide holder that not exists in cache" in {

        val sessionId = withCache(DeclarationHoldersData(Seq(DeclarationHolder(Some("ACP"), Some("eori")))))

        val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "eori1"), addActionUrlEncoded)
        val Some(result) = route(app, postRequestFormUrlEncoded(uri, sessionId)(body: _*))

        status(result) must be(SEE_OTHER)
        theCacheModelUpdated.parties.declarationHoldersData must be(
          Some(
            DeclarationHoldersData(
              Seq(DeclarationHolder(Some("ACP"), Some("eori")), DeclarationHolder(Some("ACE"), Some("eori1")))
            )
          )
        )
      }
    }

    "remove holder" when {

      "holder exists in cache" in {
        val sessionId = withCache(
          DeclarationHoldersData(
            Seq(DeclarationHolder(Some("4321"), Some("eori")), DeclarationHolder(Some("4321"), Some("eori")))
          )
        )

        val body = removeActionUrlEncoded("4321-eori")
        val Some(result) = route(app, postRequestFormUrlEncoded(uri, sessionId)(body))

        status(result) must be(OK)
        theCacheModelUpdated.parties.declarationHoldersData must be(Some(DeclarationHoldersData(List.empty)))
      }
    }

    "redirect to the next page" when {

      "user doesn't provide anything" in {

        val body = Seq(("authorisationTypeCode", ""), ("eori", ""), saveAndContinueActionUrlEncoded)
        val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/destination-countries"))
      }

      "user provide holder with empty cache" in {

        val body = Seq(("authorisationTypeCode", "ACE"), ("eori", "eori1"), saveAndContinueActionUrlEncoded)
        val Some(result) = route(app, postRequestFormUrlEncoded(uri, exampleModel.sessionId)(body: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/destination-countries"))
        theCacheModelUpdated.parties.declarationHoldersData must be(
          Some(DeclarationHoldersData(Seq(DeclarationHolder(Some("ACE"), Some("eori1")))))
        )
      }

      "user doesn't fill form but some holder exists inside the cache" in {
        val sessionId = withCache(DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori")))))

        val body = saveAndContinueActionUrlEncoded
        val Some(result) = route(app, postRequestFormUrlEncoded(uri, sessionId)(body))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/destination-countries"))
        verify(mockExportsCacheService, times(2)).get(anyString())
      }

      "user provide holder with some different holder in cache" in {
        val sessionId = withCache(DeclarationHoldersData(Seq(DeclarationHolder(Some("ACE"), Some("eori")))))

        val body = Seq(("authorisationTypeCode", "ACP"), ("eori", "eori1"), saveAndContinueActionUrlEncoded)
        val Some(result) = route(app, postRequestFormUrlEncoded(uri, sessionId)(body: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/destination-countries"))
        theCacheModelUpdated.parties.declarationHoldersData must be(
          Some(
            DeclarationHoldersData(
              Seq(DeclarationHolder(Some("ACE"), Some("eori")), DeclarationHolder(Some("ACP"), Some("eori1")))
            )
          )
        )
      }
    }
  }
  private def withCache(holders: DeclarationHoldersData): String = {
    val model = aDeclaration(withChoice("SMP"), withDeclarationHolders(holders))
    withNewCaching(model)
    model.sessionId
  }
}

object DeclarationHolderControllerSpec {
  val cacheWithMaximumAmountOfHolders =
    DeclarationHoldersData(
      Seq
        .range[Int](100, 200, 1)
        .map(elem => DeclarationHolder(Some(elem.toString), Some(elem.toString)))
    )
}
