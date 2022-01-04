/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import base.UnitWithMocksSpec
import com.typesafe.config.ConfigFactory
import play.api.Configuration

class GoogleFormFeedbackLinkConfigSpec extends UnitWithMocksSpec {

  private val googleFormFeedbackLink = "googleFormFeedbackLink"
  private val configWithGoogleFormFeedbackLink: Configuration =
    Configuration(ConfigFactory.parseString(s"urls.googleFormFeedbackLink=$googleFormFeedbackLink"))
  private val emptyConfig: Configuration = Configuration(ConfigFactory.parseString(""))

  private def googleFormFeedbackLinkConfig(configuration: Configuration) = new GoogleFormFeedbackLinkConfig(configuration)

  "GoogleFormFeedbackLinkConfig on googleFormFeedbackLink" when {

    "the link is present in configuration" should {

      "return the link" in {

        googleFormFeedbackLinkConfig(configWithGoogleFormFeedbackLink).googleFormFeedbackLink mustBe Some(googleFormFeedbackLink)
      }
    }

    "the link is not present in configuration" should {

      "return empty Option" in {

        googleFormFeedbackLinkConfig(emptyConfig).googleFormFeedbackLink mustBe None
      }
    }
  }
}
