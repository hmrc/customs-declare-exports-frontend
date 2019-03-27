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

import base.{CustomExportsBaseSpec, ExportsTestData}
import controllers.util.CacheIdGenerator
import forms.Choice
import forms.Choice.{AllowedChoiceValues, choiceId}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeStandardDec.AllowedAdditionalDeclarationTypes._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes._
import forms.declaration.additionaldeclarationtype.{AdditionalDeclarationType, AdditionalDeclarationTypeStandardDec, AdditionalDeclarationTypeSupplementaryDec}
import models.SignedInUser
import models.requests.{AuthenticatedRequest, JourneyRequest}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class AdditionalDeclarationTypePageControllerSpec extends CustomExportsBaseSpec {

  import AdditionalDeclarationTypePageControllerSpec._

  private val additionalDeclarationTypeUri = uriWithContextPath("/declaration/type")
  private val user: SignedInUser = ExportsTestData.newUser(eoriForCache, "")
  private val authenticatedRequest = AuthenticatedRequest(fakeRequest, user)

  trait TestStandardJourney {
    val journeyRequest = JourneyRequest(authenticatedRequest, Choice(AllowedChoiceValues.StandardDec))
    val cacheId = CacheIdGenerator.cacheId()(journeyRequest)

    withCaching[Choice](Some(Choice(AllowedChoiceValues.StandardDec)), choiceId)
    mockCacheBehaviour(cacheId, AdditionalDeclarationTypeStandardDec.formId)(None)
  }

  trait TestSupplementaryJourney {
    val journeyRequest = JourneyRequest(authenticatedRequest, Choice(AllowedChoiceValues.SupplementaryDec))
    val cacheId = CacheIdGenerator.cacheId()(journeyRequest)

    withCaching[Choice](Some(Choice(AllowedChoiceValues.SupplementaryDec)), choiceId)
    mockCacheBehaviour(cacheId, AdditionalDeclarationTypeStandardDec.formId)(None)
  }

  private def mockCacheBehaviour(cacheId: String, id: String)(dataToReturn: Option[AdditionalDeclarationType]) = {
    when(
      mockCustomsCacheService
        .fetchAndGetEntry[AdditionalDeclarationType](ArgumentMatchers.eq(cacheId), ArgumentMatchers.eq(id))(
          any(),
          any(),
          any()
        )
    ).thenReturn(Future.successful(dataToReturn))

    when(
      mockCustomsCacheService.cache[AdditionalDeclarationType](
        ArgumentMatchers.eq(cacheId),
        ArgumentMatchers.eq(id),
        any()
      )(any(), any(), any())
    ).thenReturn(Future.successful(CacheMap(cacheId, Map.empty)))
  }

  before {
    authorizedUser()
  }

  after {
    reset(mockCustomsCacheService)
  }

  "Additional Declaration Type Controller on GET" should {

    "return 200 code" when {

      "used for Standard Declaration" in new TestStandardJourney {

        val result = route(app, getRequest(additionalDeclarationTypeUri)).get
        status(result) must be(OK)
      }

      "used for Supplementary Declaration" in new TestSupplementaryJourney {

        val result = route(app, getRequest(additionalDeclarationTypeUri)).get
        status(result) must be(OK)
      }
    }

    "populate the form fields with data from cache" when {

      "used for Standard Declaration" in new TestStandardJourney {

        mockCacheBehaviour(cacheId, AdditionalDeclarationTypeStandardDec.formId)(
          Some(AdditionalDeclarationType(PreLodged))
        )

        val result = route(app, getRequest(additionalDeclarationTypeUri)).get
        contentAsString(result) must include("checked=\"checked\"")
      }

      "used for Supplementary Declaration" in new TestSupplementaryJourney {

        mockCacheBehaviour(cacheId, AdditionalDeclarationTypeStandardDec.formId)(
          Some(AdditionalDeclarationType(Simplified))
        )

        val result = route(app, getRequest(additionalDeclarationTypeUri)).get
        contentAsString(result) must include("checked=\"checked\"")
      }

    }
  }

  "Additional Declaration Type Controller on POST" should {

    "save the data to the cache" when {

      "used for Standard Declaration" in new TestStandardJourney {

        val validForm = buildAdditionalDeclarationTypeTestData(PreLodged)
        route(app, postRequest(additionalDeclarationTypeUri, validForm)).get.futureValue

        verify(mockCustomsCacheService).cache[AdditionalDeclarationType](
          ArgumentMatchers.eq(cacheId),
          ArgumentMatchers.eq(AdditionalDeclarationTypeSupplementaryDec.formId),
          any()
        )(any(), any(), any())
      }

      "used for Supplementary Declaration" in new TestSupplementaryJourney {

        val validForm = buildAdditionalDeclarationTypeTestData(Simplified)
        route(app, postRequest(additionalDeclarationTypeUri, validForm)).get.futureValue

        verify(mockCustomsCacheService).cache[AdditionalDeclarationType](
          ArgumentMatchers.eq(cacheId),
          ArgumentMatchers.eq(AdditionalDeclarationTypeSupplementaryDec.formId),
          any()
        )(any(), any(), any())
      }
    }

    "return 303 code" when {

      "used for Standard Declaration" in new TestStandardJourney {

        val validForm = buildAdditionalDeclarationTypeTestData(PreLodged)
        val result = route(app, postRequest(additionalDeclarationTypeUri, validForm)).get

        status(result) must be(SEE_OTHER)
      }

      "used for Supplementary Declaration" in new TestSupplementaryJourney {

        val validForm = buildAdditionalDeclarationTypeTestData(Simplified)
        val result = route(app, postRequest(additionalDeclarationTypeUri, validForm)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to 'Consignment references' page" when {

      "used for Standard Declaration" in new TestStandardJourney {

        val validForm = buildAdditionalDeclarationTypeTestData(PreLodged)
        val header = route(app, postRequest(additionalDeclarationTypeUri, validForm)).get.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignment-references"))
      }

      "used for Supplementary Declaration" in new TestSupplementaryJourney {

        val validForm = buildAdditionalDeclarationTypeTestData(Simplified)
        val header = route(app, postRequest(additionalDeclarationTypeUri, validForm)).get.futureValue.header

        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignment-references"))
      }
    }
  }

}

object AdditionalDeclarationTypePageControllerSpec {

  def buildAdditionalDeclarationTypeTestData(value: String = ""): JsValue = JsObject(
    Map("additionalDeclarationType" -> JsString(value))
  )
}
