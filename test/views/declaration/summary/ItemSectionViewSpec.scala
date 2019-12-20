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

package views.declaration.summary

import models.DeclarationType._
import forms.declaration._
import forms.declaration.additionaldocuments.DocumentsProduced
import models.Mode
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.item_section

class ItemSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val item = anItem(
    withItemId("itemId"),
    withSequenceId(1),
    withProcedureCodes(Some("1234"), Seq("000", "111")),
    withFiscalInformation(FiscalInformation("Yes")),
    withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "1234")))),
    withStatisticalValue("123"),
    withCommodityDetails(CommodityDetails(Some("231"), "description")),
    withUNDangerousGoodsCode(UNDangerousGoodsCode(Some("345"))),
    withCUSCode(CusCode(Some("321"))),
    withTaricCodes(TaricCode("999"), TaricCode("888")),
    withNactCodes(NactCode("111"), NactCode("222")),
    withPackageInformation("PB", 10, "marks"),
    withCommodityMeasure(CommodityMeasure(Some("12"), "555", "666")),
    withAdditionalInformation("1234", "additionalDescription"),
    withDocumentsProduced(DocumentsProduced(Some("C501"), Some("GBAEOC1342"), None, None, None, None, None))
  )

  "Item section" should {

    onEveryDeclarationJourney { request =>
      val view = item_section(item)(messages, request)

      "has item header" in {

        view.getElementById("item-1-header").text() mustBe messages("declaration.summary.items.item.sequenceId")
      }

      "has procedure codes separated by space with change buttons" in {

        view.getElementById("item-1-procedureCode-label").text() mustBe messages("declaration.summary.items.item.procedureCode")
        view.getElementById("item-1-procedureCode").text() mustBe "1234 000 111"

        val List(change, accessibleChange) = view.getElementById("item-1-procedureCode-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.procedureCode.change", 1)

        view.getElementById("item-1-procedureCode-change") must haveHref(
          controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, item.id)
        )
      }

      "has commodity code with change button" in {

        view.getElementById("item-1-commodityCode-label").text() mustBe messages("declaration.summary.items.item.commodityCode")
        view.getElementById("item-1-commodityCode").text() mustBe "231"

        val List(change, accessibleChange) = view.getElementById("item-1-commodityCode-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.commodityCode.change", 1)

        view.getElementById("item-1-commodityCode-change") must haveHref(
          controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, item.id)
        )
      }

      "has goods description with change button" in {

        view.getElementById("item-1-goodsDescription-label").text() mustBe messages("declaration.summary.items.item.goodsDescription")
        view.getElementById("item-1-goodsDescription").text() mustBe "description"

        val List(change, accessibleChange) = view.getElementById("item-1-goodsDescription-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.goodsDescription.change", 1)

        view.getElementById("item-1-goodsDescription-change") must haveHref(
          controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, item.id)
        )
      }

      "has cus code with change button" in {

        view.getElementById("item-1-cusCode-label").text() mustBe messages("declaration.summary.items.item.cusCode")
        view.getElementById("item-1-cusCode").text() mustBe "321"

        val List(change, accessibleChange) = view.getElementById("item-1-cusCode-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.cusCode.change", 1)

        view.getElementById("item-1-cusCode-change") must haveHref(controllers.declaration.routes.CusCodeController.displayPage(Mode.Normal, item.id))
      }

      "has nact codes separated by comma with change button" in {

        view.getElementById("item-1-nationalAdditionalCodes-label").text() mustBe messages("declaration.summary.items.item.nationalAdditionalCodes")
        view.getElementById("item-1-nationalAdditionalCodes").text() mustBe "111, 222"

        val List(change, accessibleChange) = view.getElementById("item-1-nationalAdditionalCodes-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.nationalAdditionalCodes.change", 1)

        view.getElementById("item-1-nationalAdditionalCodes-change") must haveHref(
          controllers.declaration.routes.NactCodeController.displayPage(Mode.Normal, item.id)
        )
      }

      "has package information section" in {

        view.getElementById("package-information-1").text() mustBe messages("declaration.summary.items.item.packageInformation")
      }

      "has union and national codes" in {

        view.getElementById("additional-information-1").text() mustBe messages("declaration.summary.items.item.additionalInformation")
      }

      "has supporting documents" in {

        view.getElementById("supporting-documents-1").text() mustBe messages("declaration.summary.items.item.supportingDocuments")
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL){ request =>
      val view = item_section(item)(messages, request)

      "has un dangerous goods code with change button" in {

        view.getElementById("item-1-unDangerousGoodsCode-label").text() mustBe messages("declaration.summary.items.item.unDangerousGoodsCode")
        view.getElementById("item-1-unDangerousGoodsCode").text() mustBe "345"

        val List(change, accessibleChange) = view.getElementById("item-1-unDangerousGoodsCode-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.unDangerousGoodsCode.change", 1)

        view.getElementById("item-1-unDangerousGoodsCode-change") must haveHref(
          controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, item.id)
        )
      }

      "has taric codes separated by comma with change button" in {

        view.getElementById("item-1-taricAdditionalCodes-label").text() mustBe messages("declaration.summary.items.item.taricAdditionalCodes")
        view.getElementById("item-1-taricAdditionalCodes").text() mustBe "999, 888"

        val List(change, accessibleChange) = view.getElementById("item-1-taricAdditionalCodes-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.taricAdditionalCodes.change", 1)

        view.getElementById("item-1-taricAdditionalCodes-change") must haveHref(
          controllers.declaration.routes.TaricCodeController.displayPage(Mode.Normal, item.id)
        )
      }
    }

    onClearance { request =>
      val view = item_section(item)(messages, request)
      "not has un dangerous goods code with change button" in {

        view.getElementById("item-1-unDangerousGoodsCode-label") mustBe null
        view.getElementById("item-1-unDangerousGoodsCode") mustBe null
        view.getElementById("item-1-unDangerousGoodsCode-change") mustBe null
      }

      "not has taric codes separated by comma with change button" in {

        view.getElementById("item-1-taricAdditionalCodes-label") mustBe null
        view.getElementById("item-1-taricAdditionalCodes") mustBe null
        view.getElementById("item-1-taricAdditionalCodes-change") mustBe null
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      val view = item_section(item)(messages, request)

      "has statistical item value with change button" in {

        view.getElementById("item-1-itemValue-label").text() mustBe messages("declaration.summary.items.item.itemValue")
        view.getElementById("item-1-itemValue").text() mustBe "123"

        val List(change, accessibleChange) = view.getElementById("item-1-itemValue-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.itemValue.change", 1)

        view.getElementById("item-1-itemValue-change") must haveHref(
          controllers.declaration.routes.StatisticalValueController.displayPage(Mode.Normal, item.id)
        )
      }

      "has supplementary units with change button" in {

        view.getElementById("item-1-supplementaryUnits-label").text() mustBe messages("declaration.summary.items.item.supplementaryUnits")
        view.getElementById("item-1-supplementaryUnits").text() mustBe "12"

        val List(change, accessibleChange) = view.getElementById("item-1-supplementaryUnits-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.supplementaryUnits.change", 1)

        view.getElementById("item-1-supplementaryUnits-change") must haveHref(
          controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, item.id)
        )
      }

      "has gross weight with change button" in {

        view.getElementById("item-1-grossWeight-label").text() mustBe messages("declaration.summary.items.item.grossWeight")
        view.getElementById("item-1-grossWeight").text() mustBe "666"

        val List(change, accessibleChange) = view.getElementById("item-1-grossWeight-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.grossWeight.change", 1)

        view.getElementById("item-1-grossWeight-change") must haveHref(
          controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, item.id)
        )
      }

      "has net weight with change button" in {

        view.getElementById("item-1-netWeight-label").text() mustBe messages("declaration.summary.items.item.netWeight")
        view.getElementById("item-1-netWeight").text() mustBe "555"

        val List(change, accessibleChange) = view.getElementById("item-1-netWeight-change").text().split(" ").toList

        change mustBe messages("site.change")
        accessibleChange mustBe messages("declaration.summary.items.item.netWeight.change", 1)

        view.getElementById("item-1-netWeight-change") must haveHref(
          controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, item.id)
        )
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { request =>
      val view = item_section(item)(messages, request)

      "not display statistical item value" in {

        view.getElementById("item-1-itemValue-label") mustBe null
        view.getElementById("item-1-itemValue") mustBe null
        view.getElementById("item-1-itemValue-change") mustBe null
      }

      "not display supplementary units" in {

        view.getElementById("item-1-supplementaryUnits-label") mustBe null
        view.getElementById("item-1-supplementaryUnits") mustBe null
        view.getElementById("item-1-supplementaryUnits-change") mustBe null
      }

      "not display gross weight" in {

        view.getElementById("item-1-grossWeight-label") mustBe null
        view.getElementById("item-1-grossWeight") mustBe null
        view.getElementById("item-1-grossWeight-change") mustBe null
      }

      "not display net weight" in {

        view.getElementById("item-1-netWeight-label") mustBe null
        view.getElementById("item-1-netWeight") mustBe null
        view.getElementById("item-1-netWeight-change") mustBe null
      }
    }
  }
}
