/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.declaration.countries

import base.JourneyTypeTestRunner
import connectors.CodeListConnector
import forms.common.DeclarationPageBaseSpec
import forms.declaration.countries.Countries._
import models.DeclarationType._
import models.viewmodels.TariffContentKey
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import play.api.i18n.Lang
import play.api.test.Helpers.stubMessagesApi
import models.codes.{Country => ModelCountry}
import org.mockito.Mockito.{reset, when}

import java.util.Locale
import scala.collection.immutable.ListMap

class DestinationCountriesSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner with MockitoSugar with BeforeAndAfterEach {

  implicit val mockCodeListConnector = mock[CodeListConnector]
  implicit val messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any()))
      .thenReturn(ListMap("GB" -> ModelCountry("United Kingdom", "GB"), "PL" -> ModelCountry("Poland", "PL")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  "Destination Countries" should {
    "contains object to represent every page and contain correct Id" in {
      DestinationCountryPage.id mustBe "destinationCountry"
      RoutingCountryPage.id mustBe "routingCountry"
    }
  }

  onEveryDeclarationJourney() { implicit request =>
    "Destination Countries" should {
      s"validate form with incorrect value for ${request.declarationType}" in {
        val result = Countries.form(DestinationCountryPage).fillAndValidate(Country(Some("incorrect")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.destinationCountry.error"))
      }

      s"validate form with invalid selection of GB for ${request.declarationType}" in {
        val result = Countries.form(DestinationCountryPage).fillAndValidate(Country(Some("GB")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.destinationCountry.error.uk"))
      }

      s"check if the country is duplicated for ${request.declarationType}" in {
        val cachedCountries = Seq(Country(Some("PL")))
        val result = Countries.form(RoutingCountryPage, cachedCountries).fillAndValidate(Country(Some("PL")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.routingCountries.duplication"))
      }

      s"validate if country limit is reached for ${request.declarationType}" in {
        val cachedCountries = Seq.fill(99)(Country(Some("PL")))
        val result = Countries.form(RoutingCountryPage, cachedCountries).fillAndValidate(Country(Some("GB")))

        result.errors mustBe Seq(FormError("countryCode", "declaration.routingCountries.limit"))
      }
    }
  }

  onJourney(OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY) { implicit request =>
    "Destination Countries" should {
      "return error" when {
        s"there is no value for ${request.declarationType}" in {
          val result = Countries.form(DestinationCountryPage).fillAndValidate(Country(Some("")))

          result.errors mustBe Seq(FormError("countryCode", "declaration.destinationCountry.empty"))
        }
      }
    }
  }

  onJourney(CLEARANCE) { implicit request =>
    "Destination Countries" should {
      "return no errors" when {
        s"field is not provided for ${request.declarationType}" in {
          val result = Countries.form(DestinationCountryPage).fillAndValidate(Country(None))

          result.errors mustBe empty
        }
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.clearance"), TariffContentKey(s"${messageKey}.2.clearance"))

  "DestinationCountryPage" when {
    testTariffContentKeys(DestinationCountryPage, "tariff.declaration.destinationCountry")
  }
}
