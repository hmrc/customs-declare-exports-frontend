/*
 * Copyright 2023 HM Revenue & Customs
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

package services.model

import scala.io.Source

import com.github.tototoshi.csv._
import play.api.Logging

case class FieldNamePointer(exportPattern: String, fieldName: Option[String])

object FieldNamePointer extends Logging {

  def apply(data: List[String]): FieldNamePointer = data match {
    case exportPattern :: fieldName :: Nil => FieldNamePointer(exportPattern, applyFieldName(fieldName))
    case error =>
      logger.warn("Incorrect data: " + error)
      throw new IllegalArgumentException("Field name pointer file has incorrect structure")
  }

  private def applyFieldName(name: String): Option[String] =
    if (name.isEmpty) None else Some(name)

  val allFieldNamePointers: List[FieldNamePointer] = {
    val reader =
      CSVReader.open(Source.fromURL(getClass.getClassLoader.getResource("code-lists/field-name-pointer.csv"), "UTF-8"))

    val errors: List[List[String]] = reader.all()

    errors.map(FieldNamePointer.apply)
  }

  def getFieldName(exportsPattern: String): Option[String] =
    allFieldNamePointers.find(_.exportPattern == exportsPattern).flatMap(_.fieldName)
}
