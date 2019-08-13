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

package unit.forms.declaration

import forms.LightFormMatchers
import forms.declaration.ExporterDetails
import helpers.views.declaration.CommonMessages
import org.scalatest.{MustMatchers, WordSpec}

class ExporterDetailsSpec extends WordSpec with MustMatchers with LightFormMatchers with CommonMessages {

  import forms.declaration.ExporterDetailsSpec._

  "Exporter Details" should {
    "have correct id" in {
      ExporterDetails.id mustEqual "ExporterDetails"
    }
  }

  "Exporter Details" should {
    "validate is eori and address is empty" in {
      ExporterDetails.form().bind(emptyExporterDetailsJSON).error("details") must haveMessage(eoriOrAddressEmpty)
    }
  }
}
