package unit.mock

import metrics.ExportsMetrics
import org.mockito.Mockito
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatest.mockito.MockitoSugar

trait ExportsMetricsMocks extends BeforeAndAfterEach { self: MockitoSugar with Suite =>
  val mockExportsMetrics: ExportsMetrics = mock[ExportsMetrics]

  override protected def afterEach(): Unit = {
    Mockito.reset(mockExportsMetrics)
    super.afterEach()
  }
}
