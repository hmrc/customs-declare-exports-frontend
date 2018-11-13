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
import play.api.data.Forms._
import play.api.libs.json.Json

case class CancelDeclarationForm(
                              wcoDataModelVersionCode: Option[String] = None,
                              wcoTypeName: Option[String] = None,
                              responsibleAgencyName: Option[String] = None,
                              functionalReferenceID:Option[String]= None,
                              id:String,
                              submitter:  Submitter,
                              additionalInformation: AdditionalInformation,
                              amendment: Amendment
                            )

case class Submitter (id:String)
object Submitter
{
  val formMapping = mapping("id" -> nonEmptyText)(Submitter.apply)(Submitter.unapply)

  implicit val formats = Json.format[Submitter]

}

case class Pointer(sequenceNumeric:Int, documentSectionCode:Option[String])
object Pointer
{
  val formMapping = mapping(
    "sequenceNumeric" -> number,
    "changeReasonCode" -> optional(text))(Pointer.apply)(Pointer.unapply)
  implicit val formats = Json.format[Pointer]

}

//changeReasonCode = length 6 and should be number and is mandatory
case class AdditionalInformation(statementCode:Option[String], statementDescription:String, statementTypeCode:String, pointer: Pointer)
object AdditionalInformation
{
  val formMapping = mapping(
    "statementCode" -> optional(text),
    "statementDescription" -> nonEmptyText,
    "statementTypeCode" -> text,
    "pointer" -> Pointer.formMapping
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)

  implicit val formats = Json.format[AdditionalInformation]
}

//TODO validations on changeReasonCode length 3 and is a number
case class Amendment(changeReasonCode:String)
object Amendment
{
  val formMapping = mapping("changeReasonCode" -> nonEmptyText)(Amendment.apply)(Amendment.unapply)
  implicit val formats = Json.format[Amendment]

}