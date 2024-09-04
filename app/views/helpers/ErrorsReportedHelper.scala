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
import controllers.timeline.routes.SubmissionsController
import models.DeclarationType.CLEARANCE
import models.declaration.errors.{ErrorInstance, FieldInvolved}
import models.declaration.notifications.{Notification, NotificationError}
import models.{ExportsDeclaration, Pointer}
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.helpers.PointerHelper.clearanceDecDetailsCall
import views.html.components.gds.link

import javax.inject.{Inject, Singleton}

@Singleton
class ErrorsReportedHelper @Inject() (link: link, codeListConnector: CodeListConnector, countryHelper: CountryHelper) extends Logging {

  def generateErrorRows(
    maybeNotification: Option[Notification],
    declaration: ExportsDeclaration,
    draftDecInProgress: Option[ExportsDeclaration],
    isAmendment: Boolean
  )(implicit messages: Messages): Seq[ErrorInstance] =
    maybeNotification.fold(List.empty[ErrorInstance]) { notification =>
      notification.errors
        .groupBy(_.validationCode)
        .zipWithIndex
        .map { case ((errorCode, errors), idx) =>
          val errorFieldsInvolved = errors.flatMap { notificationError =>
            val error = handleNotificationsWithoutPointers(errorCode, notificationError)
            getFieldInvolved(error, declaration, draftDecInProgress, isAmendment)
          }

          ErrorInstance(declaration, idx + 1, errorCode, errorFieldsInvolved.distinct, isAmendment)
        }
        .toList
    }

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
  private val containsContainersSeqRegEx = """.*container\.\$.*""".r
  private val containsDecDetailsEoriRegEx = """^declaration.declarantDetails.details.eori$""".r

  private def getChangeLinkCall(pointer: Pointer, pointerRecord: PointerRecord, declaration: ExportsDeclaration): Option[Call] =
    pointer.pattern match {
      case containsItemsSeqRegEx(_*)       => getItemId(pointer, pointerRecord, declaration)
      case containsContainersSeqRegEx(_*)  => getContainerId(pointer, pointerRecord, declaration)
      case containsDecDetailsEoriRegEx(_*) => getDecDetailsEoriChangeLinkUrl(pointer, pointerRecord, declaration)
      case _                               => pointerRecord.pageLink1Param
    }

  private def handleNotificationsWithoutPointers(errorCode: String, notificationError: NotificationError): NotificationError =
    errorCode match {
      case "CDS12062" if notificationError.pointer.isEmpty => NotificationError.CDS12062
      case _                                               => notificationError
    }

  private def getFieldInvolved(
    notificationError: NotificationError,
    declaration: ExportsDeclaration,
    draftDecInProgress: Option[ExportsDeclaration],
    isAmendment: Boolean
  )(implicit messages: Messages): Option[FieldInvolved] =
    notificationError.pointer.map { errorPointer =>
      val record = PointerRecord.pointersToPointerRecords.getOrElse(
        errorPointer.pattern, {
          logger.warn(s"PointerRecords MISSING for '${errorPointer.pattern}''")
          PointerRecord.defaultPointerRecord
        }
      )
      val originalValue = record.fetchReadableValue(declaration, errorPointer.sequenceIndexes: _*)(messages, countryHelper, codeListConnector)
      val draftValue =
        draftDecInProgress.flatMap(record.fetchReadableValue(_, errorPointer.sequenceIndexes: _*)(messages, countryHelper, codeListConnector))
      val updatedValue = if (draftValue != originalValue) draftValue else None

      val changeLink = errorChangeAction(notificationError.validationCode, errorPointer, record, declaration, isAmendment)

      FieldInvolved(errorPointer, originalValue, updatedValue, changeLink, notificationError.description)
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

  private def getContainerId(pointer: Pointer, record: PointerRecord, declaration: ExportsDeclaration): Option[Call] = {
    val maybeContainerId = for {
      seqId <- pointer.sequenceArgs.headOption
      container <- declaration.containerBySeqId(seqId)
    } yield container.id

    maybeContainerId
      .map(containerId => record.pageLink2Param.map(_(containerId)).orElse(record.pageLink1Param))
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
