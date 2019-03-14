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

package helpers

trait DeclarationTypeMessages {

  val declarationType: String = "supplementary.declarationType"

  val title: String = declarationType + ".title"
  val header: String = declarationType + ".header"
  val hint: String = declarationType + ".header.hint"
  val simplified: String = declarationType + ".inputText.simplified"
  val standard: String = declarationType + ".inputText.standard"
  val errorMessageEmpty: String = declarationType + ".inputText.error.empty"
  val errorMessageIncorrect :String = declarationType + ".inputText.error.incorrect"
}
