/*
 * Copyright 2021 HM Revenue & Customs
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
import base.RequestBuilder
import forms.declaration._
import models.{DeclarationType, ExportsDeclaration}
import models.DeclarationType.DeclarationType
import models.declaration.Container
import models.requests.JourneyRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.FakeRequestCSRFSupport._

trait ExportsTestData extends ExportsDeclarationBuilder with ExportsItemBuilder with RequestBuilder {

  private def declaration(`type`: DeclarationType): ExportsDeclaration = aDeclaration(
    withType(`type`),
    withConsignmentReferences(),
    withDestinationCountries(),
    withGoodsLocation(GoodsLocationForm("GBAUEMAEMAEMA")),
    withWarehouseIdentification(Some(WarehouseIdentification(Some("a")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("b")))),
    withInlandModeOfTransportCode(Some(InlandModeOfTransportCode(Some(ModeOfTransportCode.Rail)))),
    withOfficeOfExit("id"),
    withContainerData(Container("id", Seq.empty)),
    withTotalNumberOfItems(Some("123"), Some("123")),
    withNatureOfTransaction("nature"),
    withItem(anItem())
  )

  protected def journeyRequest(`type`: DeclarationType = DeclarationType.STANDARD): JourneyRequest[AnyContent] =
    new JourneyRequest(buildVerifiedEmailRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")), declaration(`type`))

  protected def journeyRequest(declaration: ExportsDeclaration): JourneyRequest[AnyContent] =
    new JourneyRequest(buildVerifiedEmailRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")), declaration)
}
