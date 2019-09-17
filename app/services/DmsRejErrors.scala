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

package services

import java.io.File

import com.github.tototoshi.csv._
import services.model.DmsRejError

object DmsRejErrors {

  private val errors: List[DmsRejError] = {
    val reader = CSVReader.open(new File("conf/code-lists/errors-dms-rej-list.csv"))

    val errors: List[List[String]] = reader.all()

    errors.map(DmsRejError.apply _)
  }

  val allRejectedErrors: List[DmsRejError] = errors
}
