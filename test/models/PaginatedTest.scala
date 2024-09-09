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

package models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

class PaginatedTest extends PlaySpec {

  private val results = Paginated(currentPageElements = Seq("value"), Page(index = 1, size = 2), elementsTotal = 3)
  private val json =
    Json.obj("currentPageElements" -> Json.arr("value"), "page" -> Json.obj("index" -> 1, "size" -> 2), "total" -> 3)

  "Paginated" should {
    "convert to JSON" in {
      Json.toJson[Paginated[String]](results) mustBe json
    }

    "convert from JSON" in {
      Json.fromJson[Paginated[String]](json) mustBe JsSuccess(results)
    }
  }

}
