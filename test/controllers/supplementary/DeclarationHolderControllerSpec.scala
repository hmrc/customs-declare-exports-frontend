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
import forms.supplementary.{DeclarationHolder, DeclarationHoldersData}
import forms.supplementary.DeclarationHoldersData.formId
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class DeclarationHolderControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  import DeclarationHolderControllerSpec._

  val uri = uriWithContextPath("/declaration/supplementary/holder-of-authorisation")

  before {
    authorizedUser()
  }

  "Declaration holder controller on display page" should {
    "display form without holders" in {
      withCaching[DeclarationHoldersData](None, formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.declarationHolder.title"))
      stringResult must include(messages("supplementary.declarationHolder.authorisationCode"))
      stringResult must include(messages("supplementary.declarationHolder.authorisationCode.hint"))
      stringResult must include(messages("supplementary.eori"))
      stringResult must include(messages("supplementary.eori.hint"))
    }

    "display table with added holders" in {
      val cachedData = DeclarationHoldersData(
        Seq(DeclarationHolder(Some("1234"), Some("eori1")), DeclarationHolder(Some("5678"), Some("eori2")))
      )
      withCaching[DeclarationHoldersData](Some(cachedData), formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result)
      stringResult must include("1234-eori1")
      stringResult must include("5678-eori2")
      stringResult must include(messages("supplementary.declarationHolder.title"))
      stringResult must include(messages("supplementary.declarationHolder.authorisationCode"))
      stringResult must include(messages("supplementary.declarationHolder.authorisationCode.hint"))
      stringResult must include(messages("supplementary.eori"))
      stringResult must include(messages("supplementary.eori.hint"))
    }

    "display back button that links to additional actors page" in {
      withCaching[DeclarationHoldersData](None, formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/additional-actors"))
    }
  }

  "Declaration holder controller in submit holder" should {
    "display the form page with error" when {
      "try to add data without holder" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = ("action", "Add")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("supplementary.declarationHolder.authorisationCode.empty"))
        stringResult must include(messages("supplementary.eori.empty"))
      }

      "try to save data without holder" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = ("action", "Save and continue")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("supplementary.declarationHolder.authorisationCode.empty"))
        stringResult must include(messages("supplementary.eori.empty"))
      }

      "try to add holder without eori number" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.eori.empty"))
      }

      "try to add holder without authorisation code" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("eori", "eori1"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolder.authorisationCode.empty"))
      }

      "try to save holder without eori number" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.eori.empty"))
      }

      "try to save holder without authorisation code" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("eori", "eori1"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolder.authorisationCode.empty"))
      }

      "try to add longer authorisation code" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "12345"), ("eori", "eori1"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolder.authorisationCode.error"))
      }

      "try to add longer eori" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("eori", createRandomString(18)), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.eori.error"))
      }

      "try to save longer authorisation code" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "12345"), ("eori", "eori1"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolder.authorisationCode.error"))
      }

      "try to save longer eori" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("eori", createRandomString(18)), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.eori.error"))
      }

      "try to add authorisation code with special characters" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1$#4"), ("eori", "eori1"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolder.authorisationCode.error"))
      }

      "try to add eori with special characters" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "e@#$1"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.eori.error"))
      }

      "try to save authorisation code with special characters" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1$#4"), ("eori", "eori1"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolder.authorisationCode.error"))
      }

      "try to save eori with special characters" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "e@#$1"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.eori.error"))
      }

      "try to add more than 99 holders" in {
        withCaching[DeclarationHoldersData](Some(cacheWithMaximumAmountOfHolders), formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolders.maximumAmount.error"))
      }

      "try to save more than 99 holders" in {
        withCaching[DeclarationHoldersData](Some(cacheWithMaximumAmountOfHolders), formId)
        val body = Seq(("authorisationTypeCode", "9999"), ("eori", "eori9"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolders.maximumAmount.error"))
      }

      "try to add duplicated holder" in {
        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolders.duplicated"))
      }

      "try to save duplicated holder" in {
        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.declarationHolders.duplicated"))
      }

      "try to remove not added additional code" in {
        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)
        val body = ("action", "Remove:4321-eori")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.title"))
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }
    }

    "add holder without error" when {
      "user provide holder with empty cache" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "user provide holder that not exists in cache" in {
        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("4321"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), ("action", "Add"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove holder" when {
      "holder exists in cache" in {
        val cachedData = DeclarationHoldersData(
          Seq(DeclarationHolder(Some("4321"), Some("eori")), DeclarationHolder(Some("4321"), Some("eori")))
        )
        withCaching[DeclarationHoldersData](Some(cachedData), formId)
        val body = ("action", "Remove:4321-eori")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to the next page" when {
      "user provide holder with empty cache" in {
        withCaching[DeclarationHoldersData](None, formId)
        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/destination-countries")
        )
      }

      "user doesn't fill form but some holder exists inside the cache" in {
        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)
        val body = ("action", "Save and continue")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/destination-countries")
        )
      }

      "user provide holder with some different holder in cache" in {
        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)
        val body = Seq(("authorisationTypeCode", "4321"), ("eori", "eori1"), ("action", "Save and continue"))

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/destination-countries")
        )
      }
    }
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
