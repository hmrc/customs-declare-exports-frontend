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

import base.SpecBase
import org.scalatest.mockito.MockitoSugar
import controllers.routes
import identifiers._
import models._

class NavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new Navigator()

  val userAnswers = mock[UserAnswers]

  "Navigator" when {

    "in Normal mode" must {
      "go to Index from an identifier that doesn't exist in the route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, NormalMode)(userAnswers) mustBe routes.IndexController.onPageLoad()
      }

      "go to Own description page after consignment has been submitted" in {
        navigator.nextPage(ConsignmentId, NormalMode)(userAnswers) mustBe
          routes.OwnDescriptionController.onPageLoad(NormalMode)
      }

      "go to Declaration question page after own description has been submitted" in {
        navigator.nextPage(OwnDescriptionId, NormalMode)(userAnswers) mustBe
          routes.WhoseDeclarationController.onPageLoad(NormalMode)
      }

      "go to Have representative page after declaration has been submitted" in {
        navigator.nextPage(WhoseDeclarationId, NormalMode)(userAnswers) mustBe
          routes.HaveRepresentativeController.onPageLoad(NormalMode)
      }

      "go to Enter EORI page after have representative has been submitted" in {
        navigator.nextPage(HaveRepresentativeId, NormalMode)(userAnswers) mustBe
          routes.EnterEORIController.onPageLoad(NormalMode)
      }

      "go to name and address page after EORI has been submitted" in {
        navigator.nextPage(EnterEORIId, NormalMode)(userAnswers) mustBe
          routes.RepresentativesAddressController.onPageLoad(NormalMode)
      }

      "got to summary page after name and address has been submitted" in {
        navigator.nextPage(representativesAddressId, NormalMode)(userAnswers) mustBe
          routes.DeclarationSummaryController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" must {
      "go to CheckYourAnswers from an identifier that doesn't exist in the edit route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, CheckMode)(userAnswers) mustBe
          routes.DeclarationSummaryController.onPageLoad(CheckMode)
      }
    }
  }
}
