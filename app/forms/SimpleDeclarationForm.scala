package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import uk.gov.voa.play.form.ConditionalMappings._

case class SimpleDeclarationForm(
    ducr: String,
    isConsolidateDucrtoWiderShipment: Boolean,
    mucr: Option[String],
    isDeclarationForSomeoneElse: Boolean,
    isAddressAndEORICorrect: Boolean,
    haveRepresentative: Boolean,
    isConsignorAddressAndEORICorrect: Boolean,
    consigneeAddress: Address,
    isFinalDestination: Boolean,
    goodsPackage: GoodsPackage,
    doYouKnowCustomsProcedureCode: Boolean,
    customsProcedure: String,
    wasPreviousCustomsProcedure: Boolean,
    additionalCustomsProcedure: String,
    doYouWantAddAdditionalInformation: Boolean,
    addAnotherItem: Boolean,
    officeOfExit: String,
    knowConsignmentDispatchCountry: Boolean)
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
        "consigneeAddress" -> Address.addressMapping,
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

case class Address(
  fullName: Option[String],
  building: Option[String],
  street: Option[String],
  townOrCity: Option[String],
  postCode: Option[String],
  country: Option[String]
)

object Address {
  val addressMapping = mapping(
    "fullName" -> optional(text),
    "building" -> optional(text),
    "street" -> optional(text),
    "townOrCity" -> optional(text),
    "postCode" -> optional(text),
    "country" -> optional(text)
  )(Address.apply)(Address.unapply)
}

case class GoodsPackage(
  commodityCode: String,
  isDescriptionOfYourGoodsCorrect: Boolean,
  isItemOnUNDGList: Boolean,
  addLicenceForItem: Boolean,
  noOfPackages: String,
  packageType: String,
  goodsInContainer: Boolean,
  addAnotherPackage: Boolean
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
