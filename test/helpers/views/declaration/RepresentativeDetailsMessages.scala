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

trait RepresentativeDetailsMessages {

  val representativeDetails: String = "supplementary.representative"

  val title: String = representativeDetails + ".title"
  val header: String = representativeDetails + ".header"
  val eoriInfo: String = representativeDetails + ".eori.info"
  val addressInfo: String = representativeDetails + ".address.info"
  val repTypeHeader: String = representativeDetails + ".representationType.header"
  val repTypeDeclarant: String = representativeDetails + ".representationType.declarant"
  val repTypeDirect: String = representativeDetails + ".representationType.direct"
  val repTypeIndirect: String = representativeDetails + ".representationType.indirect"
  val repTypeErrorEmpty: String = representativeDetails + ".representationType.error.empty"
  val repTypeErrorWrongValue: String = representativeDetails + ".representationType.error.wrongValue"
}
