package controllers.supplementary

import base.CustomExportsBaseSpec
import base.ExportsTestData._
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class ConsignorAddressControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/consignor-address")

  "Movement controller" should {
    "display consignor address form" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.consignor.add"))
      stringResult must include(messages("supplementary.consignor.add.hint"))
      stringResult must include(messages("supplementary.consignor.nameAndAddress"))
      stringResult must include(messages("supplementary.consignor.eori"))
      stringResult must include(messages("supplementary.consignor.fullName"))
      stringResult must include(messages("supplementary.consignor.address"))
      stringResult must include(messages("supplementary.consignor.townOrCity"))
      stringResult must include(messages("supplementary.consignor.postCode"))
      stringResult must include(messages("supplementary.consignor.country"))
    }

    "validate form - incorrect values" in {
      authorizedUser()

      val result = route(app, postRequest(uri, incorrectConsignorAddress)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.consignor.eori.error"))
      stringResult must include(messages("supplementary.consignor.fullName.error"))
      stringResult must include(messages("supplementary.consignor.address.error"))
      stringResult must include(messages("supplementary.consignor.townOrCity.error"))
      stringResult must include(messages("supplementary.consignor.postCode.error"))
      stringResult must include(messages("supplementary.consignor.country.error"))
    }

    "validate form - mandatory fields" in {
      authorizedUser()

      val emptyForm: JsValue = JsObject(
        Map(
          "" -> JsString("")
        )
      )

      val result = route(app, postRequest(uri, emptyForm)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.confignor.eori.mandatory"))
      stringResult must include(messages("supplementary.confignor.fullName.mandatory"))
      stringResult must include(messages("supplementary.confignor.address.mandatory"))
      stringResult must include(messages("supplementary.confignor.townOrCity.mandatory"))
      stringResult must include(messages("supplementary.confignor.postCode.mandatory"))
      stringResult must include(messages("supplementary.confignor.country.mandatory"))
    }

    "validate form - correct values" in {
      authorizedUser()

      val result = route(app, postRequest(uri, correctConsignorAddress)).get
      val header = result.futureValue.header

      status(result) mustBe(SEE_OTHER)
      header.headers.get("Location") must be(Some(""))
    }
  }
}
