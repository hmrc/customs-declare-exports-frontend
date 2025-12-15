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

package forms.section6

import base.UnitWithMocksSpec
import config.AppConfig
import connectors.CodeListConnector
import forms.common.DeclarationPageBaseSpec
import forms.section6.ModeOfTransportCode.Maritime
import forms.section6.TransportCountry.{prefix, transportCountry}
import models.codes.Country
import models.viewmodels.TariffContentKey
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.FormError
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers.stubMessagesApi
import views.helpers.ModeOfTransportCodeHelper.transportMode

import java.util.Locale
import scala.collection.immutable.ListMap

class TransportCountrySpec extends UnitWithMocksSpec with BeforeAndAfterEach with DeclarationPageBaseSpec {

  implicit val codeListConnector: CodeListConnector = mock[CodeListConnector]

  implicit val appConfig: AppConfig = mock[AppConfig]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(codeListConnector.getCountryCodes(any())).thenReturn(ListMap("ZA" -> Country("South Africa", "ZA")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(codeListConnector)
  }

  implicit val messages: Messages = stubMessagesApi().preferred(List(Lang(Locale.ENGLISH)))

  val form = TransportCountry.form(transportMode(Some(Maritime)))

  def formData(country: Option[String]): JsObject =
    Json.obj(transportCountry -> JsString(country.getOrElse("")))

  "TransportCountry mapping" should {

    "return form with errors" when {

      "provided with an invalid country" in {
        val errors = form.bind(formData(Some("12345")), JsonBindMaxChars).errors
        errors mustBe List(FormError(transportCountry, s"$prefix.country.error.invalid"))
      }
    }

    "return form without errors" when {

      "provided with a valid country" in {
        form.bind(formData(Some("ZA")), JsonBindMaxChars).hasErrors mustBe false
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(TariffContentKey(s"${messageKey}.common"))

  "TransportCountry" when {
    testTariffContentKeysNoSpecialisation(TransportCountry, "tariff.declaration.transportCountry")
  }
}
