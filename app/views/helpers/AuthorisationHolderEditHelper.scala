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

package views.helpers

import config.AppConfig
import forms.common.YesNoAnswer.{No, Yes}
import forms.section2.AuthorisationProcedureCodeChoice.{Choice1007, Choice1040, ChoiceOthers}
import forms.section1.AdditionalDeclarationType._
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.Parties
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukDetails, GovukInsetText}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.details.Details
import uk.gov.hmrc.govukfrontend.views.viewmodels.insettext.InsetText
import views.helpers.AuthorisationHolderEditHelper._
import views.html.components.gds.{bulletList, externalLink, numberedList, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AuthorisationHolderEditHelper @Inject() (
  bulletList: bulletList,
  govukDetails: GovukDetails,
  govukInsetText: GovukInsetText,
  externalLink: externalLink,
  numberedList: numberedList,
  paragraphBody: paragraphBody
) {

  def body(appConfig: AppConfig)(implicit messages: Messages, request: JourneyRequest[_]): Option[Html] = {
    val messageList = valuesToMatch(request.cacheModel) match {
      case (SUPPLEMENTARY, _, _, _)                               => listOfMessages("body.supplementary")
      case (SIMPLIFIED, Some(SIMPLIFIED_FRONTIER), Choice1007, _) => messagesWithLinkFor1007(appConfig, "simplified.arrived")
      case (SIMPLIFIED, _, _, _)                                  => listOfMessages("body.simplified")
      case (CLEARANCE, _, Choice1040, Yes)                        => listOfMessages("body.clearance.eidr.1040")
      case (CLEARANCE, _, ChoiceOthers, Yes)                      => listOfMessages("body.clearance.eidr.others")
      case (CLEARANCE, _, Choice1007, Yes)                        => messagesWithLinkFor1007(appConfig, "clearance.eidr")
      case _                                                      => List.empty
    }

    if (messageList.isEmpty) None
    else bodyText(messageList, bodyId)
  }

  def additionalBodyForArrivedDeclarationsOnly(implicit messages: Messages, request: JourneyRequest[_]): Html =
    if (isArrived(request.cacheModel.additionalDeclarationType))
      new Html(List(expandersForArrivedDeclarations))
    else HtmlFormat.empty

  def hintForAuthorisationCode(implicit messages: Messages, request: JourneyRequest[_]): List[String] =
    valuesToMatch(request.cacheModel) match {
      case (STANDARD, Some(STANDARD_PRE_LODGED), Choice1007, _)   => listOfMessages("authCode.hint.standard.prelodged.1007")
      case (STANDARD, Some(STANDARD_PRE_LODGED), ChoiceOthers, _) => listOfMessages("authCode.hint.standard.prelodged.others")
      case (STANDARD, Some(STANDARD_PRE_LODGED), Choice1040, _)   => listOfMessages("authCode.hint.standard.1040")
      case (CLEARANCE, Some(CLEARANCE_PRE_LODGED), _, No)         => listOfMessages("authCode.hint.clearance")
      case _                                                      => List.empty
    }

  def insetTextBelowAuthorisationCode(appConfig: AppConfig)(implicit messages: Messages, request: JourneyRequest[_]): Option[Html] =
    valuesToMatch(request.cacheModel) match {
      case (STANDARD, _, Choice1007, _)                             => insetTextForExciseRemovals(appConfig)
      case (STANDARD | SIMPLIFIED, _, ChoiceOthers, _)              => insetTextForNonStandardProcedures(appConfig)
      case (SIMPLIFIED, Some(SIMPLIFIED_PRE_LODGED), Choice1007, _) => insetTextForExciseRemovals(appConfig)
      case (CLEARANCE, _, ChoiceOthers, Yes)                        => insetTextForNonStandardProcedures(appConfig)
      case _                                                        => None
    }

  def textForEoriRadiosWhenEXRR(implicit messages: Messages, request: JourneyRequest[_]): Html = {
    val model = request.cacheModel
    model.additionalDeclarationType match {
      case Some(STANDARD_FRONTIER) | Some(SIMPLIFIED_FRONTIER) | Some(OCCASIONAL_FRONTIER) | Some(CLEARANCE_FRONTIER) =>
        paragraphForEoriRadiosWhenEXRR(model.parties)

      case _ => HtmlFormat.empty
    }
  }

  private val prefix = "declaration.authorisationHolder"

  private def bodyText(messageList: List[String], id: String): Option[Html] =
    Some(HtmlFormat.fill(messageList.map(message => paragraphBody(message, s"govuk-body", Some(id)))))

  private def expander(summary: String, content: String)(implicit messages: Messages): Html =
    govukDetails(Details(summary = Text(messages(summary)), content = HtmlContent(messages(content))))

  private def expandersForArrivedDeclarations(implicit messages: Messages): Html = {
    val key = "body.arrived.expander"
    new Html(List(expander(s"$prefix.$key.cse.title", s"$prefix.$key.cse.text"), expander(s"$prefix.$key.mib.title", s"$prefix.$key.mib.text")))
  }

  private def insetText(appendable: Html, key: String)(implicit messages: Messages): Option[Html] = {
    val html = new Html(List(paragraphBody(messages(s"$prefix.authCode.inset.$key.title"), "govuk-label--s"), appendable))
    Some(govukInsetText(InsetText(id = Some(insetTextId), content = HtmlContent(html))))
  }

  private def insetTextForExciseRemovals(appConfig: AppConfig)(implicit messages: Messages): Option[Html] = {

    val link1 = externalLink(messages(s"$prefix.authCode.inset.excise.bullet1.link"), appConfig.permanentExportOrDispatch.authHolder)
    val link2 = externalLink(messages(s"$prefix.authCode.inset.excise.bullet2.link"), appConfig.permanentExportOrDispatch.conditions)
    val link3 = externalLink(messages(s"$prefix.authCode.inset.excise.bullet3.link"), appConfig.permanentExportOrDispatch.documents)

    insetText(
      bulletList(
        List(
          Html(messages(s"$prefix.authCode.inset.excise.bullet1", link1)),
          Html(messages(s"$prefix.authCode.inset.excise.bullet2", link2)),
          Html(messages(s"$prefix.authCode.inset.excise.bullet3", link3))
        )
      ),
      "excise"
    )
  }

  private def insetTextForNonStandardProcedures(appConfig: AppConfig)(implicit messages: Messages): Option[Html] = {

    val link1 = externalLink(messages(s"$prefix.authCode.inset.special.bullet1.link"), appConfig.previousProcedureCodes)

    insetText(
      numberedList(
        List(
          Html(messages(s"$prefix.authCode.inset.special.bullet1", link1)),
          Html(messages(s"$prefix.authCode.inset.special.bullet2")),
          Html(messages(s"$prefix.authCode.inset.special.bullet3")),
          Html(messages(s"$prefix.authCode.inset.special.bullet4"))
        )
      ),
      "special"
    )
  }

  private def listOfMessages(key: String)(implicit messages: Messages): List[String] =
    List(messages(s"$prefix.$key"))

  private def messagesWithLinkFor1007(appConfig: AppConfig, key: String)(implicit messages: Messages): List[String] =
    List(messages(s"$prefix.body.$key.1007", externalLink(messages(s"$prefix.body.1007.link"), appConfig.permanentExportOrDispatch.section)))

  private val eoriKey = s"$prefix.eori"

  private def paragraphForEoriRadiosWhenEXRR(parties: Parties)(implicit messages: Messages): Html =
    parties.declarantIsExporter.fold {
      paragraph(s"$eoriKey.body.exrr.v2", exrrHelpTextId)
    } { declarantIsExporter =>
      if (declarantIsExporter.isYes) paragraph(s"$eoriKey.body.exrr.v1", exrrHelpTextId)
      else {
        val version = if (parties.exporterDetails.flatMap(_.details.eori).isDefined) "v2" else "v3"
        paragraph(s"$eoriKey.body.exrr.$version", exrrHelpTextId)
      }
    }

  private def paragraph(key: String, id: String)(implicit messages: Messages): Html =
    paragraphBody(message = messages(key), id = Some(id))
}

object AuthorisationHolderEditHelper {

  def valuesToMatch(model: ExportsDeclaration) =
    (model.`type`, model.additionalDeclarationType, model.parties.authorisationProcedureCodeChoice, model.parties.isEntryIntoDeclarantsRecords)

  val bodyId = "text-under-h1"
  val insetTextId = "inset-text"
  val exrrHelpTextId = "EXRR-help"
}
