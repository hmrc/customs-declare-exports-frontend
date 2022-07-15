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

package controllers.declaration

import base.ControllerSpec
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.CatOrDogFurDetails
import forms.declaration.CatOrDogFurDetails.{EducationalOrTaxidermyPurposes, InvalidPurpose}
import models.CannotExportGoodsReason.CatAndDogFur
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.GivenWhenThen
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.declaration.cat_or_dog_fur

import scala.concurrent.Future

class CatOrDogFurControllerSpec extends ControllerSpec with GivenWhenThen {

  private val catOrDogFurPage = mock[cat_or_dog_fur]

  private val controller =
    new CatOrDogFurController(
      mockAuthAction,
      stubMessagesControllerComponents(),
      mockExportsCacheService,
      mockJourneyAction,
      catOrDogFurPage,
      navigator
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(catOrDogFurPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(catOrDogFurPage)
  }

  def theResponseForm: Form[CatOrDogFurDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CatOrDogFurDetails]])
    verify(catOrDogFurPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, itemId)(request))
    theResponseForm
  }

  private val mode = Mode.Normal
  private val itemId = "itemId"

  "GET cat or dog fur page" should {

    "return 200" when {

      "when cache is empty" in {
        withNewCaching(aDeclaration())
        val result: Future[Result] = controller.displayPage(mode, itemId)(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "when cache has data" in {
        val details = CatOrDogFurDetails(YesNoAnswers.yes, Some(EducationalOrTaxidermyPurposes))
        val item = anItem(withCatOrDogFurDetails(details))
        withNewCaching(aDeclaration(withItem(item)))

        val result = controller.displayPage(mode, item.id)(getRequest())

        status(result) mustBe OK
        verify(catOrDogFurPage, times(1)).apply(any(), any(), any())(any(), any())
        theResponseForm.value mustBe Some(details)
      }
    }

    "return 303" when {

      onJourney(CLEARANCE) { request =>
        "user tries to access page" in {
          withNewCaching(request.cacheModel)
          val result = controller.displayPage(mode, itemId)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
        }
      }
    }
  }

  "POST cat or dog fur page" should {

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {
        withNewCaching(aDeclaration())

        val incorrectForm = Json.toJson(CatOrDogFurDetails(YesNoAnswers.yes, None))
        val result = controller.submitForm(mode, itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(catOrDogFurPage, times(1)).apply(any(), any(), any())(any(), any())
        theResponseForm.errors.size mustBe 1
      }
    }

    "return 303" when {

      onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { request =>
        "user answered they have cat or dog fur for some invalid purpose" in {
          val details = CatOrDogFurDetails(YesNoAnswers.yes, Some(InvalidPurpose))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withCatOrDogFurDetails(details), withItemId(itemId)))))

          val invalidPurpose = Json.toJson(details)
          val result = controller.submitForm(mode, itemId)(postRequest(invalidPurpose))

          status(result) mustBe SEE_OTHER
          redirectLocation(result).get must startWith(controllers.routes.CannotExportGoodsController.displayPage(CatAndDogFur).url)

          And("Answer is not saved in the cache when invalid purpose")
          theCacheModelUpdated.itemBy(itemId).flatMap(_.catOrDogFurDetails) mustBe None
        }
      }

      val validAnswers = Seq(CatOrDogFurDetails(YesNoAnswers.yes, Some(EducationalOrTaxidermyPurposes)), CatOrDogFurDetails(YesNoAnswers.no, None))

      for (answer <- validAnswers) onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { request =>
        s"user answered with no or with valid purpose: $answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withCatOrDogFurDetails(answer), withItemId(itemId)))))

          val body = Json.toJson(answer)
          val result = controller.submitForm(mode, itemId)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.UNDangerousGoodsCodeController.displayPage(mode, itemId)

          And("answer is saved in the cache when not invalid purpose")
          theCacheModelUpdated.itemBy(itemId).flatMap(_.catOrDogFurDetails) mustBe Some(answer)
        }
      }
    }
  }
}
