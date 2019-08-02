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

package services.mapping.goodsshipment

import forms.declaration.DestinationCountriesSpec
import forms.declaration.destinationCountries.DestinationCountries
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class DestinationBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {

  "DestinationBuilder" should {

    "correctly map a destinationCountries to the WCO-DEC GoodsShipment.Destination instance" when {
      "countryOfDestination has been supplied" in {
        val builder = new DestinationBuilder
        val goodsShipment = new Declaration.GoodsShipment
        builder.buildThenAdd(DestinationCountriesSpec.correctDestinationCountries, goodsShipment)

        goodsShipment.getDestination.getCountryCode.getValue should be("PL")

      }

      "countryOfDestination has not been supplied" in {
        val builder = new DestinationBuilder
        val goodsShipment = new Declaration.GoodsShipment
        builder.buildThenAdd(DestinationCountries.empty(), goodsShipment)

        goodsShipment.getDestination should be(null)

      }
    }
  }
}
