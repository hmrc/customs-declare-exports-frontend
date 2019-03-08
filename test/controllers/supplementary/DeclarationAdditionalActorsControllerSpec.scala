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
import controllers.util.{Add, SaveAndContinue}
import forms.supplementary.DeclarationAdditionalActors
import forms.supplementary.DeclarationAdditionalActors.PartyType
import forms.supplementary.DeclarationAdditionalActorsSpec._
import models.declaration.supplementary.DeclarationAdditionalActorsData
import models.declaration.supplementary.DeclarationAdditionalActorsData.formId
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import play.api.test.Helpers._

class DeclarationAdditionalActorsControllerSpec extends CustomExportsBaseSpec with BeforeAndAfterEach {

  val uri = uriWithContextPath("/declaration/supplementary/additional-actors")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  override def beforeEach() {
    authorizedUser()
  }

  "Declaration additional actors controller" should {
    "display declaration additional actors form with no actors" in {
      withCaching[DeclarationAdditionalActors](None)
      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.additionalActors.title"))
      stringResult must include(messages("supplementary.additionalActors.eori"))
      stringResult must include(messages("supplementary.eori.hint"))
      stringResult must include(messages("supplementary.partyType"))
      stringResult must include(messages("supplementary.partyType.CS"))
      stringResult must include(messages("supplementary.partyType.MF"))
      stringResult must include(messages("supplementary.partyType.FW"))
      stringResult must include(messages("supplementary.partyType.WH"))
    }

    "display additional information form with added items" in {
      withCaching[DeclarationAdditionalActorsData](Some(correctAdditionalActorsData), formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("eori1")
      stringResult must include(PartyType.Consolidator)
      stringResult must include(messages("supplementary.additionalActors.title"))
      stringResult must include(messages("supplementary.additionalActors.eori"))
      stringResult must include(messages("supplementary.additionalActors.partyType"))
    }

    "validate form - incorrect values" in {
      withCaching[DeclarationAdditionalActorsData](None)
      val result = route(app, postRequest(uri, incorrectAdditionalActorsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.eori.error"))
      stringResult must include(messages("supplementary.partyType.error"))
    }

    "validate form - optional data allowed" in {
      withCaching[DeclarationAdditionalActorsData](None)

      val undefinedDocument: Map[String, String] = Map(
        "eori" -> "",
        "partyType" -> "")

      val body = undefinedDocument.toSeq :+ addActionUrlEncoded
      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

      status(result) must be(BAD_REQUEST)

      val stringResult = contentAsString(result)
      stringResult must include(messages("supplementary.additionalActors.eori.isNotDefined"))
    }

    "validate form - correct values" in {
      withCaching[DeclarationAdditionalActorsData](None)

      val body = correctAdditionalActorsMap.toSeq :+ addActionUrlEncoded
      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/holder-of-authorisation")
      )
    }

    "display back button that links to package information page" in {
      withCaching[DeclarationAdditionalActorsData](None, formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/representative-details"))
    }
  }

  "Additional Information controller handling a post" should {
    "add an item successfully" when {
      "with an empty cache" in {
        withCaching[DeclarationAdditionalActorsData](None, formId)

        val body = correctAdditionalActorsMap.toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
        val stringResult = contentAsString(result)
        stringResult must include("eori1")
      }
    }
  }
}
