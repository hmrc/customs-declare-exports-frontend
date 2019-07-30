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

import java.time.LocalDateTime

import base.{CustomExportsBaseSpec, TestHelper, ViewValidator}
import controllers.declaration.TransportInformationContainersControllerSpec.cacheWithMaximumAmountOfHolders
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TransportInformationContainer
import helpers.views.declaration.{CommonMessages, TransportInformationContainerMessages}
import models.declaration.TransportInformationContainerData
import models.declaration.TransportInformationContainerData.id
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import play.api.test.Helpers._
import services.cache.ExportsCacheModel

class TransportInformationContainersControllerSpec
    extends CustomExportsBaseSpec with ViewValidator with TransportInformationContainerMessages with CommonMessages {

  private val uri = uriWithContextPath("/declaration/add-transport-containers")

  private val addActionURLEncoded = (Add.toString, "")
  private val saveAndContinueActionURLEncoded = (SaveAndContinue.toString, "")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
    withCaching[TransportInformationContainerData](None, id)
  }

  override def afterEach(): Unit =
    Mockito.reset(mockExportsCacheService)

  private def removeActionURLEncoded(value: String) = (Remove.toString, value)

  "Transport Information Containers Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
      verifyTheCacheIsUnchanged()
      verify(mockExportsCacheService, times(2)).get(anyString)
    }

    "read item from cache and display it" in {
      withNewCaching(
        aCacheModel(
          withChoice(SupplementaryDec),
          withContainerData(TransportInformationContainer("DeliveredBestGoods"))
        )
      )

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("DeliveredBestGoods")
    }
  }

  "Transport Information Containers Controller on POST" should {

    "add a container successfully" when {

      "with an empty cache" in {

        val body = Seq(("id", "J0ohn"), addActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "that does not exist in cache" in {

        val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("M1l3s")))
        withCaching[TransportInformationContainerData](Some(cachedData), id)

        val body = Seq(("id", "x4rlz"), addActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove an item successfully" when {

      "exists in cache based on id" in {

        withNewCaching(
          aCacheModel(
            withChoice(SupplementaryDec),
            withContainerData(TransportInformationContainer("M1l3s")),
            withContainerData(TransportInformationContainer("J00hn"))
          )
        )

        val body = removeActionURLEncoded("0")
        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "display the form page with an error" when {

      "adding" should {

        "container without any data" in {

          val body = Seq(("id", ""), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, ticEmpty, "#id")

          getElementByCss(page, "#error-message-id-input").text() must be(messages(ticEmpty))
          verifyTheCacheIsUnchanged()
        }

        "container with too long name" in {

          val body = Seq(("id", TestHelper.createRandomAlphanumericString(18)), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, ticErrorLength, "#id")

          getElementByCss(page, "#error-message-id-input").text() must be(messages(ticErrorLength))
          verifyTheCacheIsUnchanged()
        }

        "container with incorrect name" in {

          val body = Seq(("id", "#@$#@$@$@$@$"), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, ticErrorAlphaNumeric, "#id")

          getElementByCss(page, "#error-message-id-input").text() must be(messages(ticErrorAlphaNumeric))
          verifyTheCacheIsUnchanged()
        }

        "duplicated container" in {
          withNewCaching(
            aCacheModel(withChoice(SupplementaryDec), withContainerData(TransportInformationContainer("M1l3s")))
          )

          val body = Seq(("id", "M1l3s"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplication, "#")
          verifyTheCacheIsUnchanged()
        }

        "more than 9999 containers" in {
          withNewCaching(aCacheModel(withChoice(SupplementaryDec), withContainerData(cacheWithMaximumAmountOfHolders)))

          val body = Seq(("id", "M1l3s"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, limit, "#")
          verifyTheCacheIsUnchanged()
        }
      }

      "saving" should {

        "without any data" in {

          val body = Seq(("id", ""), saveAndContinueActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, continueMandatory, "#")
          verifyTheCacheIsUnchanged()
        }

        "duplicated container" in {
          withNewCaching(
            aCacheModel(withChoice(SupplementaryDec), withContainerData(TransportInformationContainer("M1l3s")))
          )

          val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplication, "#")
          verifyTheCacheIsUnchanged()
        }

        "more than 9999 containers" in {
          withNewCaching(aCacheModel(withChoice(SupplementaryDec), withContainerData(cacheWithMaximumAmountOfHolders)))

          val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, limit, "#")
          verifyTheCacheIsUnchanged()
        }
      }

      "try to remove a non existent container" in {
        withCache(TransportInformationContainerData(Seq(TransportInformationContainer("M1l3s"))))

        val body = ("action", "Remove:J0ohn-Coltrane")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        stringResult must include(messages(globalErrorTitle))
        stringResult must include(messages(globalErrorHeading))
        stringResult must include(messages(globalErrorMessage))
        verifyTheCacheIsUnchanged()
      }
    }
  }

  "redirect to the next page" when {

    "user provide container with empty cache" in {

      val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)

      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/summary"))
    }

    "user doesn't fill form but some containers already exist in the cache" in {
      withNewCaching(
        aCacheModel(withChoice(SupplementaryDec), withContainerData(TransportInformationContainer("Jo0hn")))
      )

      val result = route(app, postRequestFormUrlEncoded(uri, saveAndContinueActionURLEncoded)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/summary"))
    }

    "user provide container with some different container in cache" in {
      withCache(TransportInformationContainerData(Seq(TransportInformationContainer("x4rlz"))))

      val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)

      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/summary"))
    }
  }

  private def withCache(data: TransportInformationContainerData) =
    withNewCaching(aCacheModel(withChoice(SupplementaryDec), withContainerData(data)))
}

object TransportInformationContainersControllerSpec {
  val cacheWithMaximumAmountOfHolders = TransportInformationContainerData(
    Seq
      .range[Int](1, 10000, 1)
      .map(elem => TransportInformationContainer(elem.toString))
  )
}
