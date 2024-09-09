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

package utils

import base.UnitSpec
import forms.section2.authorisationHolder.AuthorisationHolder

class ListItemSpec extends UnitSpec {

  import ListItem._
  "ListItemId" should {

    val item1 = AuthorisationHolder(Some("code1"), None, None)
    val item2 = AuthorisationHolder(Some("code2"), None, None)
    val item3 = AuthorisationHolder(Some("code3"), None, None)
    val items = Seq(item1, item2, item3)
    val id2 = ListItem.createId(1, item2)

    "generate id" in {
      val item = AuthorisationHolder(Some("some code"), None, None)
      createId(2, item) mustBe s"2.${item.hashCode()}"
    }

    "find item by id" in {
      findById(id2, items) mustBe Some(item2)
    }

    "not find item by id" when {

      "id is invalid" in {
        findById("1.1", items) mustBe None
        findById("1-1", items) mustBe None
        findById("1", items) mustBe None
        findById("A.B", items) mustBe None
      }

      "item is not in list" in {
        findById(id2, Seq.empty) mustBe None
      }
    }
  }
}
