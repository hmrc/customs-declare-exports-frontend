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

package forms.declaration

import forms.declaration.officeOfExit.OfficeOfExitStandard
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class OfficeOfExitStandardSpec extends WordSpec with MustMatchers {
  import OfficeOfExitStandardSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val officeOfExit = correctOfficeOfExit
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.exitOffice.id" -> officeOfExit.officeId,
        "declaration.presentationOffice.id" -> officeOfExit.presentationOfficeId,
        "declaration.specificCircumstancesCode" -> "A20"
      )

      officeOfExit.toMetadataProperties() mustEqual expectedMetadataProperties
    }
  }
}

object OfficeOfExitStandardSpec {
  val correctOfficeOfExit = OfficeOfExitStandard(
    officeId = "123qwe12",
    presentationOfficeId = "123",
    circumstancesCode = OfficeOfExitStandard.AllowedCircumstancesCodeAnswers.yes
  )
  val emptyOfficeOfExit = OfficeOfExitStandard(officeId = "", presentationOfficeId = "", circumstancesCode = "Yes")
  val incorrectOfficeOfExit =
    OfficeOfExitStandard(officeId = "office", presentationOfficeId = "123!!!", circumstancesCode = "Yes")

  val correctOfficeOfExitJSON: JsValue = JsObject(
    Map(
      "officeId" -> JsString("123qwe12"),
      "presentationOfficeId" -> JsString("123"),
      "circumstancesCode" -> JsString("Yes")
    )
  )
  val emptyOfficeOfExitJSON: JsValue = JsObject(
    Map("officeId" -> JsString(""), "presentationOfficeId" -> JsString(""), "circumstancesCode" -> JsString(""))
  )
  val incorrectOfficeOfExitJSON: JsValue = JsObject(
    Map(
      "officeId" -> JsString("office"),
      "presentationOfficeId" -> JsString("123!!!"),
      "circumstancesCode" -> JsString("Yes")
    )
  )
}
