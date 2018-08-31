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

package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import uk.gov.voa.play.form.ConditionalMappings._

case class SimpleDeclarationForm(
    ducr: String = "",
    isConsolidateDucrToWiderShipment: Boolean = false,
    mucr: Option[String] = None,
    isDeclarationForSomeoneElse: Boolean = false,
    isAddressAndEORICorrect: Boolean = false,
    haveRepresentative: Boolean = false,
    isConsignorAddressAndEORICorrect: Boolean = false,
    consigneeAddress: SimpleAddress = SimpleAddress(),
    isFinalDestination: Boolean = false,
    goodsPackage: GoodsPackage = GoodsPackage(),
    doYouKnowCustomsProcedureCode: Boolean = false,
    customsProcedure: String = "",
    wasPreviousCustomsProcedure: Boolean = false,
    additionalCustomsProcedure: String = "",
    doYouWantAddAdditionalInformation: Boolean = false,
    addAnotherItem: Boolean = false,
    officeOfExit: String = "",
    knowConsignmentDispatchCountry: Boolean = false)
  extends DataFormats {

  def apply(): Form[SimpleDeclarationForm] =
    Form(
      mapping(
        "ducr" -> nonEmptyText.verifying(pattern(correctDucrFormat.r, error="error.ducr")),
        "isConsolidateDucrtoWiderShipment" -> boolean,
        "mucr" -> mandatoryIfTrue("isConsolidateDucrtoWiderShipment",
          nonEmptyText.verifying(pattern("""^[A-Za-z0-9 \-,.&'\/]{1,65}$""".r, error="error.ducr"))),
        "isDeclarationForSomeoneElse" -> boolean,
        "isAddressAndEORICorrect" -> boolean,
        "haveRepresentative" -> boolean,
        "isConsignorAddressAndEORICorrect" -> boolean,
        "consigneeAddress" -> SimpleAddress.addressMapping,
        "isFinalDestination" -> boolean,
        "goodsPackage" -> GoodsPackage.packageMapping,
        "doYouKnowCustomsProcedureCode" -> boolean,
        "customsProcedure" -> text,
        "wasPreviousCustomsProcedure" -> boolean,
        "additionalCustomsProcedure" -> text,
        "doYouWantAddAdditionalInformation" -> boolean,
        "addAnotherItem" -> boolean,
        "officeOfExit" -> text,
        "knowConsignmentDispatchCountry" -> boolean
      )(SimpleDeclarationForm.apply)(SimpleDeclarationForm.unapply)
    )
}

trait DataFormats {
  val correctDucrFormat = "^\\d[A-Z]{2}\\d{12}-[0-9A-Z]{1,19}$"

  val mucrFormats: Seq[String] = Seq(
    "^A:[A-Z]{3}[0-9A-Z]{0,8}$",
    "^C:[A-Z]{3}[0-9A-Z]{4}$",
    "^[A-Z]{2}\\/[A-Z]{3}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[A-Z]{4}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[0-9]{12}-[0-9A-Z]{1,}"
  )
}

case class SimpleAddress(
  fullName: Option[String] = None,
  building: Option[String] = None,
  street: Option[String] = None,
  townOrCity: Option[String] = None,
  postCode: Option[String] = None,
  country: Option[String] = None
)

object SimpleAddress {
  val addressMapping = mapping(
    "fullName" -> optional(text),
    "building" -> optional(text),
    "street" -> optional(text),
    "townOrCity" -> optional(text),
    "postCode" -> optional(text),
    "country" -> optional(text)
  )(SimpleAddress.apply)(SimpleAddress.unapply)
}

case class GoodsPackage(
  commodityCode: String = "",
  isDescriptionOfYourGoodsCorrect: Boolean = false,
  isItemOnUNDGList: Boolean = false,
  addLicenceForItem: Boolean = false,
  noOfPackages: String = "",
  packageType: String = "",
  goodsInContainer: Boolean = false,
  addAnotherPackage: Boolean = false
)

object GoodsPackage {
  val packageMapping = mapping(
    "commodityCode" -> text,
    "isDescriptionOfYourGoodsCorrect" -> boolean,
    "isItemOnUNDGList" -> boolean,
    "addLicenceForItem" -> boolean,
    "noOfPackages" -> text,
    "packageType" -> text,
    "goodsInContainer" -> boolean,
    "addAnotherPackage" -> boolean
  )(GoodsPackage.apply)(GoodsPackage.unapply)
}
