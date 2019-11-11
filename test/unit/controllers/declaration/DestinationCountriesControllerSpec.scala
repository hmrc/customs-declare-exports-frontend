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

import controllers.declaration.DestinationCountriesController
import controllers.util.Remove
import forms.declaration.destinationCountries.DestinationCountries
import models.{DeclarationType, Mode}
import play.api.libs.json.Json
import play.api.test.Helpers._
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.{destination_countries_standard, destination_countries_supplementary}

class DestinationCountriesControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  trait SetUp {
    val destinationCountriesSupplementaryPage = new destination_countries_supplementary(mainTemplate)
    val destinationCountriesStandardPage = new destination_countries_standard(mainTemplate)

    val controller = new DestinationCountriesController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      destinationCountriesSupplementaryPage,
      destinationCountriesStandardPage
    )(ec)

    setupErrorHandler()
    authorizedUser()
  }

  trait SupplementarySetUp extends SetUp {
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
  }

  trait StandardSetUp extends SetUp {
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
  }

  trait SimplifiedSetUp extends SetUp {
    withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))
  }

  "Destination Countries controller" should {

    "redirect to origination country controller for simplified journey" when {

      "cache is empty" in new SimplifiedSetUp {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to origination country controller for standard journey" when {

      "cache is empty" in new StandardSetUp {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to origination country controller for supplementary journey" when {

      "cache is empty" in new SupplementarySetUp {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(SEE_OTHER)
      }
    }

    "return 200 (OK) during supplementary journey" when {

      "display page method is invoked with data in cache" in new SupplementarySetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 200 (OK) during standard journey" when {

      "display page method is invoked with data in cache" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 200 (OK) during simplified journey" when {

      "display page method is invoked with data in cache" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST) during supplementary journey" when {

      "user put incorrect data" in new SupplementarySetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val incorrectForm = Json.toJson(DestinationCountries("incorrect", Seq.empty, "incorrect"))

        val result = controller.saveCountries(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during standard journey" when {

      "user provide wrong action" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val wrongAction = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), ("WrongAction", ""))

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide incorrect data during adding" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val incorrectForm =
          Seq(("countryOfDispatch", "incorrect"), ("countriesOfRouting[]", "incorrect"), ("countryOfDestination", "incorrect"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide incorrect data during saving" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val incorrectForm = Seq(
          ("countryOfDispatch", "incorrect"),
          ("countriesOfRouting[]", "incorrect"),
          ("countryOfDestination", "incorrect"),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide duplicated data during adding" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val duplicatedForm = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide duplicated data during saving" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val duplicatedForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items during adding" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq.fill(DestinationCountries.limit)("US"), "PL"))))

        val correctForm = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items during saving" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq.fill(DestinationCountries.limit)("US"), "PL"))))

        val correctForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 400 (BAD_REQUEST) during simplified journey" when {

      "user provide wrong action" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val wrongAction = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), ("WrongAction", ""))

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide incorrect data during adding" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val incorrectForm =
          Seq(("countryOfDispatch", "incorrect"), ("countriesOfRouting[]", "incorrect"), ("countryOfDestination", "incorrect"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide incorrect data during saving" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val incorrectForm = Seq(
          ("countryOfDispatch", "incorrect"),
          ("countriesOfRouting[]", "incorrect"),
          ("countryOfDestination", "incorrect"),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide duplicated data during adding" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val duplicatedForm = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user provide duplicated data during saving" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val duplicatedForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items during adding" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq.fill(DestinationCountries.limit)("US"), "PL"))))

        val correctForm = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reach maximum amount of items during saving" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq.fill(DestinationCountries.limit)("US"), "PL"))))

        val correctForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER) during standard journey" when {

      "user correctly add new item" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val correctForm = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(OK)
      }

      "user save correct data with empty cache" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val correctForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.LocationController.displayPage()
      }

      "user save correct data with item in cache and empty form" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val correctForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", ""), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.LocationController.displayPage()
      }

      "user remove existing item" in new StandardSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val removeAction = (Remove.toString, "countriesOfRouting_0")

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(removeAction))

        status(result) must be(OK)
      }
    }

    "return 303 (SEE_OTHER) during simplified journey" when {

      "user correctly add new item" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val correctForm = Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), addActionUrlEncoded())

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) must be(OK)
      }

      "user save correct data with empty cache" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val correctForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", "US"), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.LocationController.displayPage()
      }

      "user save correct data with item in cache and empty form" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val correctForm =
          Seq(("countryOfDispatch", "GB"), ("countriesOfRouting[]", ""), ("countryOfDestination", "PL"), saveAndContinueActionUrlEncoded)

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.LocationController.displayPage()
      }

      "user remove existing item" in new SimplifiedSetUp {

        withNewCaching(aDeclaration(withDestinationCountries(DestinationCountries("GB", Seq("US"), "PL"))))

        val removeAction = (Remove.toString, "countriesOfRouting_0")

        val result = controller.saveCountries(Mode.Normal)(postRequestAsFormUrlEncoded(removeAction))

        status(result) must be(OK)
      }
    }
  }
}
