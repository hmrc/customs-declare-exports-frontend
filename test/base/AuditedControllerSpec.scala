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

package base

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, verifyNoInteractions, when}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import services.audit.AuditService
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.Future

trait AuditedControllerSpec extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val auditService = mock[AuditService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(auditService.auditAllPagesUserInput(any(), any())(any())).thenReturn(Future.successful(Success))
  }

  def verifyAudit(): Unit = {
    verify(auditService, times(1)).auditAllPagesUserInput(any(), any())(any())
    reset(auditService)
  }

  def verifyNoAudit(): Unit = {
    verifyNoInteractions(auditService)
    reset(auditService)
  }
}
