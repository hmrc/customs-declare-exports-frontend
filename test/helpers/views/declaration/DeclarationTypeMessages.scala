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

trait DeclarationTypeMessages {

  val declarationType: String = "declaration.declarationType"

  val title: String = declarationType + ".title"
  val headerSupplementaryDec: String = declarationType + ".header.supplementary"
  val headerStandardDec: String = declarationType + ".header.standard"
  val hint: String = declarationType + ".header.hint"
  val simplified: String = declarationType + ".inputText.supplementary.simplified"
  val standard: String = declarationType + ".inputText.supplementary.standard"
  val preLodged: String = declarationType + ".inputText.standard.preLodged"
  val frontier: String = declarationType + ".inputText.standard.frontier"
  val errorMessageEmpty: String = declarationType + ".inputText.error.empty"
  val errorMessageIncorrect: String = declarationType + ".inputText.error.incorrect"
}
