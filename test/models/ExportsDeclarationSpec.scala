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

package models

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import services.cache.ExportsDeclarationBuilder

class ExportsDeclarationSpec extends WordSpec with MustMatchers with ExportsDeclarationBuilder with OptionValues {

  "Amend" should {
    val currentTime = Instant.now()
    val clock = Clock.fixed(currentTime, ZoneOffset.UTC)

    "override required fields" in {
      val declaration =
        aDeclaration(withStatus(DeclarationStatus.COMPLETE), withCreatedDate(LocalDate.of(2019, 1, 1)), withUpdateDate(LocalDate.of(2019, 1, 1)))
      val amendedDeclaration = declaration.amend()(clock)
      amendedDeclaration.status mustBe DeclarationStatus.DRAFT
      amendedDeclaration.createdDateTime mustBe currentTime
      amendedDeclaration.updatedDateTime mustBe currentTime
      amendedDeclaration.sourceId.value mustBe declaration.id
    }
  }

}
