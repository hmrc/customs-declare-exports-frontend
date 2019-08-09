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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.WarehouseIdentificationSpec._
import play.api.test.Helpers._

class WarehouseIdentificationControllerSpec extends CustomExportsBaseSpec {

  private val uri = uriWithContextPath("/declaration/warehouse")

  private val exampleModel = aDeclaration(withChoice(SupplementaryDec))

  override def beforeEach() {
    authorizedUser()
    withNewCaching(exampleModel)
  }

  "Warehouse Identification Controller on POST" should {

    "validate request and redirect - no answers" in {

      val result =
        route(app, postRequest(uri, emptyWarehouseIdentificationJSON, sessionId = exampleModel.sessionId)).get

      status(result) must be(SEE_OTHER)

      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/border-transport"))
      theCacheModelUpdated.locations.warehouseIdentification must be(Some(emptyWarehouseIdentification))
    }

    "validate request and redirect - correct values" in {

      val result =
        route(app, postRequest(uri, correctWarehouseIdentificationJSON, sessionId = exampleModel.sessionId)).get

      status(result) must be(SEE_OTHER)

      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/border-transport"))
      theCacheModelUpdated.locations.warehouseIdentification.get.identificationNumber must be(Some("1234567GB"))
    }
  }
}
