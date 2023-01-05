/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import javax.inject.Singleton

@Singleton
class PermanentExportOrDispatch(base: String, targetAuthHolder: String, targetConditions: String, targetDocuments: String, targetSection: String) {
  def authHolder = s"$base#$targetAuthHolder"
  def conditions = s"$base#$targetConditions"
  def documents = s"$base#$targetDocuments"
  def section = s"$base#$targetSection"
}

object PermanentExportOrDispatch {

  def apply(loadConfig: String => String): PermanentExportOrDispatch =
    new PermanentExportOrDispatch(
      base = loadConfig("urls.procedureCodes.permanentExportOrDispatch.base"),
      targetAuthHolder = loadConfig("urls.procedureCodes.permanentExportOrDispatch.targetAuthHolder"),
      targetConditions = loadConfig("urls.procedureCodes.permanentExportOrDispatch.targetConditions"),
      targetDocuments = loadConfig("urls.procedureCodes.permanentExportOrDispatch.targetDocuments"),
      targetSection = loadConfig("urls.procedureCodes.permanentExportOrDispatch.targetSection")
    )
}
