/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{DeclarationType, ExportsDeclaration, Pointer}
import models.declaration.notifications.Notification
import controllers.declaration.routes
import play.api.Logging
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

case class RejectionReason(errorCode: String, pointer: Option[Pointer], url: Option[String])

object RejectionReason extends Logging {
  implicit val format: OFormat[RejectionReason] = Json.format[RejectionReason]

  def fromNotifications(notifications: Seq[Notification])(implicit messages: Messages): Seq[RejectionReason] = {
    val rejectedNotification = notifications.find(_.isStatusDMSRej)

    rejectedNotification.map { notification =>
      if (notification.errors.size < 1) logger.warn("Missing errors in rejected notification! ActionId: " + notification.actionId)

      notification.errors.map { notificationError =>
        val maybePointer = notificationError.pointer.filter { pointer =>
          val defined = messages.isDefinedAt(pointer.messageKey)
          if (!defined) logger.warn("Missing error message key: " + pointer.messageKey)
          defined
        }

        RejectionReason(notificationError.validationCode, maybePointer, notificationError.url)
      }
    }.getOrElse(Seq.empty)
  }

  /**
    * Placeholders for Item Id and Url Path.
    * Those placeholders are used in the pointer mappings file on the backend
    */
  private val itemIdPlaceholder = "[ITEM_ID]"
  private val containsItemIdRegEx = ".*ITEM_ID.*".r
  private val containsUrlPathRegEx = ".*URL_PATH.*".r

  /**
    * Service Url definitions
    */
  private val itemsListUrl = routes.ItemsSummaryController.displayItemsSummaryPage().url
  private val declarantDetailsUrl = routes.DeclarantDetailsController.displayPage().url
  private val personPresentingGoodsUrl = routes.PersonPresentingGoodsDetailsController.displayPage().url

  /**
    * If required specialise the url by replacing any place holders that may exist, else leave as is.
    *
    * Items contains [ITEM_ID] to replace with real item Id
    * Parties contains [URL_PATH] to replace with the appropriate page url
    */
  def specialiseUrl(url: String, declaration: ExportsDeclaration, maybePointer: Option[Pointer]): String =
    url match {
      case containsItemIdRegEx(_*) =>
        maybePointer.fold(logResortingToDefault(itemsListUrl, declaration.id)) { pointer =>
          val maybeUrl = for {
            seqNo <- pointer.sequenceArgs.headOption
            item <- declaration.itemBySequenceNo(seqNo)
          } yield url.replace(itemIdPlaceholder, item.id)

          maybeUrl.getOrElse(logResortingToDefault(itemsListUrl, declaration.id))
        }

      case containsUrlPathRegEx(_*) =>
        maybePointer.fold(logResortingToDefault(declarantDetailsUrl, declaration.id))(getPartiesChangeLinkUrl(_, declaration))

      case _ => url
    }

  private def getPartiesChangeLinkUrl(pointer: Pointer, declaration: ExportsDeclaration): String =
    if (pointer.toString == "declaration.declarantDetails.details.eori" &&
        declaration.`type` == DeclarationType.CLEARANCE &&
        declaration.isExs &&
        declaration.parties.personPresentingGoodsDetails.nonEmpty) personPresentingGoodsUrl
    else declarantDetailsUrl

  private def logResortingToDefault(defaultUrl: String, decId: String) = {
    logger.warn(s"Was not able to specialise the provided error change link url for declaration $decId")
    defaultUrl
  }
}
