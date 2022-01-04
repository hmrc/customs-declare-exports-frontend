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

package views.declaration.summary

import java.time.LocalDateTime

import scala.concurrent.duration.FiniteDuration

import base.Injector
import config.AppConfig
import org.mockito.Mockito.when
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.draft_info_section

class DraftInfoSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val appConfig = mock[AppConfig]

  when(appConfig.draftTimeToLive).thenReturn(FiniteDuration(30, "day"))

  "Draft info section" should {

    "display draft title" in {

      val ducr = "DUCR"
      val localDateTime = LocalDateTime.of(2019, 11, 28, 14, 48)
      val data = aDeclaration(withConsignmentReferences(ducr = ducr), withCreatedDate(localDateTime), withUpdateDate(localDateTime))

      val expectedCreatedTime = "28 November 2019 at 2:48pm"
      val expectedUpdatedTime = "28 December 2019 at 2:48pm"

      val draftInfoPage = instanceOf[draft_info_section]
      val view = draftInfoPage(data)(messages)

      val ducrRow = view.getElementsByClass("draft-ducr-row")
      ducrRow must haveSummaryKey(messages("declaration.summary.draft.ducr"))
      ducrRow must haveSummaryValue(ducr)

      val createdRow = view.getElementsByClass("draft-createdDate-row")
      createdRow must haveSummaryKey(messages("declaration.summary.draft.createdDate"))
      createdRow must haveSummaryValue(expectedCreatedTime)

      val expireRow = view.getElementsByClass("draft-expireDate-row")
      expireRow must haveSummaryKey(messages("declaration.summary.draft.expireDate"))
      expireRow must haveSummaryValue(expectedUpdatedTime)
    }
  }
}
