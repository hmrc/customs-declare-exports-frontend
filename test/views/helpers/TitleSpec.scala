/*
 * Copyright 2023 HM Revenue & Customs
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

package views.helpers

import views.common.UnitViewSpec

class TitleSpec extends UnitViewSpec {

  val serviceName = messages("service.name")

  "Title" should {

    "format title without section" in {
      val message = s"${messages("declaration.declarationType.title")} - $serviceName - GOV.UK"
      Title("declaration.declarationType.title").toString(messages) mustBe message
    }

    "format title with section" in {
      val message = messages("declaration.declarationType.header.SUPPLEMENTARY")
      val expectedTitle = s"${messages("declaration.declarationType.title")} - ${message} - $serviceName - GOV.UK"

      val title = Title("declaration.declarationType.title", "declaration.declarationType.header.SUPPLEMENTARY")
      title.toString(messages) mustBe expectedTitle
    }
  }
}
