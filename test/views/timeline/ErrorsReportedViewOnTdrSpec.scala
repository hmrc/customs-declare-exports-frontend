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

package views.timeline

import base.{MessageSpec, UnitWithMocksSpec}
import com.typesafe.config.ConfigFactory
import connectors.CodeListConnector
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import tools.Stubs.{minimalConfig, updateConfig}
import views.common.ViewMatchers
import views.html.timeline.errors_reported

class ErrorsReportedViewOnTdrSpec extends UnitWithMocksSpec with MessageSpec with ViewMatchers {

  val configuration: Configuration = {
    val config = updateConfig(minimalConfig, Map("features.tdrVersion" -> "true"))
    Configuration(ConfigFactory.parseString(config))
  }

  private val injector = GuiceApplicationBuilder().configure(configuration).injector()

  private val codeListConnector = mock[CodeListConnector]
  private val declaration = aDeclaration(withConsignmentReferences("DUCR", "lrn"))

  "Errors Reported page" when {
    "environment is TDR" should {
      "have the expected body content" in {
        val page = injector.instanceOf[errors_reported]
        val view = page(None, declaration, MRN.value, None, Seq.empty)(request, messages, codeListConnector)
        val body = view.getElementsByClass("govuk-body")

        body.get(4).text mustBe messages("rejected.notification.tdr.guidance.section.3.paragraph.1")

        val email = messages("rejected.notification.tdr.guidance.section.3.paragraph.2.email")
        body.get(5).text mustBe messages("rejected.notification.tdr.guidance.section.3.paragraph.2", email)
        val emailLink = body.get(5).getElementsByClass("govuk-link").get(0)
        emailLink.getElementsByAttributeValue("href", s"mailto:$email")
      }
    }
  }
}
