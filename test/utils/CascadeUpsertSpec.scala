/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.libs.json._
import base.SpecBase
import identifiers._
import models._
import uk.gov.hmrc.http.cache.client.CacheMap

class CascadeUpsertSpec extends SpecBase {

  "using the apply method for a key that has no special function" when {
    "the key doesn't already exists" must {
      "add the key to the cache map" in {
        val originalCacheMap = new CacheMap("id", Map())
        val cascadeUpsert = new CascadeUpsert
        val result = cascadeUpsert("key", "value", originalCacheMap)
        result.data mustBe Map("key" -> JsString("value"))
      }
    }

    "data already exists for that key" must {
      "replace the value held against the key" in {
        val originalCacheMap = new CacheMap("id", Map("key" -> JsString("original value")))
        val cascadeUpsert = new CascadeUpsert
        val result = cascadeUpsert("key", "new value", originalCacheMap)
        result.data mustBe Map("key" -> JsString("new value"))
      }
    }
  }

  "addRepeatedValue" when {
    "the key doesn't already exist" must {
      "add the key to the cache map and save the value in a sequence" in {
        val originalCacheMap = new CacheMap("id", Map())
        val cascadeUpsert = new CascadeUpsert
        val result = cascadeUpsert.addRepeatedValue("key", "value", originalCacheMap)
        result.data mustBe Map("key" -> Json.toJson(Seq("value")))
      }
    }

    "the key already exists" must {
      "add the new value to the existing sequence" in {
        val originalCacheMap = new CacheMap("id", Map("key" -> Json.toJson(Seq("value"))))
        val cascadeUpsert = new CascadeUpsert
        val result = cascadeUpsert.addRepeatedValue("key", "new value", originalCacheMap)
        result.data mustBe Map("key" -> Json.toJson(Seq("value", "new value")))
      }
    }
  }
}
