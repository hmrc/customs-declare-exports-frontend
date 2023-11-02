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

package views.helpers.summary

import controllers.declaration.routes.{LocationOfGoodsController, OfficeOfExitController}
import forms.declaration.LocationOfGoods.suffixForGVMS
import models.ExportsDeclaration
import models.declaration.Locations
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import javax.inject.{Inject, Singleton}

@Singleton
class Card4ForLocations @Inject() (govukSummaryList: GovukSummaryList) extends SummaryHelper {

  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val locations = declaration.locations
    val hasData = locations.goodsLocation.isDefined || locations.officeOfExit.isDefined

    if (hasData) displayCard(locations, actionsEnabled) else HtmlFormat.empty
  }

  private def displayCard(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    govukSummaryList(SummaryList(rows(locations, actionsEnabled), card("locations")))

  private def rows(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(goodsLocation(locations, actionsEnabled), additionalInformation(locations, actionsEnabled), officeOfExit(locations, actionsEnabled)).flatten

  private def goodsLocation(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.goodsLocation.map { goodsLocation =>
      SummaryListRow(
        key("locations.goodsLocationCode"),
        value(goodsLocation.value),
        classes = "goods-location-code",
        changeLink(LocationOfGoodsController.displayPage, "locations.goodsLocationCode", actionsEnabled)
      )
    }

  private def additionalInformation(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.goodsLocation.find(_.value.endsWith(suffixForGVMS)).map { _ =>
      SummaryListRow(
        key("locations.rrs01AdditionalInformation"),
        valueKey("declaration.summary.locations.rrs01AdditionalInformation.text"),
        classes = "rrs01-additional-information"
      )
    }

  private def officeOfExit(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.officeOfExit.map { officeOfExit =>
      SummaryListRow(
        key("locations.officeOfExit"),
        value(officeOfExit.officeId),
        classes = "office-of-exit",
        changeLink(OfficeOfExitController.displayPage, "locations.officeOfExit", actionsEnabled)
      )
    }
}
