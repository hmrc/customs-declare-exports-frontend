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

package forms

import base.CustomExportsBaseSpec
import base.ExportsTestData._
import org.scalatest.BeforeAndAfter
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

class MovementSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val expected = InventoryLinkingMovementRequest(
    "EAL",
    Some(AgentDetails(Some("eori1"), Some("Agent location"), Some("Agent role"))),
    UcrBlock("5GB123456789000-123ABC456DEFIIIII", "D"),
    "Goods location",
    Some("2020-02-01T00:00:00"),
    None,
    Some("Shed"),
    None,
    None,
    None,
    Some(TransportDetails(Some("Transport Id"), Some("M"), Some("PL")))
  )

  "Movements form " should {
    "create MovementRequest for an input cacheMap" in {
      val result = Movement.createMovementRequest(getMovementCacheMap("id1", "EAL"), "eori1", Choice("EAL"))
      result must be(expected)
    }
  }
}
