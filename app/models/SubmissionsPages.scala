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

package models

import play.api.mvc.QueryStringBindable

case class SubmissionsPages(
  rejectedPageNumber: Int = SubmissionsPages.DefaultPageNumber,
  actionPageNumber: Int = SubmissionsPages.DefaultPageNumber,
  otherPageNumber: Int = SubmissionsPages.DefaultPageNumber
) {
  def changeRejectedPageNumber(newRejectedPageNumber: Int): SubmissionsPages = this.copy(rejectedPageNumber = newRejectedPageNumber)
  def changeActionPageNumber(newActionPageNumber: Int): SubmissionsPages = this.copy(actionPageNumber = newActionPageNumber)
  def changeOtherPageNumber(newOtherPageNumber: Int): SubmissionsPages = this.copy(otherPageNumber = newOtherPageNumber)
}

object SubmissionsPages {
  val DefaultPageNumber = 1

  implicit val binder: QueryStringBindable[SubmissionsPages] = new QueryStringBindable[SubmissionsPages] {
    private val intBinder = implicitly[QueryStringBindable[Int]]
    private def queryParamRejected(key: String): String = key + "-rejected"
    private def queryParamAction(key: String): String = key + "-action"
    private def queryParamOther(key: String): String = key + "-other"

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SubmissionsPages]] = {
      val rejectedPageNumber = params.get(queryParamRejected(key)).flatMap(_.headOption).map(_.toInt).getOrElse(DefaultPageNumber)
      val actionPageNumber = params.get(queryParamAction(key)).flatMap(_.headOption).map(_.toInt).getOrElse(DefaultPageNumber)
      val otherPageNumber = params.get(queryParamOther(key)).flatMap(_.headOption).map(_.toInt).getOrElse(DefaultPageNumber)

      Some(Right(SubmissionsPages(rejectedPageNumber = rejectedPageNumber, actionPageNumber = actionPageNumber, otherPageNumber = otherPageNumber)))
    }

    override def unbind(key: String, submissionsPages: SubmissionsPages): String = {

      def mapPage(number: Int, queryParam: String => String): Option[String] =
        Some(number).filterNot(_ == 1).map(intBinder.unbind(queryParam(key), _))

      Seq(
        mapPage(submissionsPages.rejectedPageNumber, queryParamRejected),
        mapPage(submissionsPages.actionPageNumber, queryParamAction),
        mapPage(submissionsPages.otherPageNumber, queryParamOther)
      ).flatten.mkString("&")
    }
  }

}
