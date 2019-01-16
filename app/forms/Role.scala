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

package forms

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class Role(role: String)

object Role {
  import AllowedRoles._

  implicit val format = Json.format[Role]
  private val correctRole = Set(Declarant, DirectRepresentative, IndirectRepresentative)

  val roleMapping =
    mapping("roleForm" -> text().verifying("Incorrect value", correctRole.contains(_)))(Role.apply)(Role.unapply)

  val roleId = "Role"

  def form() = Form(roleMapping)

  object AllowedRoles {
    val Declarant = "DEC"
    val DirectRepresentative = "DREP"
    val IndirectRepresentative = "IREP"
  }
}
