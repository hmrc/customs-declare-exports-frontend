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

package models

import org.scalatestplus.play.PlaySpec

class PageTest extends PlaySpec {

  "Page" should {
    "bind" when {
      "both params populated" in {
        Page.bindable.bind("page", Map("page-index" -> Seq("10"), "page-size" -> Seq("20"))) mustBe Some(Right(Page(10, 20)))
      }

      "index only populated" in {
        Page.bindable.bind("page", Map("page-index" -> Seq("10"))) mustBe Some(Right(Page(index = 10)))
      }

      "size only populated" in {
        Page.bindable.bind("page", Map("page-size" -> Seq("20"))) mustBe Some(Right(Page(size = 20)))
      }

      "nothing populated" in {
        Page.bindable.bind("page", Map()) mustBe Some(Right(Page()))
      }
    }

    "unbind" in {
      Page.bindable.unbind("page", Page(1, 2)) mustBe "page-index=1&page-size=2"
    }
  }

}
