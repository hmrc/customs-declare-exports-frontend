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

package services.mapping.declaration
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConverters._

class DeclarationBuilderSpec extends WordSpec with Matchers {

  "DeclarationBuilder" should {
    "correctly build a cancellation request" in {

      val declaration =
        DeclarationBuilder.buildCancellationRequest("funcRefId", "decId", "statDesc", "changeReason", "eori")

      declaration.getFunctionalReferenceID.getValue should be("funcRefId")
      declaration.getID.getValue should be("decId")
      declaration.getAdditionalInformation.asScala
        .count(_.getStatementDescription.getValue.equals("statDesc")) should be(1)
      declaration.getAmendment.asScala.count(_.getChangeReasonCode.getValue.equals("changeReason")) should be(1)
      declaration.getSubmitter.getID.getValue should be("eori")

    }
  }

}
