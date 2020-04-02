/*
 * Copyright 2020 HM Revenue & Customs
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

package views.declaration.summary

import java.time.LocalDateTime

import base.Injector
import config.AppConfig
import models.Mode
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.draft_info_section_gds

import scala.concurrent.duration.FiniteDuration

class DraftInfoSectionViewSpec extends UnitViewSpec with ExportsTestData with MockitoSugar with Injector{

  val appConfig = mock[AppConfig]

  when(appConfig.draftTimeToLive).thenReturn(FiniteDuration(30, "day"))

  "Draft info section" should {

    "display draft title" in {

      val ducr = "DUCR"
      val localDateTime = LocalDateTime.of(2019, 11, 28, 14, 48)
      val data = aDeclaration(withConsignmentReferences(ducr = ducr), withCreatedDate(localDateTime), withUpdateDate(localDateTime))

      val expectedCreatedTime = "28 Nov 2019 at 14:48"
      val expectedUpdatedTime = "28 Dec 2019 at 14:48"

      val draftInfoPage = instanceOf[draft_info_section_gds]

      val view = draftInfoPage(data)(messages)

      val row = view.getElementsByClass("draft-ducr-row")
      row must haveSummaryKey(messages("declaration.summary.draft.ducr"))
 //     row must haveSummaryValue(ducr)

      row must haveSummaryActionsText("site.change declarationData.consignmentReferences.map(_.ducr.ducr).getOrElse(\"\")")
//
//      row must haveSummaryActionsHref()




//      view.getElementById("draft-ducr-label").text() mustBe messages("declaration.summary.draft.ducr")
//      view.getElementById("draft-ducr").text() mustBe ducr
//      view.getElementById("draft-createdDate-label").text() mustBe messages("declaration.summary.draft.createdDate")
//      view.getElementById("draft-createdDate").text() mustBe expectedCreatedTime
//      view.getElementById("draft-expireDate-label").text() mustBe messages("declaration.summary.draft.expireDate")
//      view.getElementById("draft-expireDate").text() mustBe expectedUpdatedTime
    }
  }
}
