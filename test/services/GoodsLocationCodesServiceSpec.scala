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
import models.codes.GoodsLocationCode
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi

import java.util.Locale

class GoodsLocationCodesServiceSpec extends UnitSpec with Injector {

  private val goodsLocationCodesService = instanceOf[GoodsLocationCodesService]
  private implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  "OfficeOfExits" should {

    "have all entries" in {
      goodsLocationCodesService.all.length mustBe 80
    }

    "read values in order" in {
      goodsLocationCodesService.all.head mustBe GoodsLocationCode(
        "GBAUBA5ABDCGD",
        "Aberdeen 410 /  Grampian Continental Ltd, Birchwood Works, Kinellar, Aberdeen, AB21 0SH"
      )
    }
  }
}
