/*
 * Copyright 2023 HM Revenue & Customs
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

import base.ExportsTestData.{eori, newUser}
import base.RequestBuilder
import forms.declaration._
import models.DeclarationType.{DeclarationType, STANDARD}
import models.ExportsDeclaration
import models.declaration.Container
import models.requests.JourneyRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.FakeRequestCSRFSupport._

trait ExportsTestHelper extends ExportsDeclarationBuilder with ExportsItemBuilder with RequestBuilder {

  private def declaration(`type`: DeclarationType): ExportsDeclaration = aDeclaration(
    withType(`type`),
    withConsignmentReferences(),
    withDestinationCountry(),
    withGoodsLocation(LocationOfGoods("GBAUEMAEMAEMA")),
    withWarehouseIdentification(Some(WarehouseIdentification(Some("a")))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("b")))),
    withInlandModeOfTransportCode(ModeOfTransportCode.Rail),
    withOfficeOfExit("id"),
    withContainerData(Container(1, "id", Seq.empty)),
    withTotalNumberOfItems(Some("123"), Some("123"), Some("GBP")),
    withNatureOfTransaction("nature"),
    withItem(anItem())
  )

  protected def journeyRequest(`type`: DeclarationType = STANDARD): JourneyRequest[AnyContent] =
    new JourneyRequest(buildVerifiedEmailRequest(FakeRequest("", "").withCSRFToken, newUser(eori, "12345")), declaration(`type`))

  protected def journeyRequest(declaration: ExportsDeclaration): JourneyRequest[AnyContent] =
    new JourneyRequest(buildVerifiedEmailRequest(FakeRequest("", "").withCSRFToken, newUser(eori, "12345")), declaration)

  protected def journeyRequest(declaration: ExportsDeclaration, session: (String, String)*): JourneyRequest[AnyContent] = {
    val request = FakeRequest("", "").withSession(session: _*).withCSRFToken
    new JourneyRequest(buildVerifiedEmailRequest(request, newUser(eori, "12345")), declaration)
  }

  val aStandardDeclaration: ExportsDeclaration = aDeclaration(withType(STANDARD))
}
