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

package views

import base.Injector
import config.AppConfig
import controllers.CannotExportGoodsController
import models.CannotExportGoodsReason
import models.CannotExportGoodsReason.allCannotExportGoodsReasons
import views.declaration.spec.UnitViewSpec
import views.html.cannot_export_goods
import views.tags.ViewTest

@ViewTest
class CannotExportGoodsViewSpec extends UnitViewSpec with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val page = instanceOf[cannot_export_goods]
  private def view(reason: Option[CannotExportGoodsReason] = None) = page(reason)

  "Cannot export goods page" should {

    "display expected title" in {
      view().getElementById("title") must containMessage("cannotExportGoods.header")
    }

    for (reason <- allCannotExportGoodsReasons)
      s"have correct body text for $reason" in {
        view(Some(reason)).getElementsByClass("govuk-body").first must containMessage(CannotExportGoodsController.getMessageForBody(reason))
      }

    "display help and support header" in {
      view().getElementsByClass("govuk-heading-m").first must containMessage("cannotExportGoods.helpAndSupport.heading")
    }

    "display help and support body" in {
      view().getElementsByClass("govuk-body").get(0) must containMessage("cannotExportGoods.helpAndSupport.body")
    }

    "display link to Saved declarations page" in {
      view().getElementById("back-to-declaration") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations())
    }

    "display link to general enquiries page" in {
      view().getElementById("general-enquiries-help") must haveHref(appConfig.generalEnquiriesHelpUrl)
    }
  }
}
