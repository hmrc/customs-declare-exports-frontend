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

package views.helpers

import base.ExportsTestData.eori
import base.{Injector, UnitSpec}
import controllers.section1.routes.DeclarantDetailsController
import controllers.section2.routes.PersonPresentingGoodsDetailsController
import controllers.section4.routes.TotalPackageQuantityController
import controllers.section5.routes.AdditionalDocumentsController
import forms.common.Eori
import models.DeclarationType.CLEARANCE
import models.Pointer
import services.cache.ExportsTestHelper

class PointerHelperSpec extends UnitSpec with ExportsTestHelper with Injector {

  private val sampleItemId = "Item_Id"

  "PointerHelper.getChangeLinkCall" should {
    "return no Call link" when {
      "no pointer is specfied" in {
        PointerHelper.getChangeLinkCall(None, aDeclaration()) mustBe None
      }

      "supplied pointer maps to a header level field" which {
        "does not have a specific change link" in {
          PointerHelper.getChangeLinkCall(Some(Pointer("declaration.NON-EXISTING")), aDeclaration()) mustBe None
        }
      }
    }

    "return the correct Call link" when {
      "supplied pointer maps to a header level field" which {
        "does have a specific change link" in {
          val result = PointerHelper.getChangeLinkCall(Some(Pointer("declaration.totalPackageQuantity")), aDeclaration())

          result.isDefined mustBe true
          result.get.url mustBe TotalPackageQuantityController.displayPage.url
        }
      }

      "supplied pointer maps to a item level field" which {
        val itemLevelPointerNoSpecificLink = Pointer("declaration.items.#1.additionalDocument.#1.NON-EXISTING")
        val itemLevelPointerWithSpecificLink = Pointer("declaration.items.#1.additionalDocument.#1.documentStatus")

        "does not have a specific change link" in {
          val result = PointerHelper.getChangeLinkCall(Some(itemLevelPointerNoSpecificLink), aDeclaration())

          result.isDefined mustBe true
          result.get mustBe PointerHelper.defaultItemsCall
        }

        "does have a specific change link but declaration supplied has no such item" in {
          val result = PointerHelper.getChangeLinkCall(Some(itemLevelPointerWithSpecificLink), aDeclaration())

          result.isDefined mustBe true
          result.get mustBe PointerHelper.defaultItemsCall
        }

        "does have a specific change link and declaration supplied has that item" in {
          val result = PointerHelper.getChangeLinkCall(
            Some(itemLevelPointerWithSpecificLink),
            aDeclaration(withItems(anItem(withItemId(sampleItemId), withSequenceId(1))))
          )

          result.isDefined mustBe true
          result.get.url mustBe AdditionalDocumentsController.displayPage(sampleItemId).url
        }
      }

      "supplied pointer maps to the declarant details eori field" which {
        "also has a declaration that is of type CLEARANCE, EXS=true and personPresentingGoodsDetails is populated" in {
          val result = PointerHelper.getChangeLinkCall(
            Some(Pointer("declaration.declarantDetails.details.eori")),
            aDeclaration(withType(CLEARANCE), withIsExs(), withPersonPresentingGoods(Some(Eori(eori))))
          )

          result.isDefined mustBe true
          result.get mustBe PersonPresentingGoodsDetailsController.displayPage
        }

        "also has a declaration that is NOT of type CLEARANCE, EXS=true and personPresentingGoodsDetails is populated" in {
          val result = PointerHelper.getChangeLinkCall(Some(Pointer("declaration.declarantDetails.details.eori")), aStandardDeclaration)

          result.isDefined mustBe true
          result.get mustBe DeclarantDetailsController.displayPage
        }
      }
    }
  }
}
