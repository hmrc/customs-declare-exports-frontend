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

package unit.controllers.declaration

import controllers.declaration.AdditionalInformationController
import controllers.util.Remove
import forms.Choice
import forms.declaration.AdditionalInformation
import models.declaration.AdditionalInformationData
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.test.Helpers._
import play.twirl.api.Html
import services.cache.ExportItem
import unit.base.ControllerSpec
import views.html.declaration.additional_information

class AdditionalInformationControllerSpec extends ControllerSpec {

  trait SetUp {

    val additionalInformationPage = new additional_information(mainTemplate)

    val controller = new AdditionalInformationController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockCustomsCacheService,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      additionalInformationPage
    )(ec)

    authorizedUser()
    withCaching(None)
    withNewCaching(aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec)))
    withJourneyType(Choice(Choice.AllowedChoiceValues.SupplementaryDec))
  }

  "Additional information controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in new SetUp {

        val result = controller.displayPage("itemId")(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalInformation = Some(AdditionalInformationData(Seq(AdditionalInformation("12345", "description"))))
        )
        val cachedData =
          aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withItem(itemCacheData))
        withNewCaching(cachedData)

        val result = controller.displayPage("itemId")(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in new SetUp {

        val wrongAction = Seq(("code", "12345"), ("description", "text"), ("WrongAction", ""))

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm = Seq(("code", "12345"), ("description", ""), addActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalInformation = Some(AdditionalInformationData(Seq(AdditionalInformation("12345", "description"))))
        )
        val cachedData =
          aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withItem(itemCacheData))
        withNewCaching(cachedData)

        val duplicatedForm = Seq(("code", "12345"), ("description", "description"), addActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalInformation =
            Some(AdditionalInformationData(Seq.fill(99)(AdditionalInformation("12345", "description"))))
        )
        val cachedData =
          aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withItem(itemCacheData))
        withNewCaching(cachedData)

        val form = Seq(("code", "12345"), ("description", "text"), addActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in new SetUp {

        val incorrectForm = Seq(("code", "12345"), ("description", ""), saveAndContinueActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user put duplicated item" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalInformation = Some(AdditionalInformationData(Seq(AdditionalInformation("12345", "description"))))
        )
        val cachedData =
          aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withItem(itemCacheData))
        withNewCaching(cachedData)

        val duplicatedForm = Seq(("code", "12345"), ("description", "description"), saveAndContinueActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalInformation =
            Some(AdditionalInformationData(Seq.fill(99)(AdditionalInformation("12345", "description"))))
        )
        val cachedData =
          aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withItem(itemCacheData))
        withNewCaching(cachedData)

        val form = Seq(("code", "12345"), ("description", "text"), saveAndContinueActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(form: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in new SetUp {

        val correctForm = Seq(("code", "12345"), ("description", "text"), addActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
      }

      "user save correct data" in new SetUp {

        val correctForm = Seq(("code", "12345"), ("description", "text"), saveAndContinueActionUrlEncoded)

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/items/itemId/add-document"))
      }

      "user save correct data without new item" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalInformation =
            Some(AdditionalInformationData(Seq.fill(99)(AdditionalInformation("12345", "description"))))
        )
        val cachedData =
          aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withItem(itemCacheData))
        withNewCaching(cachedData)

        val correctForm = saveAndContinueActionUrlEncoded

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/items/itemId/add-document"))
      }

      "user remove existing item" in new SetUp {

        val itemCacheData = ExportItem(
          "itemId",
          additionalInformation = Some(AdditionalInformationData(Seq(AdditionalInformation("12345", "description"))))
        )
        val cachedData =
          aCacheModel(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withItem(itemCacheData))
        withNewCaching(cachedData)

        val removeForm = (Remove.toString, "0")

        val result = controller.saveAdditionalInfo("itemId")(postRequestAsFormUrlEncoded(removeForm))

        status(result) must be(SEE_OTHER)
      }
    }
  }

}
