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

package views.helpers.summary

import connectors.CodeListConnector
import controllers.navigation.Navigator
import controllers.section2.routes._
import controllers.section3.routes.DestinationCountryController
import forms.DeclarationPage
import forms.common.Address
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.section2.EntityDetails
import models.ExportsDeclaration
import models.declaration.Parties
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import services.Countries.findByCode
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import views.html.summary.summary_card

import javax.inject.{Inject, Singleton}

@Singleton
class Card2ForParties @Inject() (summaryCard: summary_card, navigator: Navigator, authorisationHoldersHelper: AuthorisationHoldersHelper)(
  implicit codeListConnector: CodeListConnector
) extends SummaryCard {

  // Called by the Final CYA page
  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val parties = declaration.parties
    val hasData =
      parties.declarantIsExporter.isDefined ||
        parties.exporterDetails.isDefined ||
        parties.representativeDetails.isDefined ||
        parties.carrierDetails.isDefined ||
        parties.consigneeDetails.isDefined ||
        parties.declarationAdditionalActorsData.isDefined ||
        parties.declarationHoldersData.isDefined

    if (hasData) content(declaration, actionsEnabled) else HtmlFormat.empty
  }

  // Called by the Mini CYA page
  override def content(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    summaryCard(card(2), rows(declaration.parties, actionsEnabled))

  override def backLink(implicit request: JourneyRequest[_]): Call = navigator.backLink(Card2ForParties)

  override def continueTo(implicit request: JourneyRequest[_]): Call = DestinationCountryController.displayPage

  private def rows(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummarySection] = {
    val hasAdditionalActors = parties.declarationAdditionalActorsData.fold(false)(_.actors.nonEmpty)

    List(
      maybeSummarySection(
        List(declarantIsExporter(parties, actionsEnabled), isEidr(parties, actionsEnabled), personPresentingGoods(parties, actionsEnabled))
          ++ exporterDetails(parties, actionsEnabled)
          ++ List(isExs(parties, actionsEnabled), representativeOtherAgent(parties, actionsEnabled))
          ++ representativeDetails(parties, actionsEnabled)
          ++ List(representativeStatusCode(parties, actionsEnabled))
          ++ carrierDetails(parties, actionsEnabled)
          ++ consigneeDetails(parties, actionsEnabled)
          ++ consignorDetails(parties, actionsEnabled)
      ),
      AdditionalActorsHelper.maybeSummarySection(parties, actionsEnabled),
      authorisationHoldersHelper.maybeSummarySection(parties, hasAdditionalActors, actionsEnabled)
    ).flatten
  }

  private def declarantIsExporter(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    parties.declarantIsExporter.map { declarantIsExporter =>
      SummaryListRow(
        key("parties.declarantIsExporter"),
        valueKey(if (declarantIsExporter.isYes) "site.yes" else "site.no"),
        classes = "declarant-is-exporter",
        changeLink(DeclarantExporterController.displayPage, "parties.declarantIsExporter", actionsEnabled)
      )
    }

  private def isEidr(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    parties.isEntryIntoDeclarantsRecords.map { isEidr =>
      SummaryListRow(
        key("parties.eidr"),
        valueKey(if (isEidr.isYes) "site.yes" else "site.no"),
        classes = "is-entry-into-declarants-records",
        changeLink(EntryIntoDeclarantsRecordsController.displayPage, "parties.eidr", actionsEnabled)
      )
    }

  private def personPresentingGoods(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    parties.personPresentingGoodsDetails.map { personPresentingGoods =>
      SummaryListRow(
        key("parties.personPresentingGoods"),
        value(personPresentingGoods.eori.value),
        classes = "person-presenting-goods",
        changeLink(PersonPresentingGoodsDetailsController.displayPage, "parties.personPresentingGoods", actionsEnabled)
      )
    }

  private def exporterDetails(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    parties.exporterDetails.map { exporterDetails =>
      eoriAndOrAddress(exporterDetails.details, "exporter", ExporterEoriNumberController.displayPage, actionsEnabled)
    }.getOrElse(Seq.empty)

  private def isExs(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    parties.isExs.map { exs =>
      SummaryListRow(key("parties.exs"), value(exs.isExs), classes = "isExs", changeLink(IsExsController.displayPage, "parties.exs", actionsEnabled))
    }

  private def representativeOtherAgent(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    parties.representativeDetails.flatMap(_.representingOtherAgent).map { otherAgent =>
      SummaryListRow(
        key("parties.representative.agent"),
        valueKey(if (otherAgent == yes) "site.yes" else "site.no"),
        classes = "representative-other-agent",
        changeLink(RepresentativeAgentController.displayPage, "parties.representative.agent", actionsEnabled)
      )
    }

  private def representativeDetails(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    parties.representativeDetails
      .flatMap(_.details)
      .map { representativeDetails =>
        eoriAndOrAddress(representativeDetails, "representative", RepresentativeEntityController.displayPage, actionsEnabled)
      }
      .getOrElse(Seq.empty)

  private def representativeStatusCode(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    parties.representativeDetails.flatMap(_.statusCode).map { statusCode =>
      SummaryListRow(
        key("parties.representative.type"),
        valueKey(s"declaration.summary.parties.representative.type.$statusCode"),
        classes = "representative-type",
        changeLink(RepresentativeStatusController.displayPage, "parties.representative.type", actionsEnabled)
      )
    }

  private def carrierDetails(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    parties.carrierDetails.map { carrierDetails =>
      eoriAndOrAddress(carrierDetails.details, "carrier", CarrierEoriNumberController.displayPage, actionsEnabled)
    }.getOrElse(Seq.empty)

  private def consigneeDetails(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    parties.consigneeDetails.map { consigneeDetails =>
      eoriAndOrAddress(consigneeDetails.details, "consignee", ConsigneeDetailsController.displayPage, actionsEnabled)
    }.getOrElse(Seq.empty)

  private def consignorDetails(parties: Parties, actionsEnabled: Boolean)(implicit messages: Messages): Seq[Option[SummaryListRow]] =
    parties.consignorDetails.map { consignorDetails =>
      eoriAndOrAddress(consignorDetails.details, "consignor", ConsignorEoriNumberController.displayPage, actionsEnabled)
    }.getOrElse(Seq.empty)

  private def eoriAndOrAddress(details: EntityDetails, fieldId: String, call: Call, actionsEnabled: Boolean)(
    implicit messages: Messages
  ): Seq[Option[SummaryListRow]] = {

    def row(fieldValue: String, eoriOrAddress: String): SummaryListRow =
      SummaryListRow(
        key(s"parties.${fieldId}.${eoriOrAddress}"),
        valueHtml(fieldValue),
        classes = s"${fieldId}-${eoriOrAddress}",
        changeLink(call, s"parties.${fieldId}.${eoriOrAddress}", actionsEnabled)
      )

    def addressValue(address: Address): String =
      List(address.fullName, address.addressLine, address.townOrCity, address.postCode, findByCode(address.country).map(_.countryName).getOrElse(""))
        .mkString("<br>")

    if (details.eori.isEmpty && details.address.isEmpty) List(None)
    else List(details.eori.map(eori => row(eori.value, "eori")), details.address.map(address => row(addressValue(address), "address")))
  }
}

object Card2ForParties extends DeclarationPage
