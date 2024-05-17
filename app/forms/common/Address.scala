/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.common

import connectors.CodeListConnector
import forms.common.Address.mappingsForAmendment
import models.AmendmentRow.{forAddedValue, forRemovedValue, pointerToSelector}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.Parties
import models.declaration.Parties.partiesPrefix
import models.{AmendmentOp, ExportsDeclaration, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.Countries._
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class Address(
  fullName: String, // alphanumeric length 1 - 35
  addressLine: String, // alphanumeric length 1 - 35
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // full country name, convert to 2 upper case alphabetic characters for backend
) extends DiffTools[Address] with AmendmentOp {

  override def createDiff(original: Address, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.fullName, fullName, combinePointers(pointerString, Address.fullNamePointer, sequenceId)),
      compareStringDifference(original.addressLine, addressLine, combinePointers(pointerString, Address.addressLinePointer, sequenceId)),
      compareStringDifference(original.townOrCity, townOrCity, combinePointers(pointerString, Address.townOrCityPointer, sequenceId)),
      compareStringDifference(original.postCode, postCode, combinePointers(pointerString, Address.postCodePointer, sequenceId)),
      compareStringDifference(original.country, country, combinePointers(pointerString, Address.countryPointer, sequenceId))
    ).flatten

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String = {
    val address = s"$fullName<br/>$addressLine<br/>$townOrCity<br/>$postCode<br/>$country"
    forAddedValue(pointerToSelector(pointer, "address"), messages(mappingsForAmendment(pointer)), address)
  }

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String = {
    val address = s"$fullName<br/>$addressLine<br/>$townOrCity<br/>$postCode<br/>$country"
    forRemovedValue(pointerToSelector(pointer, "address"), messages(mappingsForAmendment(pointer)), address)
  }
}

object Address extends FieldMapping {

  implicit val format: OFormat[Address] = Json.format[Address]

  val pointer: ExportsFieldPointer = "address"

  val fullNamePointer: ExportsFieldPointer = "fullName"
  val addressLinePointer: ExportsFieldPointer = "addressLine"
  val townOrCityPointer: ExportsFieldPointer = "townOrCity"
  val postCodePointer: ExportsFieldPointer = "postCode"
  val countryPointer: ExportsFieldPointer = "country"

  private lazy val parties = s"${ExportsDeclaration.pointer}.${Parties.pointer}"

  lazy val mappingsForAmendment: Map[String, String] = Map(
    s"${parties}.carrierDetails" -> s"${partiesPrefix}.carrier.address",
    s"${parties}.carrierDetails.address" -> s"${partiesPrefix}.carrier.address",
    s"${parties}.consigneeDetails" -> s"${partiesPrefix}.consignee.address",
    s"${parties}.consigneeDetails.address" -> s"${partiesPrefix}.consignee.address",
    s"${parties}.consignorDetails" -> s"${partiesPrefix}.consignor.address",
    s"${parties}.consignorDetails.address" -> s"${partiesPrefix}.consignor.address",
    s"${parties}.declarantDetails" -> s"${partiesPrefix}.declarant.address",
    s"${parties}.declarantDetails.address" -> s"${partiesPrefix}.declarant.address",
    s"${parties}.exporterDetails" -> s"${partiesPrefix}.exporter.address",
    s"${parties}.exporterDetails.address" -> s"${partiesPrefix}.exporter.address",
    s"${parties}.representativeDetails" -> s"${partiesPrefix}.representative.address",
    s"${parties}.representativeDetails.address" -> s"${partiesPrefix}.representative.address"
  )

  val addressId = "details.address"
  val countryId = "country"

  def mapping(addressMaxLength: Int = 70)(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[Address] = {
    val lengthError = if (addressMaxLength == 35) "length35MaxChars" else "length"

    Forms.mapping(
      "fullName" -> text()
        .verifying("declaration.address.fullName.empty", nonEmpty)
        .verifying("declaration.address.fullName.error", isEmpty or isValidFieldForAddresses)
        .verifying("declaration.address.fullName.length", isEmpty or noLongerThan(35)),
      "addressLine" -> text()
        .verifying("declaration.address.addressLine.empty", nonEmpty)
        .verifying("declaration.address.addressLine.error", isEmpty or isValidAddressField)
        .verifying(s"declaration.address.addressLine.$lengthError", isEmpty or noLongerThan(addressMaxLength)),
      "townOrCity" -> text()
        .verifying("declaration.address.townOrCity.empty", nonEmpty)
        .verifying("declaration.address.townOrCity.error", isEmpty or isValidFieldForAddresses)
        .verifying("declaration.address.townOrCity.length", isEmpty or noLongerThan(35)),
      "postCode" -> text()
        .verifying("declaration.address.postCode.empty", nonEmpty)
        .verifying("declaration.address.postCode.error", isEmpty or isAlphanumericWithSpaceAndHyphen)
        .verifying("declaration.address.postCode.length", isEmpty or noLongerThan(9)),
      countryId -> text()
        .verifying("declaration.address.country.empty", nonEmpty)
        .verifying("declaration.address.country.error", input => input.isEmpty || isValidCountryCode(input))
    )(Address.apply)(Address.unapply)
  }

  def form(implicit messages: Messages, codeListConnector: CodeListConnector): Form[Address] = Form(mapping())
}
