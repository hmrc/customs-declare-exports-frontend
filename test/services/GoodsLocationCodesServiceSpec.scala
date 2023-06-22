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

package services

import base.{Injector, UnitSpec}
import models.codes.GoodsLocationCode
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi

import java.util.Locale

class GoodsLocationCodesServiceSpec extends UnitSpec with Injector {

  private val goodsLocationCodesService = instanceOf[GoodsLocationCodesService]
  private implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  "GoodsLocationCodes" should {

    "have all entries" in {
      goodsLocationCodesService.all.length mustBe 1552
      goodsLocationCodesService.all.head mustBe GoodsLocationCode("GBCUASDDOVAPF", "AP FINANCIAL LTD. Unit 17, Arrowhead Road, Ashford. TN24 0FL")
    }

    "have entries" when {
      "depCodes" in {
        goodsLocationCodesService.depCodes.length mustBe 81
        goodsLocationCodesService.depCodes.head mustBe GoodsLocationCode(
          "GBAUBA5ABDCGD",
          "Aberdeen 410 /  Grampian Continental Ltd, Birchwood Works, Kinellar, Aberdeen, AB21 0SH"
        )
      }
      "airports" in {
        goodsLocationCodesService.airportsCodes.length mustBe 51
        goodsLocationCodesService.airportsCodes.head mustBe GoodsLocationCode("GBAUDYCABZDYC", "Aberdeen Airport - Dyce")
      }
      "coa airports" in {
        goodsLocationCodesService.coaAirportsCodes.length mustBe 110
        goodsLocationCodesService.coaAirportsCodes.head mustBe GoodsLocationCode("GBCUSAWLSASAW", "Audley End Airfield – Saffron Walden")
      }
      "maritime" in {
        goodsLocationCodesService.maritimeAndWharvesCodes.length mustBe 288
        goodsLocationCodesService.maritimeAndWharvesCodes.head mustBe GoodsLocationCode("GBAUABDABDABD", "Aberdeen Docks")
      }
      "itsf" in {
        goodsLocationCodesService.itsfCodes.length mustBe 120
        goodsLocationCodesService.itsfCodes.head mustBe GoodsLocationCode(
          "GBAUABDABDABM",
          "Aberdeen - Matthews Quay / Sea Cargo Aberdeen Limited / Matthews Quay Aberdeen Harbour Aberdeen AB11 5PG"
        )
      }
      "remote itsf" in {
        goodsLocationCodesService.remoteItsfCodes.length mustBe 16
        goodsLocationCodesService.remoteItsfCodes.head mustBe GoodsLocationCode(
          "GBAUBELBFSAEX",
          "Belfast Airport / DHL Global Fowarding (UK) Limited / Unit 6, New Cargo Agents building, Belfast Intl Airport, BT29 4GB"
        )
      }
      "external itsf" in {
        goodsLocationCodesService.externalItsfCodes.length mustBe 649
        goodsLocationCodesService.externalItsfCodes.head mustBe GoodsLocationCode(
          "GBAUPOEABZEBX",
          "Aberdeen / Crane Worldwide UK Ltd / Warehouse Unit 7, Gourdieburn, Potterton, Aberdeen, AB23 8UY"
        )
      }
      "borderInspectionPosts" in {
        goodsLocationCodesService.borderInspectionPostsCodes.length mustBe 3
        goodsLocationCodesService.borderInspectionPostsCodes.head mustBe GoodsLocationCode(
          "GBAUEMAEMABIP",
          "East Midlands Airport / North West Leicestershire District Council / Border Inspection Post, Building 20, East Midlands Airport, Castle Donington, Derby, DE74 2SA"
        )
      }
      "approvedDipositories" in {
        goodsLocationCodesService.approvedDipositoriesCodes.length mustBe 55
        goodsLocationCodesService.approvedDipositoriesCodes.head mustBe GoodsLocationCode(
          "GBAUAGNLHRRRL",
          "Abingdon / Robinsons Relocation Ltd / Nuffield Way, Abingdon, Oxford. OX14 1TN"
        )
      }
      "placeNames" in {
        goodsLocationCodesService.placeNamesGBCodes.length mustBe 0
        goodsLocationCodesService.placeNamesGBCodes.headOption mustBe None
      }
      "otherLocations" in {
        goodsLocationCodesService.otherLocationCodes.length mustBe 17
        goodsLocationCodesService.otherLocationCodes.head mustBe GoodsLocationCode("GBDUNRMFXTBLC", "BBLC - Bacton")
      }
      "cse" in {
        goodsLocationCodesService.cseCodes.length mustBe 66
        goodsLocationCodesService.cseCodes.head mustBe GoodsLocationCode("GBBUABDLEACSE", "Aberdeen, 28 Guild Street, Aberdeen, AB11 6GY")
      }
      "rail" in {
        goodsLocationCodesService.railCodes.length mustBe 16
        goodsLocationCodesService.railCodes.head mustBe GoodsLocationCode("GBAUASDASDASD", "Ashford / Eurostar / Ashford International")
      }
      "acts" in {
        goodsLocationCodesService.actsCodes.length mustBe 48
        goodsLocationCodesService.actsCodes.head mustBe GoodsLocationCode(
          "GBCUASDDOVAPF",
          "AP FINANCIAL LTD. Unit 17, Arrowhead Road, Ashford. TN24 0FL"
        )
      }
      "roro" in {
        goodsLocationCodesService.roroCodes.length mustBe 24
        goodsLocationCodesService.roroCodes.head mustBe GoodsLocationCode("GBAUCYNAYRCYN", "Cairnryan")
      }
      "gvms" in {
        goodsLocationCodesService.gvmsCodes.length mustBe 29
        goodsLocationCodesService.gvmsCodes.head mustBe GoodsLocationCode("GBAUABDABDABDGVM", "Aberdeen  / Aberdeen GVMS Port")
      }
    }
  }
}
