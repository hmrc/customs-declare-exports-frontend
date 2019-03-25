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
import base.CustomExportsBaseSpec
import base.TestHelper.createRandomString
import controllers.declaration.TransportInformationContainersPageControllerSpec.cacheWithMaximumAmountOfHolders
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.TransportInformationContainer
import models.declaration.TransportInformationContainerData
import models.declaration.TransportInformationContainerData.id
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class TransportInformationContainersPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/add-transport-containers")

  private val addActionURLEncoded = (Add.toString, "")
  private val saveAndContinueActionURLEncoded = (SaveAndContinue.toString, "")
  private def removeActionURLEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[TransportInformationContainerData](None, id)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Transport Information Containers Page Controller on GET" should {

    "display the whole content and return a 200 HTTP code" in {
      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      status(result) must be(OK)

      resultAsString must include(messages("supplementary.transportInfo.containers.title"))
      resultAsString must include(messages("supplementary.transportInfo.containerId"))
    }

    "display 'Save and continue' button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "display 'Back' button that links to 'Office-of-exit' page" in {
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("/declaration/transport-information")
    }

    "display additional information form with added items" in {

      val cachedData = TransportInformationContainerData(
        Seq(TransportInformationContainer("M1l3s"), TransportInformationContainer("X4rlz"))
      )
      withCaching[TransportInformationContainerData](Some(cachedData), id)

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      status(result) must be(OK)

      resultAsString must include(messages("supplementary.transportInfo.containerId"))
      resultAsString must include(messages("supplementary.transportInfo.containers.title"))
      resultAsString must include(messages("supplementary.transportInfo.containerId.title"))
    }
  }

  "Additional Information Controller on POST" should {

    "add a container successfully" when {

      "with an empty cache" in {
        withCaching[TransportInformationContainerData](None, id)
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
        withCaching[TransportInformationContainerData](Some(cachedData), id)

        val body = removeActionURLEncoded("0")
        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "display the form page with an error" when {

      "try to add a container without any data" in {

        withCaching[TransportInformationContainerData](None, id)
        val body = Seq(("id", ""), addActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("supplementary.transportInfo.containerId.empty"))
      }

      "try to save and continue without any data" in {

        val body = Seq(("id", ""), saveAndContinueActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("supplementary.continue.mandatory"))
      }

      "try to add duplicated container" in {

        val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("M1l3s")))
        withCaching[TransportInformationContainerData](Some(cachedData), id)

        val body = Seq(("id", "M1l3s"), addActionURLEncoded)
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.duplication"))
      }

      "try to add more than 9999 containers" in {
        withCaching[TransportInformationContainerData](Some(cacheWithMaximumAmountOfHolders), id)

        val body = Seq(("id", "M1l3s"), addActionURLEncoded)
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.limit"))
      }

      "try to save more than 9999 containers" in {
        withCaching[TransportInformationContainerData](Some(cacheWithMaximumAmountOfHolders), id)

        val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.limit"))
      }

      "try to remove a non existent container" in {
        val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("M1l3s")))
        withCaching[TransportInformationContainerData](Some(cachedData), id)

        val body = ("action", "Remove:J0ohn-Coltrane")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.title"))
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }
    }
  }
  "redirect to the next page" when {

    "user provide container with empty cache" in {

      val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)

      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/total-numbers-of-items")
      )
    }

    "user doesn't fill form but some containers already exist in the cache" in {
      val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("Jo0hn")))
      withCaching[TransportInformationContainerData](Some(cachedData), id)

      val result = route(app, postRequestFormUrlEncoded(uri, saveAndContinueActionURLEncoded)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/total-numbers-of-items")
      )
    }

    "user provide container with some different container in cache" in {
      val cachedData = TransportInformationContainerData(Seq(TransportInformationContainer("x4rlz")))
      withCaching[TransportInformationContainerData](Some(cachedData), id)

      val body = Seq(("id", "M1l3s"), saveAndContinueActionURLEncoded)

      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/total-numbers-of-items")
      )
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
