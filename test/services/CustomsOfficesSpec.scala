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

package services

import base.{Injector, UnitSpec}
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi
import services.model.CustomsOffice

import java.util.Locale

class CustomsOfficesSpec extends UnitSpec with Injector {

  private val customsOfficesService = instanceOf[CustomsOfficesService]
  private implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  "SupervisingCustomsOffice" should {

    "have 143 entries" in {
      customsOfficesService.all.length mustBe 96
    }

    "read values from CSV and order by description, alphabetically ascending" in {
      customsOfficesService.all must contain.inOrder(
        CustomsOffice("GBABD001", "Aberdeen, Ruby House"),
        CustomsOffice("GBLBA001", "Leeds, Peter Bennett House"),
        CustomsOffice("GBWXH001", "Wrexham, Plas Gororau")
      )
    }
  }
}
