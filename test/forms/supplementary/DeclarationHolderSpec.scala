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

package forms.supplementary
import base.TestHelper
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

class DeclarationHolderSpec extends WordSpec with MustMatchers {
  import DeclarationHolderSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val declarationHolders = correctDeclarationHolder
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.authorisationHolders[0].categoryCode" -> declarationHolders.holders.head.authorisationTypeCode.get,
        "declaration.authorisationHolders[0].id" -> declarationHolders.holders.head.eori.get
      )

      declarationHolders.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

  "Declaration object" should {
    "contains correct limit value" in {
      DeclarationHoldersData.limitOfHolders must be(99)
    }
  }
}

object DeclarationHolderSpec {
  private val eoriMaxLength = 17

  val correctDeclarationHolder = DeclarationHoldersData(
    Seq(DeclarationHolder(authorisationTypeCode = Some("1234"), eori = Some("PL213472539481923")))
  )
  val emptyDeclarationHolder = DeclarationHolder(authorisationTypeCode = None, eori = None)
  val incorrectDeclarationHolder =
    DeclarationHolder(
      authorisationTypeCode = Some("12345"),
      eori = Some(TestHelper.createRandomString(eoriMaxLength + 1))
    )

  val correctDeclarationHolderJSON: JsValue = JsObject(
    Map("authorisationTypeCode" -> JsString("1234"), "eori" -> JsString("PL213472539481923"))
  )
  val emptyDeclarationHolderJSON: JsValue = JsObject(
    Map("authorisationTypeCode" -> JsString(""), "eori" -> JsString(""))
  )
  val incorrectDeclarationHolderJSON: JsValue = JsObject(
    Map(
      "authorisationTypeCode" -> JsString("12345"),
      "eori" -> JsString(TestHelper.createRandomString(eoriMaxLength + 1))
    )
  )

  val correctDeclarationHoldersDataJSON: JsValue = JsObject(
    Map("holders" -> JsArray(Seq(correctDeclarationHolderJSON)))
  )
}
