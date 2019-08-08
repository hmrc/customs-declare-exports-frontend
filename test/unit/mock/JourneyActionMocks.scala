/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.mock

import base.MockExportCacheService
import controllers.actions.JourneyAction
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar
import unit.tools.Stubs

import scala.concurrent.ExecutionContext

trait JourneyActionMocks extends MockExportCacheService with BeforeAndAfterEach {
  self: MockitoSugar with Suite with Stubs =>

  val mockJourneyAction: JourneyAction = new JourneyAction(mockExportsCacheService)(ExecutionContext.global)
}
