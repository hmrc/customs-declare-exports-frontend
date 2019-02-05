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
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class SupervisingCustomsOfficeSpec extends WordSpec with MustMatchers {
  import SupervisingCustomsOfficeSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val supervisingCustomsOffice = correctSupervisingCustomsOffice
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.supervisingOffice.id" -> supervisingCustomsOffice.office.get
      )

      supervisingCustomsOffice.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object SupervisingCustomsOfficeSpec {
  val correctSupervisingCustomsOffice = SupervisingCustomsOffice(office = Some("12345678"))
  val emptySupervisingCustomsOffice = SupervisingCustomsOffice(office = None)
  val incorrectSupervisingCustomsOffice = SupervisingCustomsOffice(office = Some("123456789"))

  val correctSupervisingCustomsOfficeJSON: JsValue = JsObject(Map("supervisingCustomsOffice" -> JsString("12345678")))
  val emptySupervisingCustomsOfficeJSON: JsValue = JsObject(Map("supervisingCustomsOffice" -> JsString("")))
  val incorrectSupervisingCustomsOfficeJSON: JsValue = JsObject(Map("supervisingCustomsOffice" -> JsString("123456789")))
}
