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

package models.declaration

import play.api.libs.json.Json

case class Containers(containers: Seq[Container]) {

  def addOrUpdate(updatedContainer: Container): Seq[Container] =
    if (containers.isEmpty) {
      Seq(updatedContainer)
    } else if (!containers.exists(_.id == updatedContainer.id)) {
      containers :+ updatedContainer
    } else {
      containers.map {
        case container if updatedContainer.id == container.id => updatedContainer
        case otherContainer                                   => otherContainer
      }
    }
}

object Containers {
  implicit val format = Json.format[Containers]

  val maxNumberOfItems = 9999
}
