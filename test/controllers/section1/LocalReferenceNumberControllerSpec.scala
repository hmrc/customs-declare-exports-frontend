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

package controllers.section1

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.actions.AmendmentDraftFilterSpec
import controllers.section1.routes.LinkDucrToMucrController
import forms.section1.{Lrn, LrnValidator}
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.GivenWhenThen
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section1.local_reference_number

import scala.concurrent.Future

class LocalReferenceNumberControllerSpec extends ControllerSpec with AuditedControllerSpec with AmendmentDraftFilterSpec with GivenWhenThen {

  private val lrnValidator = mock[LrnValidator]
  private val lrnPage = mock[local_reference_number]

  val controller = new LocalReferenceNumberController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    lrnValidator,
    navigator,
    stubMessagesControllerComponents(),
    lrnPage
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(false))
    when(lrnPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(lrnValidator, lrnPage)
  }

  def nextPageOnTypes: Seq[NextPageOnType] =
    allDeclarationTypesExcluding(SUPPLEMENTARY).map(NextPageOnType(_, LinkDucrToMucrController.displayPage))

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aStandardDeclaration)
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[Lrn] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Lrn]])
    verify(lrnPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "LocalReferenceNumberControllerSpec" should {

    "correctly trim and convert LRN to upper case characters" in {
      Lrn.form2Data("qslrn123123123") mustBe Lrn("QSLRN123123123")
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { req =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(req.cacheModel)

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {
          withNewCaching(aDeclaration(withType(req.declarationType), withConsignmentReferences()))

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user enters incorrect data" in {
          withNewCaching(req.cacheModel)
          val incorrectForm = Json.toJson(Some(Lrn("")))

          val result = controller.submitForm()(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }

        "LrnValidator returns false" in {
          when(lrnValidator.hasBeenSubmittedInThePast48Hours(any[Lrn])(any(), any())).thenReturn(Future.successful(true))
          withNewCaching(req.cacheModel)
          val correctForm = Json.obj("lrn" -> LRN)

          val result = controller.submitForm()(postRequest(correctForm))
          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER) and redirect to 'Link DUCR to MUCR' page" in {
        val request = journeyRequest(aDeclaration(withType(req.declarationType)))
        withNewCaching(request.cacheModel)
        val correctForm = Json.obj("lrn" -> LRN)

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe LinkDucrToMucrController.displayPage
        verifyAudit()
      }
    }
  }
}
