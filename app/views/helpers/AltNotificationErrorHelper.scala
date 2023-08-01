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

package views.helpers

import connectors.CodeListConnector
import controllers.routes.SubmissionsController
import models.{ExportsDeclaration, Pointer}
import models.declaration.errors.{ErrorInstance, FieldInvolved}
import models.declaration.notifications.Notification
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import views.html.components.gds.{link, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AltNotificationErrorHelper @Inject() (link: link, codeListConnector: CodeListConnector, paragraphBody: paragraphBody) {

  def generateErrorRows(
    maybeNotification: Option[Notification],
    declaration: ExportsDeclaration,
    draftDecInProgress: Option[ExportsDeclaration],
    isAmendment: Boolean
  )(implicit messages: Messages): Seq[ErrorInstance] =
    maybeNotification.map { notification =>
      val errorsGroupedByCode = notification.errors.groupBy(_.validationCode)

      errorsGroupedByCode.zipWithIndex.map { case ((errorCode, errors), idx) =>
        val errorFieldsInvolved = errors.flatMap { error =>
          error.pointer.map { errorPointer =>
            val pointerRecord = PointerHelper.pointerRecords(errorPointer.pattern)

            val originalValue = pointerRecord.fetchValue(declaration, errorPointer.sequenceIndexes: _*)
            val draftValue = draftDecInProgress.flatMap(pointerRecord.fetchValue(_, errorPointer.sequenceIndexes: _*))
            val updatedValue = if (draftValue != originalValue) draftValue else None

            val changeLink = errorChangeAction(errorCode, Some(errorPointer), declaration, isAmendment)

            FieldInvolved(errorPointer, originalValue, updatedValue, changeLink, error.description)
          }
        }

        ErrorInstance(declaration, idx + 1, errorCode, errorFieldsInvolved.distinct)
      }.toSeq
    }.getOrElse(Seq.empty[ErrorInstance])

  private def errorChangeAction(validationCode: String, pointer: Option[Pointer], declaration: ExportsDeclaration, isAmendment: Boolean)(
    implicit messages: Messages
  ): Option[Html] = {
    def constructChangeLinkAction(call: Call): Html = {
      val errorPattern = pointer.map(_.pattern).getOrElse("")
      val errorMessage = messages(s"dmsError.${validationCode}.title")
      val url = SubmissionsController.amendErrors(declaration.id, call.url, errorPattern, errorMessage, isAmendment).url

      link(text = messages("site.change"), call = Call("GET", url), id = Some("item-header-action"))
    }

    PointerHelper
      .getChangeLinkCall(pointer, declaration)
      .map(call => constructChangeLinkAction(call))
  }
}
