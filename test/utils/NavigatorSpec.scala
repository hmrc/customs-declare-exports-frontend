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
import play.api.mvc.{Call, Result}
import uk.gov.hmrc.http.cache.client.CacheMap

class NavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new Navigator()

  val cacheMap = mock[CacheMap]

  private def convertResultToCall(result: Result): Call =
    Call("GET", result.header.headers.get("Location").getOrElse(""))

  "Navigator" when {

    "in Normal mode" must {
      "go to Index from an identifier that doesn't exist in the route map" in {
        val result = navigator.redirect(UnknownIdentifier, NormalMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.IndexController.onPageLoad()
      }

      "go to Own description page after consignment has been submitted" in {
        val result = navigator.redirect(ConsignmentId, NormalMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.OwnDescriptionController.onPageLoad()
      }

      "go to Declaration question page after own description has been submitted" in {
        val result = navigator.redirect(OwnDescriptionId, NormalMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.WhoseDeclarationController.onPageLoad()
      }

      "go to Have representative page after declaration has been submitted" in {
        val result = navigator.redirect(WhoseDeclarationId, NormalMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.HaveRepresentativeController.onPageLoad()
      }

      "go to Enter EORI page after have representative has been submitted" in {
        val result = navigator.redirect(HaveRepresentativeId, NormalMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.EnterEORIController.onPageLoad()
      }

      "go to name and address page after EORI has been submitted" in {
        val result = navigator.redirect(EnterEORIId, NormalMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.RepresentativesAddressController.onPageLoad()
      }

      "go to summary page after name and address has been submitted" in {
        val result = navigator.redirect(RepresentativesAddressId, NormalMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.DeclarationSummaryController.onPageLoad()
      }
    }

    "in Check mode" must {
      "go to CheckYourAnswers from an identifier that doesn't exist in the edit route map" in {
        val result = navigator.redirect(UnknownIdentifier, CheckMode, cacheMap)
        val call = convertResultToCall(result)

        call mustBe routes.DeclarationSummaryController.onPageLoad()
      }
    }
  }
}
