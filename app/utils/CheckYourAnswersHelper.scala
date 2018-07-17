/*
 * Copyright 2018 HM Revenue & Customs
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

package utils

import controllers.routes
import models.CheckMode
import viewmodels.{AnswerRow, RepeaterAnswerRow, RepeaterAnswerSection}

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def nameAndAddress: Option[AnswerRow] = userAnswers.nameAndAddress map {
    x => AnswerRow("nameAndAddress.checkYourAnswersLabel", s"$x", false, routes.NameAndAddressController.onPageLoad(CheckMode).url)
  }

  def enterEORI: Option[AnswerRow] = userAnswers.enterEORI map {
    x => AnswerRow("enterEORI.checkYourAnswersLabel", s"$x", false, routes.EnterEORIController.onPageLoad(CheckMode).url)
  }

  def haveRepresentative: Option[AnswerRow] = userAnswers.haveRepresentative map {
    x => AnswerRow("haveRepresentative.checkYourAnswersLabel", s"haveRepresentative.$x", true, routes.HaveRepresentativeController.onPageLoad(CheckMode).url)
  }

  def declarationForYourselfOrSomeoneElse: Option[AnswerRow] = userAnswers.declarationForYourselfOrSomeoneElse map {
    x => AnswerRow("declarationForYourselfOrSomeoneElse.checkYourAnswersLabel", s"declarationForYourselfOrSomeoneElse.$x", true, routes.DeclarationForYourselfOrSomeoneElseController.onPageLoad(CheckMode).url)
  }

  def submitPage: Option[AnswerRow] = userAnswers.submitPage map {
    x => AnswerRow("submitPage.checkYourAnswersLabel", s"$x", false, routes.SubmitPageController.onPageLoad(CheckMode).url)
  }

  def consignment: Option[AnswerRow] = userAnswers.consignment map {
    x => AnswerRow("consignment.checkYourAnswersLabel", s"consignment.$x", true, routes.ConsignmentController.onPageLoad(CheckMode).url)
  }

  def selectRole: Option[AnswerRow] = userAnswers.selectRole map {
    x => AnswerRow("selectRole.checkYourAnswersLabel", s"selectRole.$x", true, routes.SelectRoleController.onPageLoad(CheckMode).url)
  }
}
