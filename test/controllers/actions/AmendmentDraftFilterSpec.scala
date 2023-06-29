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

package controllers.actions

import base.ControllerSpec
import models.DeclarationType.DeclarationType
import models.declaration.DeclarationStatus
import play.api.mvc.Call
import play.api.test.Helpers._

trait AmendmentDraftFilterSpec { self: ControllerSpec =>

  case class NextPageOnType(declarationType: DeclarationType, nextPage: Call)

  val controller: AmendmentDraftFilter
  def nextPageOnTypes: Seq[NextPageOnType]

  private val declarationStatus = withStatus(DeclarationStatus.AMENDMENT_DRAFT)

  "controller.displayPage" when {
    "declarations have AMENDMENT_DRAFT status and" when {
      for (nextPageOntype <- nextPageOnTypes)
        s"declarations have ${nextPageOntype.declarationType} type" should {
          s"redirect to ${nextPageOntype.nextPage.url}" in {
            withNewCaching(aDeclaration(withType(nextPageOntype.declarationType), declarationStatus))

            val result = controller.displayPage.apply(getRequest())
            status(result) must be(SEE_OTHER)
            redirectLocation(result) mustBe Some(nextPageOntype.nextPage.url)
          }
        }
    }
  }

  "controller.submitForm" when {
    "declarations have AMENDMENT_DRAFT status and" when {
      for (nextPageOntype <- nextPageOnTypes)
        s"declarations have ${nextPageOntype.declarationType} type" should {
          s"redirect to ${nextPageOntype.nextPage.url}" in {
            withNewCaching(aDeclaration(withType(nextPageOntype.declarationType), declarationStatus))

            val result = controller.submitForm.apply(getRequest())
            status(result) must be(SEE_OTHER)
            redirectLocation(result) mustBe Some(nextPageOntype.nextPage.url)
          }
        }
    }
  }
}
