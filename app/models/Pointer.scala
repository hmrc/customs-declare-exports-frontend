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

package models

import models.ExportsFieldPointer.ExportsFieldPointer
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Reads, Writes}

enum PointerSectionType {
  case FIELD, SEQUENCE
  implicit val pointerSectionTypeFormat: Format[PointerSectionType] =
    new Format[PointerSectionType] {

      def reads(json: JsValue): JsResult[PointerSectionType] =
        json.validate[String].flatMap { s =>
          PointerSectionType.values.find(_.toString == s) match {
            case Some(value) => JsSuccess(value)
            case None        => JsError(s"Unknown PointerSectionTypeFormat: $s")
          }
        }

      def writes(sectionType: PointerSectionType): JsValue =
        JsString(sectionType.toString)
    }

}

case class PointerSection(value: String, `type`: PointerSectionType) {
  lazy val pattern: String = `type` match {
    case PointerSectionType.FIELD    => value
    case PointerSectionType.SEQUENCE => "$"
  }

  override def toString: String = `type` match {
    case PointerSectionType.FIELD    => value
    case PointerSectionType.SEQUENCE => "#" + value
  }
}

object PointerSection {
  private val SEQUENCE_REGEX = "^#(\\d*)$".r
  implicit val format: Format[PointerSection] =
    Format[PointerSection](Reads(js => js.validate[String].map(PointerSection(_))), Writes(section => JsString(section.toString)))

  def apply(value: String): PointerSection = SEQUENCE_REGEX.findFirstMatchIn(value).map(_.group(1)) match {
    case Some(sequence) => PointerSection(sequence, PointerSectionType.SEQUENCE)
    case _              => PointerSection(value, PointerSectionType.FIELD)
  }
}

case class Pointer(sections: Seq[PointerSection]) {
  //  Converts a pointer into it's pattern form
  // e.g. ABC.DEF.*.GHI (if the pointer contains a sequence index)
  // e.g. ABC.DEF.GHI (if the pointer doesn't contain a sequence)
  lazy val pattern: String = sections.map(_.pattern).mkString(".")

  lazy val sequenceArgs: Seq[String] = sections.filter(_.`type` == PointerSectionType.SEQUENCE).map(_.value)
  lazy val sequenceIndexes: Seq[Int] = sequenceArgs.flatMap(_.toIntOption).map(_ - 1)

  lazy val messageKey: String = "field." + pattern

  // Converts a pointer to it's string form preserving the type
  // e.g. ABC.DEF.#1.GHI (if the pointer contains a sequence with index 1)
  // e.g. ABC.DEF.GHI (if the pointer doesnt contain a sequence)
  override def toString: String = sections.map(_.toString).mkString(".")
}

object Pointer {
  implicit val format: Format[Pointer] =
    Format(Reads(js => js.validate[String].map(Pointer(_))), Writes(pointer => JsString(pointer.toString)))

  def apply(sections: String): Pointer = Pointer(sections.split("\\.").toIndexedSeq.map(PointerSection(_)))
}

object ExportsFieldPointer {
  type ExportsFieldPointer = String
}

trait FieldMapping {
  val pointer: ExportsFieldPointer
}
