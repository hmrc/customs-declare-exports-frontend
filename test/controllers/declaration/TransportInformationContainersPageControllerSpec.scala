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
import controllers.declaration.TransportInformationContainersPageControllerSpec.cacheWithMaximumAmountOfHolders
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.TransportInformationContainer
import helpers.views.declaration.{CommonMessages, TransportInformationContainerMessages}
import models.declaration.TransportInformationContainerData
import models.declaration.TransportInformationContainerData.id
import play.api.test.Helpers._

class TransportInformationContainersPageControllerSpec
    extends CustomExportsBaseSpec with ViewValidator with TransportInformationContainerMessages with CommonMessages {

  private val uri = uriWithContextPath("/declaration/add-transport-containers")

  private val addActionURLEncoded = (Add.toString, "")
  private val saveAndContinueActionURLEncoded = (SaveAndContinue.toString, "")
  private def removeActionURLEncoded(value: String) = (Remove.toString, value)

  override def beforeEach() {
    authorizedUser()
    withNewCaching(createModelWithNoItems())
    withCaching[TransportInformationContainerData](None, id)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Transport Information Containers Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("DeliveredBestGoods")))
      withNewCaching(createModel().copy(containerData = Some(cachedData)))

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

        val cachedData = TransportInformationContainerData(
          Seq(TransportInformationContainer("M1l3s"), TransportInformationContainer("J00hn"))
        )
        withNewCaching(createModel().copy(containerData = Some(cachedData)))

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
        }

        "container with too long name" in {

          val body = Seq(("id", TestHelper.createRandomAlphanumericString(18)), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, ticErrorLength, "#id")

          getElementByCss(page, "#error-message-id-input").text() must be(messages(ticErrorLength))
        }

        "container with incorrect name" in {

          val body = Seq(("id", "#@$#@$@$@$@$"), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, ticErrorAlphaNumeric, "#id")

          getElementByCss(page, "#error-message-id-input").text() must be(messages(ticErrorAlphaNumeric))
        }

        "duplicated container" in {

          val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("M1l3s")))
          withNewCaching(createModel().copy(containerData = Some(cachedData)))

          val body = Seq(("id", "M1l3s"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplication, "#")
        }

        "more than 9999 containers" in {
          withNewCaching(createModel().copy(containerData = Some(cacheWithMaximumAmountOfHolders)))

          val body = Seq(("id", "M1l3s"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, limit, "#")
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
        }

        "duplicated container" in {

          val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("M1l3s")))
          withNewCaching(createModel().copy(containerData = Some(cachedData)))

          val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplication, "#")
        }

        "more than 9999 containers" in {
          withNewCaching(createModel().copy(containerData = Some(cacheWithMaximumAmountOfHolders)))

          val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, limit, "#")
        }
      }

      "try to remove a non existent container" in {

        val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("M1l3s")))
        withCaching[TransportInformationContainerData](Some(cachedData), id)

        val body = ("action", "Remove:J0ohn-Coltrane")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        stringResult must include(messages(globalErrorTitle))
        stringResult must include(messages(globalErrorHeading))
        stringResult must include(messages(globalErrorMessage))
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
      val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("Jo0hn")))
      withNewCaching(createModel().copy(containerData = Some(cachedData)))

      val result = route(app, postRequestFormUrlEncoded(uri, saveAndContinueActionURLEncoded)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/summary"))
    }

    "user provide container with some different container in cache" in {
      val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("x4rlz")))
      withCaching[TransportInformationContainerData](Some(cachedData), id)

      val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)

      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/summary"))
    }
  }
}

object TransportInformationContainersPageControllerSpec {
  val cacheWithMaximumAmountOfHolders = TransportInformationContainerData(
    Seq
      .range[Int](1, 10000, 1)
      .map(elem => TransportInformationContainer(elem.toString))
  )
}
