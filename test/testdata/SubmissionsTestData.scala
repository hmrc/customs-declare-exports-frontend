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

package testdata

import base.TestHelper.createRandomAlphanumericString
import models.Pointer
import models.declaration.notifications.{Notification, NotificationError}
import models.declaration.submissions.EnhancedStatus._
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import models.declaration.submissions.{Action, NotificationSummary, Submission, SubmissionStatus}
import models.declaration.submissions.Action.defaultDateTimeZone

import java.time.temporal.ChronoUnit.{DAYS, HOURS, MINUTES}
import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.util.Random

object SubmissionsTestData {

  val uuid: String = UUID.randomUUID().toString
  val uuid_2: String = UUID.randomUUID().toString
  val uuid_3: String = UUID.randomUUID().toString
  val uuid_4: String = UUID.randomUUID().toString
  val eori: String = "GB167676"
  val ducr: String = createRandomAlphanumericString(16)
  val lrn: String = createRandomAlphanumericString(22)
  val mrn: String = "MRN87878797"
  val mrn_2: String = "MRN12341234"
  val mrn_3: String = "MRN12341235"
  val mrn_4: String = "MRN12341236"
  val conversationId: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e11"
  val conversationId_2: String = "b1c09f1b-7c94-4e90-b754-7c5c71c55e22"
  val conversationId_3: String = "b1c09f1b-7c94-4e90-b754-7c5c71c55e23"
  val conversationId_4: String = "b1c09f1b-7c94-4e90-b754-7c5c71c55e24"

  def createSubmission(
    uuid: String = uuid,
    lrn: String = lrn,
    specifiedMrn: Option[String] = Some(mrn),
    specifiedDucr: Option[String] = Some(ducr),
    statuses: Seq[EnhancedStatus]
  ) = {
    val now = ZonedDateTime.now(defaultDateTimeZone)
    val summaries = statuses.map { status =>
      NotificationSummary(UUID.randomUUID(), now, enhancedStatus = status)
    }
    val action = Action(
      requestType = SubmissionRequest,
      id = conversationId,
      notifications = summaries.headOption.map(_ => summaries),
      decId = Some(uuid),
      versionNo = 1
    )
    Submission(
      uuid,
      eori,
      lrn,
      specifiedMrn,
      specifiedDucr,
      statuses.lastOption,
      statuses.lastOption.map(_ => now),
      actions = Seq(action),
      latestDecId = Some(uuid)
    )
  }

  lazy val action = Action(requestType = SubmissionRequest, id = conversationId, notifications = None, decId = Some(uuid), versionNo = 1)
  lazy val action_2 =
    Action(
      requestType = SubmissionRequest,
      id = conversationId_2,
      requestTimestamp = action.requestTimestamp.plus(2, DAYS),
      notifications = None,
      decId = Some(uuid),
      versionNo = 1
    )
  lazy val action_3 =
    Action(
      requestType = SubmissionRequest,
      id = conversationId_2,
      requestTimestamp = action.requestTimestamp.minus(2, DAYS),
      notifications = None,
      decId = Some(uuid),
      versionNo = 1
    )
  lazy val action_4 =
    Action(
      requestType = SubmissionRequest,
      id = conversationId_4,
      requestTimestamp = action.requestTimestamp.minus(2, DAYS),
      notifications = None,
      decId = Some(uuid),
      versionNo = 1
    )

  lazy val actionCancellation =
    Action(
      requestType = CancellationRequest,
      id = conversationId,
      requestTimestamp = action.requestTimestamp.plus(3, HOURS),
      notifications = None,
      decId = Some(uuid),
      versionNo = 1
    )
  lazy val actionCancellation_2 =
    Action(
      requestType = CancellationRequest,
      id = conversationId,
      requestTimestamp = action.requestTimestamp.plus(6, HOURS),
      notifications = None,
      decId = Some(uuid),
      versionNo = 1
    )

  lazy val submission: Submission =
    Submission(uuid, eori = eori, lrn = lrn, mrn = Some(mrn), ducr = Some(ducr), actions = Seq(action), latestDecId = Some(uuid))

  lazy val submission_2: Submission =
    Submission(uuid_2, eori = eori, lrn = lrn, mrn = Some(mrn_2), ducr = Some(ducr), actions = Seq(action_2), latestDecId = Some(uuid_2))

  lazy val submission_3: Submission =
    Submission(uuid_3, eori = eori, lrn = lrn, mrn = Some(mrn_3), ducr = Some(ducr), actions = Seq(action_3), latestDecId = Some(uuid_3))

  lazy val submission_4: Submission =
    Submission(uuid_4, eori = eori, lrn = lrn, mrn = Some(mrn_4), ducr = Some(ducr), actions = Seq(action_4), latestDecId = Some(uuid_4))

  lazy val cancelledSubmission: Submission =
    Submission(uuid, eori = eori, lrn = lrn, mrn = Some(mrn), ducr = Some(ducr), actions = Seq(action, actionCancellation), latestDecId = Some(uuid))

  private lazy val functionCodes: Seq[String] =
    Seq("01", "02", "03", "05", "06", "07", "08", "09", "10", "11", "16", "17", "18")
  private lazy val functionCodesRandomised: Iterator[String] = Random.shuffle(functionCodes).iterator

  private def randomResponseFunctionCode: String = functionCodesRandomised.next()

  val dateTimeIssued: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
  val dateTimeIssued_2: ZonedDateTime = dateTimeIssued.plus(3, MINUTES)
  val dateTimeIssued_3: ZonedDateTime = dateTimeIssued_2.plus(3, MINUTES)
  val functionCode: String = randomResponseFunctionCode
  val functionCode_2: String = randomResponseFunctionCode
  val functionCode_3: String = randomResponseFunctionCode
  val nameCode: Option[String] = None
  val errors = Seq(NotificationError(validationCode = "CDS12056", pointer = Some(Pointer("42A.26B"))))

  private val payloadExemplaryLength = 300
  val payload = createRandomAlphanumericString(payloadExemplaryLength)
  val payload_2 = createRandomAlphanumericString(payloadExemplaryLength)
  val payload_3 = createRandomAlphanumericString(payloadExemplaryLength)

  val notification =
    Notification(actionId = conversationId, mrn = mrn, dateTimeIssued = dateTimeIssued, status = SubmissionStatus.ACCEPTED, errors = errors)

  val notification_2 =
    Notification(actionId = conversationId, mrn = mrn, dateTimeIssued = dateTimeIssued_2, status = SubmissionStatus.REJECTED, errors = errors)

  val notification_3 =
    Notification(actionId = conversationId_2, mrn = mrn, dateTimeIssued = dateTimeIssued_3, status = SubmissionStatus.ACCEPTED, errors = Seq.empty)

  val notificationSummary =
    NotificationSummary(notificationId = UUID.fromString(uuid), dateTimeIssued = dateTimeIssued, enhancedStatus = CUSTOMS_POSITION_GRANTED)
  val notificationSummary_2 =
    NotificationSummary(notificationId = UUID.fromString(uuid_2), dateTimeIssued = dateTimeIssued_2, enhancedStatus = QUERY_NOTIFICATION_MESSAGE)
  val notificationSummary_3 =
    NotificationSummary(notificationId = UUID.fromString(uuid_3), dateTimeIssued = dateTimeIssued_3, enhancedStatus = ADDITIONAL_DOCUMENTS_REQUIRED)
}
