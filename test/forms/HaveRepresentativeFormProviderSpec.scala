/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import connectors.FakeDataCacheConnector
import forms.behaviours.OptionFieldBehaviours
import models.HaveRepresentative
import org.scalatest.concurrent.ScalaFutures
import play.api.data.FormError
import play.api.libs.json.JsString
import uk.gov.hmrc.http.cache.client.CacheMap

class HaveRepresentativeFormProviderSpec extends OptionFieldBehaviours with ScalaFutures {

  val formProvider = new HaveRepresentativeFormProvider(FakeDataCacheConnector)
  val form = formProvider()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "haveRepresentative.error.required"

    behave like optionsField[HaveRepresentative](
      form,
      fieldName,
      validValues  = HaveRepresentative.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "clear representative cache method" should {
    "return cache without enter EORI and representatives address" in {
      val data = Map(
        "testId" -> JsString("Test value"),
        "enterEORI" -> JsString("EORI"),
        "representativesAddress" -> JsString("Address")
      )
      val cacheMap = CacheMap("id", data)

      whenReady(formProvider.clearRepresentativeCache(cacheMap)){ result =>
        result.getEntry[String]("testId").isDefined shouldBe true
        result.getEntry[String]("enterEORI").isDefined shouldBe false
        result.getEntry[String]("representativesAddress").isDefined shouldBe false
      }
    }
  }
}
