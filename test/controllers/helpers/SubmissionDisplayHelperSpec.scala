/*
 * Copyright 2022 HM Revenue & Customs
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

import models.declaration.notifications.Notification
import models.declaration.submissions.{Submission, SubmissionStatus}
import base.UnitSpec
import testdata.SubmissionsTestData._

class SubmissionDisplayHelperSpec extends UnitSpec {

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
