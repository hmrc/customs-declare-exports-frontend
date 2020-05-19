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

package controllers.util

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.time.temporal.ChronoUnit.{DAYS, HOURS, MINUTES}
import java.util.UUID

import base.TestHelper.createRandomAlphanumericString
import models.Pointer
import models.declaration.notifications.{Notification, NotificationError}
import models.declaration.submissions.RequestType.{CancellationRequest, SubmissionRequest}
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import org.scalatest.{MustMatchers, WordSpec}

import scala.util.Random

class SubmissionDisplayHelperSpec extends WordSpec with MustMatchers {

  import SubmissionDisplayHelperSpec._

  "SubmissionDisplayHelper on createSubmissionsWithNotificationsMap" when {

    "provided with empty lists of both Submissions and Notifications" should {
      "return empty Map" in {
        val submissions = Seq.empty[Submission]
        val notifications = Seq.empty[Notification]

        val result = SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications)

        result mustBe empty
      }
    }

    "provided with empty list of Submissions" should {
      "return empty Map" in {
        val submissions = Seq.empty[Submission]
        val notifications = Seq(notification, notification_2, notification_3)

        val result = SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications)

        result mustBe empty
      }
    }

    "provided with multiple Submissions and empty list of Notifications" should {
      "return Map with empty lists as values" in {
        val submissions = Seq(submission, submission_2)
        val notifications = Seq.empty[Notification]

        val result =
          Map(SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications): _*)

        val expectedKeySet = Set(submission, submission_2)
        result.size must equal(2)
        result.keySet must equal(expectedKeySet)
        result(submission) must equal(Seq.empty)
        result(submission_2) must equal(Seq.empty)
      }
    }

    "provided with multiple Submissions and matching Notifications" should {
      "return Map with Notifications assigned to the Submissions" in {
        val submissions = Seq(submission, submission_2)
        val notifications = Seq(notification, notification_2, notification_3)

        val result =
          Map(SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications): _*)

        val expectedKeySet = Set(submission, submission_2)
        val expectedNotificationsForFirstSubmission = Seq(notification, notification_2)
        val expectedNotificationsForSecondSubmission = Seq(notification_3)

        result.size must equal(2)
        result.keySet must equal(expectedKeySet)
        result(submission).length must equal(2)
        expectedNotificationsForFirstSubmission.foreach { result(submission) must contain(_) }
        result(submission_2).length must equal(1)
        expectedNotificationsForSecondSubmission.foreach { result(submission_2) must contain(_) }
      }
    }

    "provided with multiple Submissions and additional non-matching Notification" should {
      "return Map with matching Notifications only" in {
        val submissions = Seq(submission, submission_2)
        val additionalNotification = notification.copy(actionId = "anything-different", mrn = "any-other-mrn")
        val notifications = Seq(notification, notification_2, notification_3, additionalNotification)

        val result =
          Map(SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications): _*)

        val expectedKeySet = Set(submission, submission_2)
        val expectedNotificationsForFirstSubmission = Seq(notification, notification_2)
        val expectedNotificationsForSecondSubmission = Seq(notification_3)

        result.size must equal(2)
        result.keySet must equal(expectedKeySet)
        result(submission).length must equal(2)
        expectedNotificationsForFirstSubmission.foreach { result(submission) must contain(_) }
        result(submission_2).length must equal(1)
        expectedNotificationsForSecondSubmission.foreach { result(submission_2) must contain(_) }
      }
    }

    "provided with multiple Submissions and no matching Notifications" should {
      "return Map with empty lists as values" in {
        val submissions = Seq(submission, submission_2)
        val notifications = Seq(
          notification.copy(actionId = "actionId1", mrn = "mrn1"),
          notification_2.copy(actionId = "actionId2", mrn = "mrn2"),
          notification_3.copy(actionId = "actionId3", mrn = "mrn3")
        )

        val result =
          Map(SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications): _*)

        val expectedKeySet = Set(submission, submission_2)
        result.size must equal(2)
        result.keySet must equal(expectedKeySet)
        result(submission) must equal(Seq.empty)
        result(submission_2) must equal(Seq.empty)
      }
    }

    "provided with single Submission and 3 matching Notifications" should {
      "return Map with Notifications sorted in descending order" in {
        val submissions = Seq(submission)
        val newNotification_3 =
          notification_3.copy(actionId = notification.actionId, mrn = notification.mrn)
        val notifications = Seq(notification, newNotification_3, notification_2)

        val result =
          Map(SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, notifications): _*)

        val expectedKeySet = Set(submission)
        val expectedNotificationsOrder = Seq(newNotification_3, notification_2, notification)
        result.size must equal(1)
        result.keySet must equal(expectedKeySet)
        result(submission).length must equal(3)
        result(submission) must equal(expectedNotificationsOrder)
      }
    }

    "provide with correct order" in {
      val submissions = Seq(submission, submission_2, submission_3)

      val result = SubmissionDisplayHelper.createSubmissionsWithSortedNotificationsMap(submissions, Seq.empty)

      result.map(_._1) must contain inOrder (submission_2, submission, submission_3)
    }

    "filter submissions" should {
      "return submissions that match the notification test" in {
        val submissions = Seq(submission -> Seq(notification), submission_2 -> Seq(notification_2), submission_3 -> Seq(notification_3))

        val result1 = SubmissionDisplayHelper.filterSubmissions(submissions, notifications => notifications.contains(notification))
        result1 must equal(Seq(submission -> Seq(notification)))

        val result2 = SubmissionDisplayHelper.filterSubmissions(
          submissions,
          notifications => notifications.headOption.map(_.status).exists(SubmissionStatus.rejectedStatuses.contains)
        )
        result2 must equal(Seq(submission_2 -> Seq(notification_2)))
      }

      "return submissions that have missing notifications" in {
        val submissions = Seq(submission -> Seq(notification), submission_2 -> Seq(notification_2), submission_3 -> Seq.empty)

        val results = SubmissionDisplayHelper.filterSubmissions(
          submissions,
          notifications => notifications.isEmpty || notifications.headOption.map(_.status).exists(SubmissionStatus.otherStatuses.contains)
        )

        results must equal(Seq(submission -> Seq(notification), submission_3 -> Seq.empty))
      }
    }
  }

}

