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
  val headerSimplifiedDec: String = declarationType + ".header.simplified"
  val headerOccasionalDec: String = declarationType + ".header.occasional"
  val headerClearanceDec: String = declarationType + ".header.clearance"
  val hint: String = declarationType + ".header.hint"
  val supplementarySimplified: String = declarationType + ".inputText.supplementary.simplified"
  val supplementaryStandard: String = declarationType + ".inputText.supplementary.standard"
  val standardPreLodged: String = declarationType + ".inputText.standard.preLodged"
  val standardFrontier: String = declarationType + ".inputText.standard.frontier"
  val simplifiedPreLodged: String = declarationType + ".inputText.simplified.preLodged"
  val simplifiedFrontier: String = declarationType + ".inputText.simplified.frontier"
  val occasionalPreLodged: String = declarationType + ".inputText.occasional.preLodged"
  val occasionalFrontier: String = declarationType + ".inputText.occasional.frontier"
  val clearancePreLodged: String = declarationType + ".inputText.clearance.preLodged"
  val clearanceFrontier: String = declarationType + ".inputText.clearance.frontier"
  val errorMessageEmpty: String = declarationType + ".inputText.error.empty"
  val errorMessageIncorrect: String = declarationType + ".inputText.error.incorrect"
}
