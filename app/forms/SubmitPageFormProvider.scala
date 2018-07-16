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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.{Form, Forms, Mapping}
import utils.UserAnswers

case class ConsignmentAnswers(
  reference: Option[String] = Some("Example reference"),
  ownDescription: Option[String] = Some("Example ownDescription"),
  timeOfDeclaration: Option[String] = Some("Example timeOfDeclaration"),
  yourselfOrSomeoneElse: Option[String] = Some("Example yourselfOrSomeoneElse"),
  eoriNumber: Option[String] = Some("Example EoriNumber"),
  nameAndAddress: Option[String] = Some("Example nameAndAddress"),
  representative: Option[String] = Some("Example representative")
)

case class ItemsAnswers()

case class PackageAnswers()

case class TransportAnswers()

case class DeclarationSummary(
  consignmentAnswers: ConsignmentAnswers = ConsignmentAnswers(),
  itemsAnswers: ItemsAnswers = ItemsAnswers(),
  packageAnswers: PackageAnswers = PackageAnswers(),
  transportAnswers: TransportAnswers = TransportAnswers()
)

// TODO add correct values instead of Some("") after you create a page for it
object DeclarationSummary {
  def buildFromAnswers(userAnswers: UserAnswers): DeclarationSummary = {

    val consignmentAnswers = ConsignmentAnswers(
      reference = userAnswers.consignment.flatMap(ConsignmentData.ducr(_)),
      ownDescription = userAnswers.ownDescription.flatMap(_.description),
      timeOfDeclaration = Some(""),
      yourselfOrSomeoneElse = userAnswers.declarationForYourselfOrSomeoneElse.map(_.toString()),
      eoriNumber = userAnswers.enterEORI.map(_.toString()),
      nameAndAddress = Some(""),
      representative = userAnswers.haveRepresentative.map(_.toString())
    )

    DeclarationSummary(
      consignmentAnswers,
      ItemsAnswers(),
      PackageAnswers(),
      TransportAnswers()
    )
  }
}

class SubmitPageFormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("submitPage.error.required")
        .verifying(maxLength(100, "submitPage.error.length"))
    )
}
