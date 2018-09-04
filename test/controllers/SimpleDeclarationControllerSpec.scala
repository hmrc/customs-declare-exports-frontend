/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import base.SpecBase
import play.api.libs.json.{JsBoolean, JsObject, JsString, JsValue}
import play.api.test.Helpers._

class SimpleDeclarationControllerSpec extends SpecBase{

  val uri = uriWithContextPath("/simple-declaration")

  val addressJson: JsValue = JsObject(
    Map(
      "fullName" -> JsString("Full name"),
      "building" -> JsString("Building"),
      "street" -> JsString("Street"),
      "townOrCity" -> JsString("Town or City"),
      "postcode" -> JsString("Postcode"),
      "country" -> JsString("Country")
    )
  )

  val goodsPackageJson: JsValue = JsObject(
    Map(
      "commodityCode" -> JsString("Commodity code"),
      "isDescriptionOfYourGoodsCorrect" -> JsBoolean(false),
      "isItemOnUNDGList" -> JsBoolean(false),
      "addLicenceForItem" -> JsBoolean(false),
      "noOfPackages" -> JsString("Number of packages"),
      "packageType" -> JsString("Package type"),
      "goodsInContainer" -> JsBoolean(false),
      "addAnotherPackage" -> JsBoolean(false)
    )
  )

  val jsonBody: JsValue = JsObject(
    Map(
      "ducr" -> JsString("5GB123456789000-123ABC456DEFIIIIIII"),
      "isConsolidateDucrtoWiderShipment" -> JsBoolean(false),
      "mucr" -> JsString(""),
      "isDeclarationForSomeoneElse" -> JsBoolean(false),
      "isAddressAndEORICorrect" -> JsBoolean(false),
      "haveRepresentative" -> JsBoolean(false),
      "isConsignorAddressAndEORICorrect" -> JsBoolean(false),
      "address" -> addressJson,
      "isFinalDestination" -> JsBoolean(false),
      "goodsPackage" -> goodsPackageJson,
      "doYouKnowCustomsProcedureCode" -> JsBoolean(false),
      "customsProcedure" -> JsString("Custom procedure"),
      "wasPreviousCustomsProcedure" -> JsBoolean(false),
      "additionalCustomsProcedure" -> JsString("AdditionalCustomProcedure"),
      "doYouWantAddAdditionalInformation" -> JsBoolean(false),
      "addAnotherItem" -> JsBoolean(false),
      "officeOfExit" -> JsString("Office of exit"),
      "knowConsignmentDispatchCountry" -> JsBoolean(false)
    )
  )

  val wrongJson: JsValue = JsObject(
    Map(
      "ducr" -> JsString("")
    )
  )

  "SimpleDeclarationSpec" should {
    "process only authenticated user requests " in {
      authorizedUser()
      val result = route(app, getRequest(uri))

      result.map(status(_) must be (OK))
    }

    "return 200 with a success" in {
      authorizedUser()
      val result = route(app, getRequest(uri))

      result.map(status(_) must be (OK))
    }

    "display Simple-declaration" in {
      authorizedUser()
      val result = route(app, getRequest(uri))

      result.map(contentAsString(_).contains("Do you have a representative?") must be (true))
      result.map(contentAsString(_).contains("Is consolidate DUCR to wider shipment?") must be (true))
      result.map(contentAsString(_).contains("Building and street") must be (true))
    }

    "should validate form submitted" in {
      authorizedUser()
      val result = route(app, postRequest(uri, wrongJson))
      succesfulCustomsDeclarationReponse()
      result.map(contentAsString(_) must not be ("Declaration has been submitted successfully."))
    }

    "should redirect to next page" in {
      authorizedUser()
      val result = route(app, postRequest(uri, jsonBody))
      succesfulCustomsDeclarationReponse()
      result.map(status(_) must be(OK))
      result.map(contentAsString(_) must be ("Declaration has been submitted successfully."))
    }
  }
}
