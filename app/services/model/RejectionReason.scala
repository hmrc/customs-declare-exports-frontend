/*
 * Copyright 2020 HM Revenue & Customs
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

import com.github.tototoshi.csv._
import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.declaration.notifications.Notification
import models.{DeclarationType, ExportsDeclaration, Pointer}
import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}

import scala.io.Source

case class RejectionReason(code: String, summaryErrorMessage: String, url: Option[String], pageErrorMessage: Option[String], pointer: Option[Pointer])

object RejectionReason {
  implicit val format: OFormat[RejectionReason] = Json.format[RejectionReason]
  private val logger = Logger(this.getClass)

  def extractErrorDescription(useImprovedErrorMessage: Boolean, cdsDescription: String, exportsDescription: String): String =
    if (useImprovedErrorMessage && exportsDescription.nonEmpty) exportsDescription else cdsDescription

  private def applyErrorUrl(url: String): Option[String] = if (url.isEmpty) None else Some(url)

  def apply(list: List[String], useImprovedErrorMessage: Boolean, pageErrorsMap: Map[String, String]): RejectionReason = list match {
    case code :: cdsDescription :: exportsDescription :: url :: Nil =>
      RejectionReason(
        code,
        extractErrorDescription(useImprovedErrorMessage, cdsDescription, exportsDescription),
        applyErrorUrl(url),
        pageErrorsMap.get(code),
        None
      )
    case error =>
      logger.warn("Incorrect error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }

  /**
    * Placeholders for Item Id and Container Id.
    * Those placeholders are used in pointer urls in the error-dms-rej-list.csv
    */
  private val itemIdPlaceholder = "[ITEM_ID]"
  private val containerIdPlaceholder = "[CONTAINER_ID]"
  private val urlPathPlaceholder = "[URL_PATH]"

  /**
    * Build a url for items and containers.
    * For other pages return the input url.
    * Items contains [ITEM_ID] to replace with real item Id
    * Containers contains [CONTAINER_ID] to replace with real container Id
    */
  def url(url: String, declaration: ExportsDeclaration, pointerOpt: Option[Pointer]): String = {
    val defaultItemsUrl = "/customs-declare-exports/declaration/export-items"
    val defaultContainersUrl = "/customs-declare-exports/declaration/containers"

    if (url.contains(itemIdPlaceholder)) {
      pointerOpt match {
        case Some(pointer) =>
          pointer.sequenceArgs.headOption.flatMap { itemNo =>
            declaration.items.find(_.sequenceId.toString == itemNo).map { item =>
              url.replace(itemIdPlaceholder, item.id)
            }
          }.getOrElse(defaultItemsUrl)
        case _ => defaultItemsUrl
      }
    } else if (url.contains(containerIdPlaceholder)) {
      pointerOpt match {
        case Some(pointer) =>
          pointer.sequenceArgs.headOption.flatMap { containerNo =>
            (try {
              Some(declaration.containers.apply(containerNo.toInt - 1))
            } catch {
              case _: Throwable => None
            }).map(container => url.replace(containerIdPlaceholder, container.id))
          }.getOrElse(defaultContainersUrl)
        case _ => defaultContainersUrl
      }
    } else if (url.contains(urlPathPlaceholder)) {
      pointerBasedUrl(url, declaration, pointerOpt)
    } else url
  }

  private def pointerBasedUrl(url: String, declaration: ExportsDeclaration, pointerOpt: Option[Pointer]) =
    pointerOpt match {
      case Some(pointer) if pointer.toString == "declaration.declarantDetails.details.eori" =>
        if (declaration.`type` == DeclarationType.CLEARANCE && declaration.isExs && declaration.parties.personPresentingGoodsDetails.nonEmpty) {
          url.replace(urlPathPlaceholder, "person-presenting-goods")
        } else url.replace(urlPathPlaceholder, "declarant-details")
      case Some(pointer) => throw new IllegalArgumentException(s"Pointer [${pointer.toString}] does not have a corresponding URL")
    }

}

@Singleton
class RejectionReasons @Inject()(config: AppConfig) {

  private val logger = Logger(this.getClass)

  private val pageErrorsMap: Map[String, String] = {
    def convertRow(list: List[String]): (String, String) = list match {
      case code :: bratDescription :: exportsDescription :: Nil =>
        code -> RejectionReason.extractErrorDescription(config.isUsingImprovedErrorMessages, bratDescription, exportsDescription)
      case error =>
        logger.warn("Incorrect error: " + error)
        throw new IllegalArgumentException("PageErrors has incorrect structure")
    }

    val reader =
      CSVReader.open(Source.fromURL(getClass.getClassLoader.getResource("code-lists/page-error-list.csv"), "UTF-8"))

    val errors: List[List[String]] = reader.all()

    errors.map(convertRow).toMap
  }

  val allRejectionReasons: List[RejectionReason] = {
    val reader =
      CSVReader.open(Source.fromURL(getClass.getClassLoader.getResource("code-lists/errors-dms-rej-list.csv"), "UTF-8"))

    val errors: List[List[String]] = reader.all()

    errors.map(errs => RejectionReason.apply(errs, config.isUsingImprovedErrorMessages, pageErrorsMap))
  }

  def unknown(errorCode: String, pointer: Option[Pointer]) = RejectionReason(errorCode, "Unknown error", None, None, pointer)

  def fromNotifications(notifications: Seq[Notification])(implicit messages: Messages): Seq[RejectionReason] = {
    val rejectedNotification = notifications.find(_.isStatusRejected)

    rejectedNotification.map { notification =>
      notification.errors.map { error =>
        val pointer = error.pointer.filter { p =>
          val defined = messages.isDefinedAt(p.messageKey)
          if (!defined) logger.warn("Missing error message key: " + p.messageKey)
          defined
        }

        val url = error.url

        val rejectedReason = allRejectionReasons
          .find(_.code == error.validationCode)
          .map(_.copy(pointer = pointer))
          .getOrElse(unknown(error.validationCode, pointer))

        if (url.isDefined) {
          rejectedReason.copy(url = url)
        } else rejectedReason
      }
    }.getOrElse(Seq.empty)
  }

}
