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

package controllers.util

import play.api.data.{Form, FormError}
import uk.gov.hmrc.http.InternalServerException

import scala.util.Try

/**
  * Object to help dealing with multiple items page.
  *
  * Handled logic: Add, Remove and Continue.
  */
object MultipleItemsHelper {

  /**
    * Method to handle adding item with simple validation for duplication or limit for amount of items.
    *
    * @param form - multiple item form
    * @param cachedData - cached data which contains sequence of added items
    * @param limit - maximum limit of items
    * @tparam A - type of case class represents form
    * @return Either which can contain Form with errors or Sequence ready to insert to db
    */
  def add[A](form: Form[A], cachedData: Seq[A], limit: Int): Either[Form[A], Seq[A]] = form.value match {
    case Some(document) => prepareData(form, document, cachedData, limit)
    case _              => Left(form)
  }

  private def prepareData[A](form: Form[A], document: A, cachedData: Seq[A], limit: Int): Either[Form[A], Seq[A]] =
    (duplication(document, cachedData) ++ limitOfElems(limit, cachedData)) match {
      case Seq()  => Right(addElement(document, cachedData))
      case errors => Left(form.copy(errors = errors))
    }

  private def duplication[A](document: A, cachedData: Seq[A]): Seq[FormError] =
    if (cachedData.contains(document)) Seq(FormError("", "supplementary.duplication")) else Seq.empty

  private def limitOfElems[A](limit: Int, cachedData: Seq[A]): Seq[FormError] =
    if (cachedData.length >= limit) Seq(FormError("", "supplementary.limit")) else Seq.empty

  private def addElement[A](document: A, cachedData: Seq[A]): Seq[A] = cachedData :+ document

  /**
    * Method to handle removing item. This method will throw an exception when user try to remove
    * non-existing item
    *
    * @param idOpt - optional item id based on the request
    * @param cachedData - cached data which contains sequence of added items
    * @tparam A - type of case class represents form
    * @return Updated sequence ready to update to db
    */
  def remove[A](idOpt: Option[String], cachedData: Seq[A]): Seq[A] = idOpt match {
    case Some(id) if validateId(id) && cachedData.length - 1 >= id.toInt => removeItem(id, cachedData)
    case _                                             => throw new InternalServerException("Incorrect id")
  }

  private def validateId(id: String): Boolean = id.nonEmpty && id.forall(_.isDigit)

  private def removeItem[A](id: String, cachedData: Seq[A]): Seq[A] =
    cachedData.zipWithIndex.filter(_._2 != id.toInt).map(_._1)

  /**
    * Method to handle continue with items.
    *
    * @param form - multiple item form
    * @param cachedData - cached data which contains sequence of added items
    * @param isMandatory - defines that screen is mandatory
    * @tparam A - type of case class represents form
    * @return Form with updated errors
    */
  def continue[A](form: Form[A], cachedData: Seq[A], isMandatory: Boolean): Form[A] = {
    val errors = checkInputs(form) ++ checkMandatory(isMandatory, cachedData)
    form.copy(errors = errors)
  }

  private def checkInputs[A](form: Form[A]): Seq[FormError] =
    if (!isFormEmpty(form)) Seq(FormError("", "supplementary.continue.error"))
    else Seq.empty

  private def checkMandatory[A](isMandatory: Boolean, cachedData: Seq[A]): Seq[FormError] =
    if (isMandatory && cachedData.isEmpty) Seq(FormError("", "supplementary.continue.mandatory"))
    else Seq.empty

  /**
    * Method to handle save and continue.
    *
    * @param form - multiple item form
    * @param cachedData - cached data which contains sequence of added items
    * @param isMandatory - defines that screen is mandatory
    * @param limit - limit of elements allowed for page
    * @tparam A - type of case class represents form
    * @return Form with updated errors or Sequence ready to insert to db
    */
  def saveAndContinue[A](form: Form[A], cachedData: Seq[A], isMandatory: Boolean, limit: Int): Either[Form[A], Seq[A]] =
    if (!isFormEmpty(form)) add(form, cachedData, limit)
    else {
      val mandatoryFieldsError = checkMandatory(isMandatory, cachedData)
      if (mandatoryFieldsError.nonEmpty) Left(form.copy(errors = mandatoryFieldsError))
      else Right(cachedData)
    }

  private def isFormEmpty[A](form: Form[A]): Boolean =
    retrieveData(form).filter { case (_, value) => value.nonEmpty }.isEmpty

  private def retrieveData[A](form: Form[A]): Map[String, String] =
    form.data.filter { case (name, _) => name != "csrfToken" }
}
