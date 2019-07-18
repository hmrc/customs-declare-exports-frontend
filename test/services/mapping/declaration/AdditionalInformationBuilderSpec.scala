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

class AdditionalInformationBuilderSpec extends WordSpec with Matchers {

  "AdditionalInformationBuilder" should {
    "correctly map to the WCO-DEC AdditionalInformation instance" in {
      val additionalInformation = AdditionalInformationBuilder.build("functionReferenceId")
      additionalInformation.getStatementTypeCode.getValue should be("AES")
      additionalInformation.getPointer.get(0).getSequenceNumeric.intValue() should be(1)
      additionalInformation.getPointer.get(0).getDocumentSectionCode.getValue should be("42A")
      additionalInformation.getPointer.get(1).getDocumentSectionCode.getValue should be("06A")
    }
  }
}
