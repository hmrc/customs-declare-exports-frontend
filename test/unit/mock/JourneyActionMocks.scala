package unit.mock

import base.MockExportsCacheService
import controllers.actions.JourneyAction
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar
import unit.tools.Stubs

import scala.concurrent.ExecutionContext

trait JourneyActionMocks extends MockExportsCacheService with BeforeAndAfterEach { self: MockitoSugar with Suite with Stubs =>

  val mockJourneyAction: JourneyAction = JourneyAction(mockExportsCacheService)(ExecutionContext.global)
}
