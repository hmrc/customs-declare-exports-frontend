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
import forms.declaration.{BorderTransport, TransportDetails}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import services.cache.ExportsCacheModelBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

class BorderTransportMeansBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {

  "BorderTransportMeansBuilder" should {
    "correctly map to the WCO-DEC BorderTransportMeans instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(
            TransportDetails.formId -> Json.toJson(
              TransportDetails(Some("Portugal"), true, "40", Some("1234567878ui"), Some("A"))
            ),
            BorderTransport.formId -> Json.toJson(BorderTransport("3", "10", Some("123112yu78")))
          )
        )
      val borderTransportMeanst = BorderTransportMeansBuilder.build(cacheMap)
      borderTransportMeanst.getID.getValue should be("1234567878ui")
      borderTransportMeanst.getIdentificationTypeCode.getValue should be("40")
      borderTransportMeanst.getRegistrationNationalityCode.getValue should be("PT")
      borderTransportMeanst.getModeCode.getValue should be("3")
      borderTransportMeanst.getName should be(null)
      borderTransportMeanst.getTypeCode should be(null)
    }

    "build then add" when {
      "no border transport or transport details" in {
        val model = aCacheModel(withoutTransportDetails(), withoutBorderTransport())
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getBorderTransportMeans should be(null)
      }

      "transport details only" in {
        val model = aCacheModel(
          withTransportDetails(
            meansOfTransportCrossingTheBorderNationality = Some("United Kingdom"),
            meansOfTransportCrossingTheBorderType = "type",
            meansOfTransportCrossingTheBorderIDNumber = Some("id")
          ),
          withoutBorderTransport()
        )
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getBorderTransportMeans.getID.getValue should be("id")
        declaration.getBorderTransportMeans.getIdentificationTypeCode.getValue should be("type")
        declaration.getBorderTransportMeans.getRegistrationNationalityCode.getValue should be("GB")
      }

      "invalid nationality" in {
        val model = aCacheModel(
          withTransportDetails(
            meansOfTransportCrossingTheBorderNationality = Some("other"),
            meansOfTransportCrossingTheBorderType = "type",
            meansOfTransportCrossingTheBorderIDNumber = Some("id")
          )
        )
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getBorderTransportMeans.getRegistrationNationalityCode.getValue should be("")
      }

      "border transport only" in {
        val model = aCacheModel(withoutTransportDetails(), withBorderTransport(borderModeOfTransportCode = "code"))
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getBorderTransportMeans.getModeCode.getValue should be("code")
      }

      "fully populated" in {
        val model = aCacheModel(
          withTransportDetails(
            meansOfTransportCrossingTheBorderNationality = Some("United Kingdom"),
            meansOfTransportCrossingTheBorderType = "type",
            meansOfTransportCrossingTheBorderIDNumber = Some("id")
          ),
          withBorderTransport(borderModeOfTransportCode = "code")
        )
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getBorderTransportMeans.getID.getValue should be("id")
        declaration.getBorderTransportMeans.getIdentificationTypeCode.getValue should be("type")
        declaration.getBorderTransportMeans.getRegistrationNationalityCode.getValue should be("GB")
        declaration.getBorderTransportMeans.getModeCode.getValue should be("code")
      }
    }
  }

  private def builder = new BorderTransportMeansBuilder()
}
