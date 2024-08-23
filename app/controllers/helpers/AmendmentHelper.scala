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

package controllers.helpers

import connectors.CodeListConnector
import models.{ExportsDeclaration, Pointer}
import play.api.Logging
import play.api.i18n.Messages
import views.helpers.{CountryHelper, PointerPatterns, PointerRecord}

import javax.inject.{Inject, Singleton}

case class AmendmentInstance(pointer: Pointer, fieldId: String, originalValue: Option[String], amendedValue: Option[String])

@Singleton
class AmendmentHelper @Inject() (implicit codeListConnector: CodeListConnector, countryHelper: CountryHelper) extends Logging {
  def generateAmendmentRows(originalDeclaration: ExportsDeclaration, amendedDeclaration: ExportsDeclaration)(
    implicit messages: Messages
  ): Seq[AmendmentInstance] = {
    val amendedPointers = {
      val diff = amendedDeclaration.createDiff(originalDeclaration)
      diff.flatMap { alteredField =>
        PointerPatterns.expandPointer(Pointer.apply(alteredField.fieldPointer), originalDeclaration, amendedDeclaration)
      }
    }

    val pointersAndRecords = amendedPointers.map(pointer => (pointer, PointerRecord.pointersToPointerRecords(pointer.pattern)))

    pointersAndRecords.flatMap { case (pointer, record) =>
      record.amendKey.fold {
        logger.warn(s"No amend key found for pointer [$pointer]")
        Option.empty[AmendmentInstance]
      } { key =>
        val origVal = record.fetchReadableValue(originalDeclaration, pointer.sequenceIndexes: _*)
        val amendedVal = record.fetchReadableValue(amendedDeclaration, pointer.sequenceIndexes: _*)

        if (origVal.isEmpty && amendedVal.isEmpty)
          Option.empty[AmendmentInstance]
        else
          Some(AmendmentInstance(pointer, key, origVal, amendedVal))
      }
    }
  }
}
