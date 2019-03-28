/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.wco.dec.{GovernmentAgencyGoodsItem, Packaging}

class ItemSummaryControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks with OptionValues {

  private val uri = uriWithContextPath("/declaration/export-items")
  private val formId = "PackageInformation"

  "Item Summary Controller" should {

    "displayForm" should {

      "return UNAUTHORIZED" when {

        "user does not have EORI" in {
          userWithoutEori()
          withCaching[Seq[GovernmentAgencyGoodsItem]](None)

          val result = route(app, getRequest(uri)).value
          intercept[InsufficientEnrolments](status(result))

        }
      }

      "return OK" when {

        "user is signed in" in {
          authorizedUser()
          withCaching[Seq[GovernmentAgencyGoodsItem]](None)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

          val result = route(app, getRequest(uri)).value
          val stringResult = contentAsString(result)

          status(result) must be(OK)
          stringResult must include(messages("supplementary.itemsAdd.title"))
          stringResult must include(messages("supplementary.itemsAdd.title.hint"))
        }
      }

      "load data from cache" when {

        "1 export item added before" in {
          authorizedUser()

          val cachedData = Seq(GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging())))
          withCaching[Seq[GovernmentAgencyGoodsItem]](Some(cachedData), formId)

          val result = route(app, getRequest(uri)).value
          status(result) must be(OK)

          val stringResult = contentAsString(result)

          stringResult.contains(s"1 Export item added")
        }

        "more than one export item added " in {
          authorizedUser()

          GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging()))
          val cachedData = Seq(GovernmentAgencyGoodsItem(sequenceNumeric = 1, packagings = Seq(Packaging())))

          withCaching[Seq[GovernmentAgencyGoodsItem]](Some(cachedData), formId)

          val result = route(app, getRequest(uri)).value
          status(result) must be(OK)

          val stringResult = contentAsString(result)

          stringResult.contains(s"${cachedData.size} Export items added")
        }
      }
    }
  }
}
