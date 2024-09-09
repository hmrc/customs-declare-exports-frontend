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

package controllers

import base.ControllerWithoutFormSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import services.audit.AuditService

class FileUploadControllerSpec extends ControllerWithoutFormSpec {

  val mockAuditService = mock[AuditService]
  val dummySfusLink = "dummySfusLink"
  val mrn = "mrn"

  val controller = new FileUploadController(mockSfusConfig, mockAuthAction, mcc, mockAuditService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockSfusConfig.sfusUploadLink).thenReturn(dummySfusLink)
    reset(mockAuditService)
  }

  "FileUploadController" should {

    "redirect to File Upload and call the audit service" when {

      "the startFileUpload method is called with an mrn" in {
        val result = controller.startFileUpload(mrn)(getRequest())

        redirectLocation(result).get must be(s"$dummySfusLink/$mrn")
        status(result) must be(SEE_OTHER)
        verify(mockAuditService).audit(any(), any())(any())
      }

      "the startFileUpload method is called without an mrn" in {
        val result = controller.startFileUpload()(getRequest())

        redirectLocation(result).get must be(s"$dummySfusLink/")
        status(result) must be(SEE_OTHER)
        verify(mockAuditService).audit(any(), any())(any())
      }
    }
  }
}
