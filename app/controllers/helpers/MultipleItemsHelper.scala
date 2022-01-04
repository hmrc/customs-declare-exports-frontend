/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.helpers

import play.api.data.{Form, FormError}

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
    * @param fieldId - (optional) - field id of the HTML element to which the error should be attached
    * @tparam A - type of case class represents form
    * @return Either which can contain Form with errors or Sequence ready to insert to db
    */
  def add[A](form: Form[A], cachedData: Seq[A], limit: Int, fieldId: String = "", messageKey: String): Either[Form[A], Seq[A]] = form.value match {
    case Some(document) => prepareData(form, document, cachedData, limit, fieldId, messageKey)
    case _              => Left(form)
  }

  private def prepareData[A](
    form: Form[A],
    document: A,
    cachedData: Seq[A],
    limit: Int,
    fieldId: String,
    messageKey: String
  ): Either[Form[A], Seq[A]] =
    (duplication(document, cachedData, fieldId, messageKey) ++ limitOfElems(limit, cachedData, fieldId)) match {
      case Seq()  => Right(addElement(document, cachedData))
      case errors => Left(form.copy(errors = errors))
    }

  private def duplication[A](document: A, cachedData: Seq[A], fieldId: String, messageKey: String): Seq[FormError] =
    if (cachedData.contains(document))
      Seq(FormError(fieldId, s"${messageKey}.error.duplicate"))
    else Seq.empty

  private def limitOfElems[A](limit: Int, cachedData: Seq[A], fieldId: String): Seq[FormError] =
    if (cachedData.length >= limit) Seq(FormError(fieldId, "supplementary.limit")) else Seq.empty

  private def addElement[A](document: A, cachedData: Seq[A]): Seq[A] = cachedData :+ document

  def remove[A](cachedData: Seq[A], filterNot: A => Boolean): Seq[A] =
    cachedData.filterNot(filterNot)

  /**
    * Method to handle continue with items.
    *
    * @param form - multiple item form
    * @param cachedData - cached data which contains sequence of added items
    * @param isMandatory - defines that screen is mandatory
    * @param fieldId - (optional) - field id of the HTML element to which the error should be attached
    * @tparam A - type of case class represents form
    * @return Form with updated errors
    */
  def continue[A](form: Form[A], cachedData: Seq[A], isMandatory: Boolean, fieldId: String = ""): Form[A] = {
    val errors = checkInputs(form, fieldId) ++ checkMandatory(isMandatory, cachedData, fieldId)
    form.copy(errors = errors)
  }

  private def checkInputs[A](form: Form[A], fieldId: String): Seq[FormError] =
    if (!isFormEmpty(form)) Seq(FormError(fieldId, "supplementary.continue.error"))
    else Seq.empty

  private def checkMandatory[A](isMandatory: Boolean, cachedData: Seq[A], fieldId: String): Seq[FormError] =
    if (isMandatory && cachedData.isEmpty) Seq(FormError(fieldId, "supplementary.continue.mandatory"))
    else Seq.empty

  /**
    * Method to handle save and continue.
    *
    * @param form - multiple item form
    * @param cachedData - cached data which contains sequence of added items
    * @param isMandatory - defines that screen is mandatory
    * @param limit - limit of elements allowed for page
    * @param fieldId - (optional) - field id of the HTML element to which the error should be attached
    * @tparam A - type of case class represents form
    * @return Form with updated errors or Sequence ready to insert to db
    */
  def saveAndContinue[A](
    form: Form[A],
    cachedData: Seq[A],
    isMandatory: Boolean,
    limit: Int,
    fieldId: String = "",
    messageKey: String
  ): Either[Form[A], Seq[A]] =
    if (!isFormEmpty(form)) add(form, cachedData, limit, fieldId, messageKey)
    else {
      val mandatoryFieldsError = checkMandatory(isMandatory, cachedData, fieldId)
      if (mandatoryFieldsError.nonEmpty) Left(form.copy(errors = mandatoryFieldsError))
      else Right(cachedData)
    }

  private def isFormEmpty[A](form: Form[A]): Boolean =
    !retrieveData(form).exists { case (_, value) => value.nonEmpty }

  private def retrieveData[A](form: Form[A]): Map[String, String] =
    form.data.filter { case (name, _) => name != "csrfToken" }

  def appendAll[A](sequence: Seq[A], maybeA: Option[A]*): Seq[A] = {
    def append(sequence: Seq[A], maybeItem: Option[A]): Seq[A] = maybeItem.foldLeft(sequence)((seq, item) => seq :+ item)
    maybeA.foldLeft(sequence)((sequence, maybeItem) => append(sequence, maybeItem))
  }
}
