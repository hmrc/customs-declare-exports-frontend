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

package services.cache

import base.ExportsTestData.newUser
import forms.declaration.{GoodsLocation, TransportInformationContainer}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.test.FakeRequest
import utils.FakeRequestCSRFSupport._

trait ExportsTestData extends ExportsDeclarationBuilder with ExportsItemBuilder {

  private val declaration = aDeclaration(
    withConsignmentReferences(),
    withDestinationCountries(),
    withGoodsLocation(GoodsLocation("PL", "type", "id", Some("a"), Some("b"), Some("c"), Some("d"), Some("e"))),
    withWarehouseIdentification(Some("a"), Some("b"), Some("c"), Some("d")),
    withOfficeOfExit("id", Some("office"), Some("code")),
    withContainerData(TransportInformationContainer("id")),
    withTotalNumberOfItems(Some("123"), Some("123")),
    withNatureOfTransaction("nature"),
    withItem(anItem())
  )

  protected val journeyRequest =
    new JourneyRequest(
      new AuthenticatedRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")),
      declaration
    )
}
