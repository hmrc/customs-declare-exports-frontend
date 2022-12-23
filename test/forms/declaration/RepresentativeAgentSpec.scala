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

package forms.declaration

import forms.common.DeclarationPageBaseSpec
import forms.common.YesNoAnswer.YesNoAnswers.yes
import play.api.libs.json.{JsObject, JsString, JsValue}

class RepresentativeAgentSpec extends DeclarationPageBaseSpec {

  import RepresentativeAgentSpec._

  "RepresentativeAgent mapping used for binding data" should {

    "return form with errors" when {

      "provided with unrecognized response" in {
        val form = RepresentativeAgent.form.bind(representativeAgentJSON("invalid"), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.representative.agent.error")
      }

      "provided with missing response" in {
        val form = RepresentativeAgent.form.bind(representativeAgentJSON(""), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.representative.agent.error")
      }
    }

    "return form without errors" when {
      "provided with valid response" in {
        val form = RepresentativeAgent.form.bind(representativeAgentJSON(yes), JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }

  "RepresentativeAgent" when {
    testTariffContentKeys(RepresentativeAgent, "tariff.declaration.areYouCompletingThisDeclarationOnBehalfOfAnotherAgent")
  }
}

object RepresentativeAgentSpec {
  val correctRepresentativeAgent: RepresentativeAgent = RepresentativeAgent(representingAgent = yes)

  def representativeAgentJSON(value: String): JsValue = JsObject(Map("representingAgent" -> JsString(value)))
}
