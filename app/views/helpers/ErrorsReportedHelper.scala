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
import models.DeclarationType.CLEARANCE
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.helpers.PointerHelper.clearanceDecDetailsCall
import views.html.components.gds.{link, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class ErrorsReportedHelper @Inject() (link: link, codeListConnector: CodeListConnector, pointerRecords: PointerRecords, paragraphBody: paragraphBody)
    extends Logging {

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
            val record = pointerRecords.library.get(errorPointer.pattern).getOrElse {
              logger.warn(s"PointerRecords MISSING for '${errorPointer.pattern}''")
              pointerRecords.defaultPointerRecord
            }
            val originalValue = record.fetchValue(declaration, errorPointer.sequenceIndexes: _*)
            val draftValue = draftDecInProgress.flatMap(record.fetchValue(_, errorPointer.sequenceIndexes: _*))
            val updatedValue = if (draftValue != originalValue) draftValue else None

            val changeLink = errorChangeAction(errorCode, errorPointer, record, declaration, isAmendment)

            FieldInvolved(errorPointer, originalValue, updatedValue, changeLink, error.description)
          }
        }

        ErrorInstance(declaration, idx + 1, errorCode, errorFieldsInvolved.distinct)
      }.toSeq
    }.getOrElse(Seq.empty[ErrorInstance])

  private def errorChangeAction(
    validationCode: String,
    pointer: Pointer,
    pointerRecord: PointerRecord,
    declaration: ExportsDeclaration,
    isAmendment: Boolean
  )(implicit messages: Messages): Option[Html] = {
    def constructChangeLinkAction(call: Call): Html = {
      val errorMessage = messages(s"dmsError.${validationCode}.title")
      val url = SubmissionsController.amendErrors(declaration.id, pointer.pattern, errorMessage, isAmendment, RedirectUrl(call.url)).url

      link(text = messages("site.change"), call = Call("GET", url), id = Some("item-header-action"))
    }

    getChangeLinkCall(pointer, pointerRecord, declaration)
      .map(call => constructChangeLinkAction(call))
  }

  private val containsItemsSeqRegEx = """.*items\.\$.*""".r
  private val containsDecDetailsEoriRegEx = """^declaration.declarantDetails.details.eori$""".r

  def getChangeLinkCall(pointer: Pointer, pointerRecord: PointerRecord, declaration: ExportsDeclaration): Option[Call] =
    pointer.pattern match {
      case containsItemsSeqRegEx(_*)       => getItemId(pointer, pointerRecord, declaration)
      case containsDecDetailsEoriRegEx(_*) => getDecDetailsEoriChangeLinkUrl(pointer, pointerRecord, declaration)
      case _                               => pointerRecord.pageLink1Param
    }

  private def getItemId(pointer: Pointer, pointerRecord: PointerRecord, declaration: ExportsDeclaration): Option[Call] = {
    val maybeItemId = for {
      seqNo <- pointer.sequenceArgs.headOption
      item <- declaration.itemBySequenceNo(seqNo)
    } yield item.id

    maybeItemId
      .map(itemId => pointerRecord.pageLink2Param.map(_(itemId)).orElse(pointerRecord.pageLink1Param))
      .getOrElse {
        logger.warn(s"Was not able to specialise the provided error change link url for declaration ${declaration.id}")
        None
      }
  }

  private def getDecDetailsEoriChangeLinkUrl(pointer: Pointer, pointerRecord: PointerRecord, declaration: ExportsDeclaration): Option[Call] =
    if (declaration.isType(CLEARANCE) && declaration.isExs && declaration.parties.personPresentingGoodsDetails.nonEmpty)
      Some(clearanceDecDetailsCall)
    else
      pointerRecord.pageLink1Param
}
