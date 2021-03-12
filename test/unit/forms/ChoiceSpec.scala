/*
 * Copyright 2021 HM Revenue & Customs
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

package unit.forms

import config.SfusConfig
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import unit.base.UnitSpec
import base.ExportsTestData._

class ChoiceSpec extends UnitSpec with BeforeAndAfterEach {

  val sfusConfig = mock[SfusConfig]

  override def beforeEach() = {
    when(sfusConfig.isSfusUploadEnabled).thenReturn(true)
    when(sfusConfig.isSfusSecureMessagingEnabled).thenReturn(true)
  }

  override def afterEach() =
    reset(sfusConfig)

  "Choice" when {
    "calling filterAvailableJourneys" should {
      "leave the availableJourneys seq unmodified" when {
        "both sfus and sfusSecureMessaging flags are enabled" in {
          setSfusConfigReply()
          Choice.filterAvailableJourneys(allJourneys, sfusConfig) mustBe allJourneys
        }

        "neither sfus or sfusSecureMessaging journey types are enabled by the 'list-of-available-journeys' values" in {
          setSfusConfigReply(isSfusUploadEnabled = false, isSfusSecureMessagingEnabled = false)

          val limitedJourneys = Seq(CreateDec, ContinueDec)
          Choice.filterAvailableJourneys(limitedJourneys, sfusConfig) mustBe limitedJourneys
        }
      }

      "remove the sfus availableJourney when the corresponding flag is disabled" in {
        setSfusConfigReply(isSfusUploadEnabled = false)

        Choice.filterAvailableJourneys(allJourneys, sfusConfig) mustBe Seq(CreateDec, ContinueDec, CancelDec, Submissions, ViewMessages)
      }

      "remove the sfusSecureMessaging availableJourney when the corresponding flag is disabled" in {
        setSfusConfigReply(isSfusSecureMessagingEnabled = false)

        Choice.filterAvailableJourneys(allJourneys, sfusConfig) mustBe Seq(CreateDec, ContinueDec, CancelDec, Submissions, UploadDocuments)
      }

      "remove the both the sfus and sfusSecureMessaging availableJourneys when the corresponding flags are disabled" in {
        setSfusConfigReply(isSfusUploadEnabled = false, isSfusSecureMessagingEnabled = false)

        Choice.filterAvailableJourneys(allJourneys, sfusConfig) mustBe Seq(CreateDec, ContinueDec, CancelDec, Submissions)
      }
    }
  }

  private def setSfusConfigReply(isSfusUploadEnabled: Boolean = true, isSfusSecureMessagingEnabled: Boolean = true) = {
    when(sfusConfig.isSfusUploadEnabled).thenReturn(isSfusUploadEnabled)
    when(sfusConfig.isSfusSecureMessagingEnabled).thenReturn(isSfusSecureMessagingEnabled)
  }
}
