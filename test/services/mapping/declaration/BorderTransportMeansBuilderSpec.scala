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
import forms.declaration.{TransportInformation, TransportInformationSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class BorderTransportMeansBuilderSpec extends WordSpec with Matchers {

  "BorderTransportMeansBuilder" should {
    "correctly map to the WCO-DEC BorderTransportMeans instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(TransportInformation.id -> TransportInformationSpec.correctTransportInformationJSON))
      val borderTransportMeanst = BorderTransportMeansBuilder.build(cacheMap)
      borderTransportMeanst.getID.getValue should be("1234567878ui")
      borderTransportMeanst.getIdentificationTypeCode.getValue should be("40")
      borderTransportMeanst.getRegistrationNationalityCode.getValue should be("PT")
      borderTransportMeanst.getModeCode.getValue should be("3")
      borderTransportMeanst.getName should be(null)
      borderTransportMeanst.getTypeCode should be(null)
    }
  }
}