object SubmissionDisplayHelperSpec {

  val uuid: String = UUID.randomUUID().toString
  val uuid_2: String = UUID.randomUUID().toString
  val uuid_3: String = UUID.randomUUID().toString
  val eori: String = "GB167676"
  val ducr: String = createRandomAlphanumericString(16)
  val lrn: String = createRandomAlphanumericString(22)
  val mrn: String = "MRN87878797"
  val mrn_2: String = "MRN12341234"
  val mrn_3: String = "MRN12341235"
  val conversationId: String = "b1c09f1b-7c94-4e90-b754-7c5c71c44e11"
  val conversationId_2: String = "b1c09f1b-7c94-4e90-b754-7c5c71c55e22"
  val conversationId_3: String = "b1c09f1b-7c94-4e90-b754-7c5c71c55e23"

  lazy val action = Action(requestType = SubmissionRequest, id = conversationId)
  lazy val action_2 = Action(requestType = SubmissionRequest, id = conversationId_2, requestTimestamp = action.requestTimestamp.plus(2, DAYS))
  lazy val action_3 = Action(requestType = SubmissionRequest, id = conversationId_2, requestTimestamp = action.requestTimestamp.minus(2, DAYS))
  lazy val actionCancellation =
    Action(requestType = CancellationRequest, id = conversationId, requestTimestamp = action.requestTimestamp.plus(3, HOURS))

  lazy val submission: Submission =
    Submission(uuid = uuid, eori = eori, lrn = lrn, mrn = Some(mrn), ducr = Some(ducr), actions = Seq(action))
  lazy val submission_2: Submission =
    Submission(uuid = uuid_2, eori = eori, lrn = lrn, mrn = Some(mrn_2), ducr = Some(ducr), actions = Seq(action_2))
  lazy val submission_3: Submission =
    Submission(uuid = uuid_3, eori = eori, lrn = lrn, mrn = Some(mrn_3), ducr = Some(ducr), actions = Seq(action_3))
  lazy val cancelledSubmission: Submission =
    Submission(uuid = uuid, eori = eori, lrn = lrn, mrn = Some(mrn), ducr = Some(ducr), actions = Seq(action, actionCancellation))

  private lazy val functionCodes: Seq[String] =
    Seq("01", "02", "03", "05", "06", "07", "08", "09", "10", "11", "16", "17", "18")
  private lazy val functionCodesRandomised: Iterator[String] = Random.shuffle(functionCodes).toIterator

  private def randomResponseFunctionCode: String = functionCodesRandomised.next()

  val dateTimeIssued: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
  val dateTimeIssued_2: ZonedDateTime = dateTimeIssued.plus(3, MINUTES)
  val dateTimeIssued_3: ZonedDateTime = dateTimeIssued_2.plus(3, MINUTES)
  val functionCode: String = randomResponseFunctionCode
  val functionCode_2: String = randomResponseFunctionCode
  val functionCode_3: String = randomResponseFunctionCode
  val nameCode: Option[String] = None
  val errors = Seq(NotificationError(validationCode = "CDS12056", pointer = Some(Pointer("42A.26B")), url = None))

  private val payloadExemplaryLength = 300
  val payload = createRandomAlphanumericString(payloadExemplaryLength)
  val payload_2 = createRandomAlphanumericString(payloadExemplaryLength)
  val payload_3 = createRandomAlphanumericString(payloadExemplaryLength)

  val notification = Notification(
    actionId = conversationId,
    mrn = mrn,
    dateTimeIssued = dateTimeIssued,
    status = SubmissionStatus.ACCEPTED,
    errors = errors,
    payload = payload
  )
  val notification_2 = Notification(
    actionId = conversationId,
    mrn = mrn,
    dateTimeIssued = dateTimeIssued_2,
    status = SubmissionStatus.REJECTED,
    errors = errors,
    payload = payload_2
  )
  val notification_3 = Notification(
    actionId = conversationId_2,
    mrn = mrn,
    dateTimeIssued = dateTimeIssued_3,
    status = SubmissionStatus.ACCEPTED,
    errors = Seq.empty,
    payload = payload_3
  )
}
