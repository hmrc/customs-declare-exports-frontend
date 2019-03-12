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
import base.TestHelper.createRandomString
import controllers.supplementary.AdditionalInformationControllerSpec.cacheWithMaximumAmountOfHolders
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.supplementary.AdditionalInformation
import models.declaration.supplementary.AdditionalInformationData
import models.declaration.supplementary.AdditionalInformationData.formId
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class AdditionalInformationControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri: String = uriWithContextPath("/declaration/supplementary/additional-information")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
  }

  "Additional Information Controller when getting the page" should {
    "display additional information form with no items" in {
      withCaching[AdditionalInformationData](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.additionalInformation.title"))
      stringResult must include(messages("supplementary.additionalInformation.code"))
      stringResult must include(messages("supplementary.additionalInformation.description"))
    }

    "display additional information form with added items" in {
      val cachedData = AdditionalInformationData(
        Seq(AdditionalInformation("M1l3s", "Davis"), AdditionalInformation("X4rlz", "Mingus"))
      )
      withCaching[AdditionalInformationData](Some(cachedData), formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("M1l3s-Davis")
      stringResult must include("X4rlz-Mingus")
      stringResult must include(messages("supplementary.additionalInformation.title"))
      stringResult must include(messages("supplementary.additionalInformation.code"))
      stringResult must include(messages("supplementary.additionalInformation.description"))
    }

    "display back button that links to package information page" in {
      withCaching[AdditionalInformationData](None, formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/package-information"))
    }
  }

  "Additional Information controller handling a post" should {
    "add an item sucessfully" when {
      "with an empty cache" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", "J0ohn"), ("description", "Coltrane"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "that does not exist in cache" in {
        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis")))
        withCaching[AdditionalInformationData](Some(cachedData), formId)
        val body = Seq(("code", "x4rlz"), ("description", "Mingusss"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove an item successfully" when {
      "exists in cache based on id" in {
        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis"), AdditionalInformation("J00hn", "Coltrane"))
        )
        withCaching[AdditionalInformationData](Some(cachedData), formId)
        val body = removeActionUrlEncoded("0")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "display the form page with an error" when {
      "try to add an item without any data" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", ""), ("description", ""), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("supplementary.additionalInformation.code.empty"))
        stringResult must include(messages("supplementary.additionalInformation.description.empty"))
      }

      "try to save and continue without any items" in {
        withCaching[AdditionalInformationData](None, formId)

        val body = Seq(("code", ""), ("description", ""), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("supplementary.continue.mandatory"))
      }

      "try to add an item without code" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", ""), ("description", "Davis"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.additionalInformation.code.empty"))
      }

      "try to add an item without a description" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", "M1l3s"), ("description", ""), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.additionalInformation.description.empty"))
      }

      "try to save and continue without providing a code" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", ""), ("description", "Davis"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.additionalInformation.code.empty"))
      }

      "try to save and continue without a description" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", "123rt"), ("description", ""), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.additionalInformation.description.empty"))
      }

      "try to add a longer code" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", createRandomString(6)), ("description", ""), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.additionalInformation.code.error"))
      }

      "try to add a shorter code" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", createRandomString(3)), ("description", ""), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.additionalInformation.code.error"))
      }

      "try to add a longer description" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", "M1l3s"), ("description", createRandomString(100)), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.additionalInformation.description.error"))
      }

    "try to add duplicated item" in {
      val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis")))
      withCaching[AdditionalInformationData](Some(cachedData), formId)
      val body = Seq(("code", "M1l3s"), ("description", "Davis"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.duplication"))
      }

      "try to add more than 99 items" in {
        withCaching[AdditionalInformationData](Some(cacheWithMaximumAmountOfHolders), formId)
        val body = Seq(("code", "M1l3s"), ("description", "Davis"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.limit"))
      }

      "try to save more than 99 items" in {
        withCaching[AdditionalInformationData](Some(cacheWithMaximumAmountOfHolders), formId)
        val body = Seq(("code", "M1l3s"), ("description", "Davis"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.limit"))
      }

    "try to remove a non existent code" in {
      val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis")))
      withCaching[AdditionalInformationData](Some(cachedData), formId)
      val body = ("action", "Remove:J0ohn-Coltrane")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.title"))
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }
    }
    "redirect to the next page" when {
      "user provide item with empty cache" in {
        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", "M1l3s"), ("description", "Davis"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/add-document"))
      }

      "user doesn't fill form but some items already exist in the cache" in {
        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("Jo0hn", "Coltrane")))
        withCaching[AdditionalInformationData](Some(cachedData), formId)

        val result = route(app, postRequestFormUrlEncoded(uri, saveAndContinueActionUrlEncoded)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/add-document"))
      }

      "user provide holder with some different holder in cache" in {
        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("x4rlz", "Mingus")))
        withCaching[AdditionalInformationData](Some(cachedData), formId)
        val body = Seq(("code", "M1l3s"), ("description", "Davis"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/add-document"))
      }
    }
  }

}

object AdditionalInformationControllerSpec {
  val cacheWithMaximumAmountOfHolders = AdditionalInformationData(
    Seq
      .range[Int](100, 200, 1)
      .map(elem => AdditionalInformation(elem.toString, elem.toString))
  )
}
