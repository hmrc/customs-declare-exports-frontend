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

package utils.validators.forms

import play.api.data.FormBinding
import play.api.mvc.Request

trait AutoCompleteFieldBinding {

  // Suffix added by 'auto-complete-fix.js' to the form-binding key of the auto-complete input field.
  private val suffix = "-autocomp"
  private val suffixLen = suffix.length

  def formValuesFromRequest(key: String)(implicit request: Request[_], formBinding: FormBinding): Map[String, Seq[String]] = {
    val formValues = formBinding(request)
    if (formValues.contains(key)) formValues
    else {
      // Removing the suffix from a key, if such a suffixed key exists
      val resultingMap = formValues.map(kv => if (kv._1.endsWith(suffix)) (kv._1.dropRight(suffixLen) -> kv._2) else kv)

      if (resultingMap.contains(key)) resultingMap
      // Add the expected key, with no value, to the map if still not present.
      else resultingMap + (key -> List.empty)
    }
  }
}
