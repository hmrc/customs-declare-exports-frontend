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

package services

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

import base.UnitWithMocksSpec
import config.featureFlags.TariffApiConfig
import connectors.TariffApiConnector
import forms.declaration.CommodityDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, EitherValues}
import play.api.libs.json.Json
import services.TariffApiService.{CommodityCodeNotFound, SupplementaryUnitsNotRequired, TariffApiResult}
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}

class TariffApiServiceSpec
    extends UnitWithMocksSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with ExportsItemBuilder with EitherValues with ScalaFutures {

  private val tariffApiConfig = mock[TariffApiConfig]
  private val tariffApiConnector = mock[TariffApiConnector]

  private val tariffApiService = new TariffApiService(tariffApiConfig, tariffApiConnector)(global)

  override def beforeEach(): Unit = {
    reset(tariffApiConfig, tariffApiConnector)

    when(tariffApiConfig.isCommoditiesEnabled).thenReturn(true)
    when(tariffApiConnector.getCommodity(any())).thenReturn(Future.successful(None))
  }

  private val commodityCode = "2208303000"

  private def retrieveCommodityInfoIfAny(code: Option[String] = Some(commodityCode)): Future[TariffApiResult] = {
    val commodityDetails = CommodityDetails(code, Some("Description"))
    val item = anItem(withCommodityDetails(commodityDetails))
    tariffApiService.retrieveCommodityInfoIfAny(aDeclaration(withItems(item)), item.id)
  }

  private def extractCommodityInfoIfAnyFromJson(json: String): TariffApiResult = {
    val response = Future.successful(Some(Json.parse(json)))
    when(tariffApiConnector.getCommodity(any())).thenReturn(response)
    retrieveCommodityInfoIfAny().futureValue
  }

  "TariffApiService.retrieveCommodityInfoIfAny" when {

    "the feature flag 'isCommoditiesEnabled' is disabled" should {
      "return 'None'" in {
        when(tariffApiConfig.isCommoditiesEnabled).thenReturn(false)
        retrieveCommodityInfoIfAny().futureValue.left.value mustBe CommodityCodeNotFound
      }
    }

    "the feature flag 'isCommoditiesEnabled' is enabled" should {

      "return 'None'" when {

        "the given declaration does not contain an item with a commodity code" in {
          retrieveCommodityInfoIfAny(None).futureValue.left.value mustBe CommodityCodeNotFound
        }

        "the Tariff API responds with a 404 http status code (NOT_FOUND)" in {
          retrieveCommodityInfoIfAny().futureValue.left.value mustBe CommodityCodeNotFound
        }

        "the Tariff API provides a Json payload that does not include a 'included' Json array" in {
          val json = """{ "data": { "id": "91561" } }"""
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe CommodityCodeNotFound
        }

        "the Tariff API provides a Json payload that includes 'included' but not as Json array" in {
          val json = """{ "included": { "id": "91561" } }"""
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe CommodityCodeNotFound
        }

        "the Tariff API provides a Json payload that includes an empty 'included' Json array" in {
          val json = """{ "included":[] }"""
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }

        "no 'included' Json array's element of 'measure' type also has a '/relationships/measure_type/data/id' == '109'" in {
          val json = """{ "included":[ { "type": "measure" } ] }"""
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }

        "the element of 'measure' type with a '/relationships/measure_type/data/id' == '109' does not have a '/relationships/duty_expression' object" in {
          val json = """{ "included":[ { "type": "measure", "relationships": { "measure_type": { "data": { "id": "109" } } } } ] }"""
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }

        "no element with 'id' == '/relationships/duty_expression/data/id' is found" in {
          val json = jsonWithoutAttributes("")
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }

        "an element with 'id' == '/relationships/duty_expression/data/id' is found but does not include an 'attributes' object" in {
          val json = jsonWithoutAttributes(""",{ "id": "2982610-duty_expression" }""")
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }

        "the element with 'id' and 'attributes' object is found but 'attributes' does not include a 'formatted_base' attribute" in {
          val json = jsonWithAttributes("")
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }

        "the 'attributes' object with a 'formatted_base' attribute is found but 'formatted_base' cannot be parsed" in {
          val json = jsonWithAttributes(""""formatted_base":"<abbr>l alc. 100%</abbr>"""")
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }

        "the 'attributes' object with a parseable 'formatted_base' attribute is found but the commodity's information are empty" in {
          val json = jsonWithAttributes(""""formatted_base":"<abbr title='  '>  </abbr>"""")
          extractCommodityInfoIfAnyFromJson(json).left.value mustBe SupplementaryUnitsNotRequired
        }
      }

      "return a CommodityInfo object" when {
        "given a commodity code, the Tariff API provides a Json payload that includes the commodity' information" in {
          val json = jsonWithAttributes(""""formatted_base":"<abbr title='Litre pure (100%) alcohol'>l alc. 100%</abbr>"""")
          extractCommodityInfoIfAnyFromJson(json).value mustBe CommodityInfo(commodityCode, "litre pure (100%) alcohol", "l alc. 100%")
        }
      }

      def jsonWithAttributes(formattedBase: String): String =
        s"""{
          |  "included":[
          |    {
          |      "type": "measure",
          |      "relationships": {
          |        "duty_expression": {
          |          "data": {
          |            "id": "2982610-duty_expression"
          |          }
          |        },
          |        "measure_type": {
          |          "data": {
          |            "id": "109"
          |          }
          |        }
          |      }
          |    },
          |    {
          |      "id": "2982610-duty_expression",
          |      "attributes": { $formattedBase }
          |    }
          |  ]
          }""".stripMargin

      def jsonWithoutAttributes(dutyExpression: String): String =
        s"""{
          |  "included":[
          |    {
          |      "type": "measure",
          |      "relationships": {
          |        "duty_expression": {
          |          "data": {
          |            "id": "2982610-duty_expression"
          |          }
          |        },
          |        "measure_type": {
          |          "data": {
          |            "id": "109"
          |          }
          |        }
          |      }
          |    }
          |    $dutyExpression
          |  ]
          }""".stripMargin
    }
  }
}
