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

package controllers.helpers

import base.{Injector, MockExportCacheService, UnitWithMocksSpec}
import connectors.CodeListConnector
import forms.section6.ModeOfTransportCode.{Rail, Road}
import org.mockito.Mockito.reset
import org.scalatest.OptionValues.convertOptionToValuable
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import views.helpers.CountryHelper

class AmendmentHelperSpec extends UnitWithMocksSpec with MockExportCacheService with Injector {

  private implicit val messages: Messages = instanceOf[MessagesApi].preferred(FakeRequest("GET", "/foo")).messages

  private implicit val codeListConnector: CodeListConnector = mock[CodeListConnector]
  private implicit val countryHelper: CountryHelper = mock[CountryHelper]
  private val amendmentHelper = new AmendmentHelper

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(codeListConnector, countryHelper)
  }

  "AmendmentHelper.generateAmendmentRows" should {
    "return the expected sequence of AmendmentInstance" when {

      "a Declaration has been amended" in {
        val originalDeclaration = aDeclaration(withInlandModeOfTransportCode(Rail))
        val amendedDeclaration = withInlandModeOfTransportCode(Road)(originalDeclaration)

        val result = amendmentHelper.generateAmendmentRows(originalDeclaration, amendedDeclaration)

        result.size mustBe 1
        result.head.fieldId mustBe "declaration.summary.transport.inlandModeOfTransport"
        result.head.originalValue.value mustBe messages("declaration.summary.transport.inlandModeOfTransport.Rail")
        result.head.amendedValue.value mustBe messages("declaration.summary.transport.inlandModeOfTransport.Road")
      }

      "a Declaration has NOT been amended" in {
        val originalDeclaration = aDeclaration(withInlandModeOfTransportCode(Rail))

        assert(amendmentHelper.generateAmendmentRows(originalDeclaration, originalDeclaration).isEmpty)
      }
    }
  }
}
