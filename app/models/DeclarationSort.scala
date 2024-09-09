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

package models

import play.api.libs.json.{Format, Json, OFormat}
import play.api.mvc.QueryStringBindable
import models.SortBy.SortBy
import models.SortDirection.SortDirection
import utils.EnumJson

case class DeclarationSort(by: SortBy = DeclarationSort.DEFAULT_BY, direction: SortDirection = DeclarationSort.DEFAULT_DIRECTION)

object DeclarationSort {
  val DEFAULT_BY = SortBy.CREATED
  val DEFAULT_DIRECTION = SortDirection.ASC

  implicit val format: OFormat[DeclarationSort] = Json.format[DeclarationSort]
  implicit val bindable: QueryStringBindable[DeclarationSort] =
    new QueryStringBindable[DeclarationSort] {
      private val strBinder = implicitly[QueryStringBindable[String]]
      private def queryParamBy(key: String) = key + "-by"
      private def queryParamDirection(key: String) = key + "-direction"

      def toSortBy: String => Option[SortBy] = value => SortBy.values.find(_.toString == value)

      def toSortDirection: String => Option[SortDirection] = value => SortDirection.values.find(_.toString == value)

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, DeclarationSort]] = {
        val by: SortBy = params.get(queryParamBy(key)).flatMap(_.headOption).flatMap(toSortBy).getOrElse(DEFAULT_BY)
        val direction = params
          .get(queryParamDirection(key))
          .flatMap(_.headOption)
          .flatMap(toSortDirection)
          .getOrElse(DEFAULT_DIRECTION)
        Some(Right(DeclarationSort(by, direction)))
      }

      override def unbind(key: String, sort: DeclarationSort): String =
        strBinder.unbind(queryParamBy(key), sort.by.toString) + "&" + strBinder.unbind(queryParamDirection(key), sort.direction.toString)
    }
}

object SortBy extends Enumeration {
  type SortBy = Value
  implicit val format: Format[SortBy.Value] = EnumJson.format(SortBy)
  val CREATED = Value("declarationMeta.createdDateTime")
  val UPDATED = Value("declarationMeta.updatedDateTime")
}

object SortDirection extends Enumeration {
  type SortDirection = Value
  implicit val format: Format[SortDirection.Value] = EnumJson.format(SortDirection)
  val ASC = Value(1, "asc")
  val DESC = Value(-1, "desc")
}
