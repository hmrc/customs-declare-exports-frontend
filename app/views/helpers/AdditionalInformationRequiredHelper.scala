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

package views.helpers

import config.AppConfig
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.declaration.ProcedureCodesData
import play.api.mvc.Call
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import views.html.components.gds.{exportsInsetText, externalLink, paragraphBody}

import javax.inject.{Inject, Singleton}

@Singleton
class AdditionalInformationRequiredHelper @Inject() (
  appConfig: AppConfig,
  paragraphBody: paragraphBody,
  insetText: exportsInsetText,
  externalLink: externalLink
) {
  def getBodyContent(decType: DeclarationType, mayBeProcedureCode: Option[ProcedureCodesData])(implicit messages: Messages): Html =
    (decType, mayBeProcedureCode.flatMap(_.procedureCode)) match {
      case (CLEARANCE, _)                => getBodyContentForClearanceOr1040()
      case (_, Some(pc)) if pc != "1040" => getBodyContentForNot1040(pc)
      case _                             => getBodyContentForClearanceOr1040()
    }

  private def getBodyContentForClearanceOr1040()(implicit messages: Messages): Html = {
    val para1 = paragraphBody(
      messages(
        "declaration.additionalInformationRequired.clearanceOr1040.para1",
        externalLink(
          id = Some("ai_containers_link"),
          text = messages("declaration.additionalInformationRequired.clearanceOr1040.para1.link"),
          url = appConfig.guidance.aiCodesForContainers
        )
      )
    )

    val para2 = paragraphBody(
      messages(
        "declaration.additionalInformationRequired.clearanceOr1040.para2",
        externalLink(
          id = Some("ai_codes_link"),
          text = messages("declaration.additionalInformationRequired.clearanceOr1040.para2.link"),
          url = appConfig.guidance.aiCodes
        )
      )
    )

    new Html(List(para1, para2))
  }

  private def getBodyContentForNot1040(procedureCode: String)(implicit messages: Messages): Html =
    new Html(
      List(paragraphBody(messages("declaration.additionalInformationRequired.not1040.para1")), insetText(content = getInsetContent(procedureCode)))
    )

  private def getInsetContent(procedureCode: String)(implicit messages: Messages): HtmlContent = {
    val insetPara1 = paragraphBody(
      messages(
        "declaration.additionalInformationRequired.not1040.inset.para1",
        procedureCode,
        externalLink(
          id = Some("proc_codes_link"),
          text = messages("declaration.additionalInformationRequired.not1040.inset.para1.link"),
          url = appConfig.previousProcedureCodes
        )
      )
    )

    val insetPara2 = paragraphBody(
      messages(
        "declaration.additionalInformationRequired.not1040.inset.para2",
        externalLink(
          id = Some("ai_codes_link"),
          text = messages("declaration.additionalInformationRequired.not1040.inset.para2.link"),
          url = appConfig.guidance.aiCodes
        )
      )
    )

    HtmlContent(new Html(List(insetPara1, insetPara2)))
  }
}
