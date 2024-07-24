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
import models.{AmendmentOp, ExportsDeclaration}
import models.ExportsFieldPointer.ExportsFieldPointer
import play.api.Logging
import play.api.i18n.Messages
import services.OriginalAndNewValues
import views.helpers.{CountryHelper, PointerRecord}

import javax.inject.{Inject, Singleton}

case class AmendmentInstance(pointer: ExportsFieldPointer, fieldId: String, originalValue: Option[String], amendedValue: Option[String])

//TODO add logic that fetches all leaf (only) pointer records when a parent level pointer comes through
// capture any logic from valueAdded et al methods on case classes#
@Singleton
class AmendmentHelper @Inject() (implicit codeListConnector: CodeListConnector, countryHelper: CountryHelper) extends Logging {
  def generateAmendmentRows(originalDeclaration: ExportsDeclaration, amendedDeclaration: ExportsDeclaration)(
    implicit messages: Messages
  ): Seq[AmendmentInstance] = {
    val amendedPointers = {
      val diff = amendedDeclaration.createDiff(originalDeclaration)

      diff.flatMap(af => af.values.newVal.orElse(af.values.originalVal).map {
        case Some(a: AmendmentOp) =>
          val convertedPointer = af.fieldPointer.replaceAll("\\.#[0-9]+\\.?", ".")
          val finalPointer = if (convertedPointer.endsWith(".")) convertedPointer.dropRight(1) else convertedPointer
          a.getLeafPointersIfAny(finalPointer)
        case _ =>
          logger.warn(s"'AlteredField(${af.fieldPointer}) with no value difference??")
          Seq.empty
      }).flatten
    }

    val pointersAndRecords = amendedPointers.map(pointer => (pointer, PointerRecord.library(pointer)))

    pointersAndRecords.flatMap { case (pointer, record) =>
      record.amendKey.fold {
        logger.warn(s"No amend key found for pointer [$pointer]")
        Option.empty[AmendmentInstance]
      } {
        key => Some(AmendmentInstance(pointer, key, record.fetchReadableValue(originalDeclaration), record.fetchReadableValue(amendedDeclaration)))
      }
    }
  }
}
