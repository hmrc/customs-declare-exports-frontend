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

package helpers.views.declaration

trait ConsignmentReferencesMessages {

  val consignmentReferences: String = "supplementary.consignmentReferences"

  val title: String = consignmentReferences + ".title"
  val header: String = consignmentReferences + ".header"
  val lrnInfo: String = consignmentReferences + ".lrn.info"
  val lrnHint: String = consignmentReferences + ".lrn.hint"
  val lrnEmpty: String = consignmentReferences + ".lrn.error.empty"
  val lrnLength: String = consignmentReferences + ".lrn.error.length"
  val lrnSpecialCharacter: String = consignmentReferences + ".lrn.error.specialCharacter"
  val ucrInfo: String = consignmentReferences + ".ucr.info"
  val ucrHint: String = consignmentReferences + ".ucr.hint"
}
