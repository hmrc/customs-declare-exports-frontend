/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section1

import base.ExportsTestData.lrn
import base.UnitSpec
import connectors.CustomsDeclareExportsConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LrnValidatorSpec extends UnitSpec with ScalaFutures {

  private val customsDeclareExportsConnector = mock[CustomsDeclareExportsConnector]
  private val lrnValidator = new LrnValidator(customsDeclareExportsConnector)

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "LrnValidator on hasBeenSubmittedInThePast48Hours" should {

    "return false" when {

      "provided with form containing LRN" that {

        "has not been used" in {
          when(customsDeclareExportsConnector.isLrnAlreadyUsed(any[Lrn])(any(), any()))
            .thenReturn(Future.successful(false))

          val testLrn = Lrn(lrn)

          val result = lrnValidator.hasBeenSubmittedInThePast48Hours(testLrn).futureValue
          result mustBe false
        }

        "has been used more than 48 hours in the past" in {
          when(customsDeclareExportsConnector.isLrnAlreadyUsed(any[Lrn])(any(), any()))
            .thenReturn(Future.successful(false))

          val testLrn = Lrn(lrn)

          val result = lrnValidator.hasBeenSubmittedInThePast48Hours(testLrn).futureValue
          result mustBe false
        }
      }

      "return true" when {
        "provided with form containing LRN that has been used in the past 48 hours" in {
          when(customsDeclareExportsConnector.isLrnAlreadyUsed(any[Lrn])(any(), any()))
            .thenReturn(Future.successful(true))

          val testLrn = Lrn(lrn)

          val result = lrnValidator.hasBeenSubmittedInThePast48Hours(testLrn).futureValue
          result mustBe true
        }
      }
    }
  }
}
