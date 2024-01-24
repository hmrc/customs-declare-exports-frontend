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

package forms.declaration.exporter

import base.JourneyTypeTestRunner
import forms.LightFormMatchers
import forms.common.DeclarationPageBaseSpec
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.EntityDetailsSpec
import forms.declaration.EntityDetailsSpec.emptyEntityDetailsJSON
import models.ExportsDeclaration
import models.requests.JourneyRequest
import base.UnitSpec
import connectors.CodeListConnector
import models.codes.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.{JsObject, JsValue}
import play.api.test.Helpers.stubMessagesApi

import java.util.Locale
import scala.collection.immutable.ListMap

class ExporterDetailsSpec
    extends UnitSpec with LightFormMatchers with JourneyTypeTestRunner with DeclarationPageBaseSpec with MockitoSugar with BeforeAndAfterEach {

  val emptyExporterDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
  implicit val mockCodeListConnector: CodeListConnector = mock[CodeListConnector]
  implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private def form(model: Option[ExportsDeclaration])(implicit request: JourneyRequest[_]): Form[ExporterDetails] =
    ExporterDetails.form(request.declarationType, model)

  onClearance { implicit request =>
    s"Exporter Details form for ${request.declarationType}" when {

      "it is EIDR" should {
        "validate is eori and address is non empty" in {

          val cachedModel: ExportsDeclaration = aDeclaration(withEntryIntoDeclarantsRecords(YesNoAnswers.yes))

          val errors = form(Some(cachedModel)).bind(emptyExporterDetailsJSON, JsonBindMaxChars).errors
          EntityDetailsSpec.assertEmptyDetails(errors)
        }
      }

      "it is not EIDR" should {
        "allow an empty eori and empty address" in {

          val cachedModel = aDeclaration(withEntryIntoDeclarantsRecords(YesNoAnswers.no))

          val result = form(Some(cachedModel)).bind(emptyExporterDetailsJSON, JsonBindMaxChars)
          result mustBe errorless
        }
      }
    }
  }

  "ExporterDetails" when {
    testTariffContentKeys(ExporterDetails, "tariff.declaration.exporterAddress")
  }
}
