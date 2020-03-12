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

package models.dis.parsers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import models.dis.{MrnStatus, PreviousDocument, XmlTags}

import scala.xml.NodeSeq

class MrnStatusParser {

  private val timeStampFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssz")
  private val displayFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' hh:mma")

  def parse(responseXml: NodeSeq): MrnStatus = {
    import models.dis.XmlTags._

    MrnStatus(
      mrn = (responseXml \ declarationStatusDetails \ declaration \ id).text,
      versionId = (responseXml \ declarationStatusDetails \ declaration \ versionId).text,
      eori = (responseXml \ declarationStatusDetails \ declaration \ submitter \ id).text,
      declarationType = ((responseXml \ declarationStatusDetails \ declaration)(1) \ typeCode).text,
      ucr = StringOption(((responseXml \ declarationStatusDetails \ declaration)(1) \ goodsShipment \ ucr \ traderAssignedReferenceID).text),
      receivedDateTime = timeFormatter((responseXml \ declarationStatusDetails \ declaration \ receivedDateTime \ dateTimeString).text),
      releasedDateTime =
        StringOption((responseXml \ declarationStatusDetails \ declaration \ goodsReleasedDateTime \ dateTimeString).text).map(timeFormatter),
      acceptanceDateTime =
        StringOption((responseXml \ declarationStatusDetails \ declaration \ acceptanceDateTime \ dateTimeString).text).map(timeFormatter),
      createdDateTime = timeFormatter(ZonedDateTime.now()),
      roe = (responseXml \ declarationStatusDetails \ declaration \ roe).text,
      ics = (responseXml \ declarationStatusDetails \ declaration \ ics).text,
      irc = StringOption((responseXml \ declarationStatusDetails \ declaration \ irc).text),
      totalPackageQuantity = ((responseXml \ declarationStatusDetails \ declaration)(1) \ totalPackageQuantity).text,
      goodsItemQuantity = ((responseXml \ declarationStatusDetails \ declaration)(1) \ goodsItemQuantity).text,
      previousDocuments = previousDocuments(((responseXml \ declarationStatusDetails \ declaration)(1) \ goodsShipment) \ previousDocument)
    )
  }

  private def previousDocuments(documents: NodeSeq): Seq[PreviousDocument] =
    documents.map { doc =>
      val id = (doc \ XmlTags.id).text
      val typeCode = (doc \ XmlTags.typeCode).text
      PreviousDocument(id, typeCode)
    }

  private def timeFormatter(zonedDateTime: String): String = timeFormatter(ZonedDateTime.parse(zonedDateTime, timeStampFormatter))

  private def timeFormatter(zonedDateTime: ZonedDateTime): String =
    displayFormatter
      .format(zonedDateTime)
      .replace("AM", "am")
      .replace("PM", "pm")

}
