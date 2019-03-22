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

package helpers.views.supplementary

trait ProcedureCodesMessages {

  val procedureCodes: String = "supplementary.procedureCodes"

  val title: String = procedureCodes + ".title"
  val procCodeHeader: String = procedureCodes + ".procedureCode.header"
  val procCodeHeaderHint: String = procedureCodes + ".procedureCode.header.hint"
  val procCodeErrorEmpty: String = procedureCodes + ".procedureCode.error.empty"
  val procCodeErrorLength: String = procedureCodes + ".procedureCode.error.length"
  val procCodeErrorSpecialCharacters: String = procedureCodes + ".procedureCode.error.specialCharacters"
  val addProcCodeHeader: String = procedureCodes + ".additionalProcedureCode.header"
  val addProcCodeHeaderHint: String = procedureCodes + ".additionalProcedureCode.header.hint"
  val addProcCodeErrorLength: String = procedureCodes + ".additionalProcedureCode.error.length"
  val addProcCodeErrorSpecialCharacters: String = procedureCodes + ".additionalProcedureCode.error.specialCharacters"
  val addProcCodeErrorMandatory: String = procedureCodes + ".additionalProcedureCode.mandatory.error"
  val addProcCodeErrorMaxAmount: String = procedureCodes + ".additionalProcedureCode.maximumAmount.error"
  val addProcCodeErrorEmpty: String = procedureCodes + ".additionalProcedureCode.empty"
  val addProcCodeErrorDuplication: String = procedureCodes + ".additionalProcedureCode.duplication"
}
