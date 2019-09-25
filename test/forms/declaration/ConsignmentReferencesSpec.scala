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

package forms.declaration

import forms.{Ducr, Lrn}
import play.api.libs.json.{JsObject, JsString, JsValue}

object ConsignmentReferencesSpec {
  val exemplaryDucr = "8GB123456789012-1234567890QWERTYUIO"

  val correctConsignmentReferences = ConsignmentReferences(ducr = Ducr(ducr = exemplaryDucr), lrn = Lrn("123LRN"))
  val correctConsignmentReferencesNoDucr = ConsignmentReferences(ducr = Ducr(""), lrn = Lrn("123LRN"))
  val emptyConsignmentReferences = ConsignmentReferences(ducr = Ducr(""), lrn = Lrn(""))

  val correctConsignmentReferencesJSON: JsValue = JsObject(
    Map("ducr" -> JsObject(Map("ducr" -> JsString(exemplaryDucr))), "lrn" -> JsString("123LRN"))
  )
  val correctConsignmentReferencesNoDucrJSON: JsValue = JsObject(
    Map("ducr" -> JsObject(Map("ducr" -> JsString(""))), "lrn" -> JsString("123LRN"))
  )
  val emptyConsignmentReferencesJSON: JsValue = JsObject(
    Map("ducr" -> JsObject(Map("ducr" -> JsString(""))), "lrn" -> JsString(""))
  )
}
