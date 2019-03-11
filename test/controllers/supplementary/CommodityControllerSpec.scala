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

package controllers.supplementary

import base.CustomExportsBaseSpec
import forms.supplementary.CommodityMeasure.commodityFormId
import forms.supplementary.{CommodityMeasure, PackageInformation}
import generators.Generators
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.InsufficientEnrolments

class CommodityControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks with OptionValues {

  val uri = uriWithContextPath("/declaration/supplementary/commodity-measure")

  val form = CommodityMeasure.form()

  "CommodityController" should {

    "displayForm" should {

      "return UNAUTHORIZED" when {

        "user does not have" in {
          userWithoutEori()
          withCaching[CommodityMeasure](None)

          val result = route(app, getRequest(uri)).value
          intercept[InsufficientEnrolments](status(result))

        }
      }
      "return OK" when {

        "user is signed in" in {
          authorizedUser()
          val packages = arbitraryPackagingSeq.sample
          withCaching[Seq[PackageInformation]](packages, "PackageInformation")

          withCaching[CommodityMeasure](None)

          val result = route(app, getRequest(uri)).value
          val stringResult = contentAsString(result)

          //status(result) must be(OK)
          stringResult must include(("supplementary.commodityMeasure.title"))
          stringResult must include(messages("supplementary.commodityMeasure.title"))
          stringResult must include(messages("supplementary.commodityMeasure.supplementaryUnits"))
          stringResult must include(messages("supplementary.commodityMeasure.supplementaryUnits.hint"))
          stringResult must include(messages("supplementary.commodityMeasure.netMass"))
          stringResult must include(messages("supplementary.commodityMeasure.grossMass"))
        }
      }

      "load data from cache" when {

        "packageInformation is added before" in {
          authorizedUser()
          val packages = arbitraryPackagingSeq.sample
          withCaching[List[PackageInformation]](packages, "PackageInformation")
          val cachedData = arbitraryCommodityMeasure.arbitrary.sample
          withCaching[CommodityMeasure](cachedData, commodityFormId)

          val result = route(app, getRequest(uri)).value
          status(result) must be(BAD_REQUEST)

          val stringResult = contentAsString(result)

          cachedData.map(res => stringResult.contains("You should add one package information to proceed"))

        }
      }
      "show global error" when {

        "when no packages added and user tries to navigate to the screen" in {
          authorizedUser()
          withCaching[Seq[PackageInformation]](None, "PackageInformation")
          withCaching[CommodityMeasure](None, commodityFormId)
          val result = route(app, getRequest(uri)).value
          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include("You must add package information to proceed")

        }
      }
    }

    ".onSubmit" should {

      "return UNAUTHORIZED" when {

        "user does not have an eori" in {
          userWithoutEori()
          val body = Seq(("typesOfPackages", "A1"))
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          intercept[InsufficientEnrolments](status(result))
        }
      }

      "return BAD_REQUEST" when {

        "invalid data is submitted" in {
          authorizedUser()
          withCaching[CommodityMeasure](None, commodityFormId)

          val body = Seq(("supplementaryUnits", "abcd"), ("netMass", ""), ("grossMass", ""))

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          status(result) must be(BAD_REQUEST)
          val content = contentAsString(result)
          content must include(messages("supplementary.commodityMeasure.netMass.empty"))
          content must include(messages("supplementary.commodityMeasure.grossMass.empty"))
          content must include(messages("supplementary.commodityMeasure.supplementaryUnits.error"))

        }
      }

      "add CommodityMeasure to the cache" when {

        "with valid data and on click of  add" in {

          forAll(arbitrary[CommodityMeasure]) { commodityMeasure =>
            authorizedUser()
            withCaching[CommodityMeasure](None, commodityFormId)
            val body = Seq(
              ("supplementaryUnits", commodityMeasure.supplementaryUnits.getOrElse("")),
              ("netMass", commodityMeasure.netMass),
              ("grossMass", commodityMeasure.grossMass)
            )

            val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

            status(result) must be(SEE_OTHER)
            result.futureValue.header.headers.get("Location") must be(
              Some("/customs-declare-exports/declaration/supplementary/additional-information")
            )

            verify(mockCustomsCacheService)
              .cache[CommodityMeasure](
                any(),
                ArgumentMatchers.eq(commodityFormId),
                ArgumentMatchers.eq(commodityMeasure)
              )(any(), any(), any())
          }
        }
      }

      "navigate to additionalInformation" when {

        "on click of continue when a record has already been added" in {
          forAll(arbitrary[CommodityMeasure]) { commodityMeasure =>
            authorizedUser()
            withCaching[CommodityMeasure](Some(commodityMeasure), commodityFormId)
            val payload = Seq(
              ("supplementaryUnits", commodityMeasure.supplementaryUnits.getOrElse("")),
              ("netMass", commodityMeasure.netMass),
              ("grossMass", commodityMeasure.grossMass)
            )
            val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
            status(result) must be(SEE_OTHER)
            result.futureValue.header.headers.get("Location") must be(
              Some("/customs-declare-exports/declaration/supplementary/additional-information")
            )
          }
        }
      }
    }
  }
  private def toMap(commodityMeasure: CommodityMeasure) =
    for ((k, Some(v)) <- commodityMeasure.getClass.getDeclaredFields
           .map(_.getName)
           .zip(commodityMeasure.productIterator.to)
           .toMap)
      yield k -> v.asInstanceOf[Any].toString

}
