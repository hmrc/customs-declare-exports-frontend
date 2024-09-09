/*
 * Copyright 2024 HM Revenue & Customs
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

package models.declaration

import forms.common.YesNoAnswer
import forms.section6.{TransportCountry, TransportLeavingTheBorder, TransportPayment}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.{Format, Json}
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, compareStringDifference, ExportsDeclarationDiff}

case class Transport(
  expressConsignment: Option[YesNoAnswer] = None,
  transportPayment: Option[TransportPayment] = None,
  containers: Option[Seq[Container]] = None,
  borderModeOfTransportCode: Option[TransportLeavingTheBorder] = None,
  meansOfTransportOnDepartureType: Option[String] = None,
  meansOfTransportOnDepartureIDNumber: Option[String] = None,
  transportCrossingTheBorderNationality: Option[TransportCountry] = None,
  meansOfTransportCrossingTheBorderType: Option[String] = None,
  meansOfTransportCrossingTheBorderIDNumber: Option[String] = None
) extends DiffTools[Transport] {

  def createDiff(original: Transport, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareDifference(
        original.expressConsignment,
        expressConsignment,
        combinePointers(pointerString, Transport.expressConsignmentPointer, sequenceId)
      ),
      compareDifference(original.transportPayment, transportPayment, combinePointers(pointerString, TransportPayment.pointer, sequenceId)),
      compareDifference(
        original.borderModeOfTransportCode,
        borderModeOfTransportCode,
        combinePointers(pointerString, TransportLeavingTheBorder.pointer, sequenceId)
      ),
      compareStringDifference(
        original.meansOfTransportOnDepartureType,
        meansOfTransportOnDepartureType,
        combinePointers(pointerString, Transport.transportOnDeparturePointer, sequenceId)
      ),
      compareStringDifference(
        original.meansOfTransportOnDepartureIDNumber,
        meansOfTransportOnDepartureIDNumber,
        combinePointers(pointerString, Transport.transportOnDepartureIdPointer, sequenceId)
      ),
      compareDifference(
        original.transportCrossingTheBorderNationality,
        transportCrossingTheBorderNationality,
        combinePointers(pointerString, TransportCountry.pointer, sequenceId)
      ),
      compareStringDifference(
        original.meansOfTransportCrossingTheBorderType,
        meansOfTransportCrossingTheBorderType,
        combinePointers(pointerString, Transport.transportCrossingTheBorderPointer, sequenceId)
      ),
      compareStringDifference(
        original.meansOfTransportCrossingTheBorderIDNumber,
        meansOfTransportCrossingTheBorderIDNumber,
        combinePointers(pointerString, Transport.transportCrossingTheBorderIdPointer, sequenceId)
      )
    ).flatten ++
      createDiff(original.containers, containers, combinePointers(pointerString, Container.pointer, sequenceId))

  def addOrUpdateContainer(updatedContainer: Container): Transport = {

    def containerSequence: Seq[Container] = containers.getOrElse(Seq.empty)

    def hasContainer(id: String) = containers.exists(_.exists(_.id == id))

    val updatedContainers =
      if (containers.isEmpty) Seq(updatedContainer)
      else if (hasContainer(updatedContainer.id)) {
        containerSequence.map {
          case container if updatedContainer.id == container.id => updatedContainer
          case otherContainer                                   => otherContainer
        }
      } else containerSequence :+ updatedContainer

    copy(containers = Some(updatedContainers))
  }
}

object Transport extends FieldMapping {
  implicit val format: Format[Transport] = Json.format[Transport]

  val pointer: ExportsFieldPointer = "transport"
  val expressConsignmentPointer: ExportsFieldPointer = "expressConsignment"
  val transportOnDeparturePointer: ExportsFieldPointer = "meansOfTransportOnDepartureType"
  val transportOnDepartureIdPointer: ExportsFieldPointer = "meansOfTransportOnDepartureIDNumber"
  val transportCrossingTheBorderPointer: ExportsFieldPointer = "meansOfTransportCrossingTheBorderType"
  val transportCrossingTheBorderIdPointer: ExportsFieldPointer = "meansOfTransportCrossingTheBorderIDNumber"

  type ExpressConsignment = Option[YesNoAnswer]
}
