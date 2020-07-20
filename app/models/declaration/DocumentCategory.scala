/*
 * Copyright 2020 HM Revenue & Customs
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

class DocumentCategory(val value: String) {
  override def equals(obj: Any): Boolean = obj match {
    case that: DocumentCategory => this.value == that.value
    case _                      => false
  }

  override def hashCode(): Int = 31 * (if (value == null) 1 else value.hashCode)
}

object DocumentCategory {
  import play.api.libs.json._

  case object SimplifiedDeclaration extends DocumentCategory("Y")
  case object RelatedDocument extends DocumentCategory("Z")

  implicit val format = new Format[DocumentCategory] {
    override def writes(category: DocumentCategory): JsValue = JsString(category.value)

    override def reads(json: JsValue): JsResult[DocumentCategory] = json match {
      case JsString("Y") => JsSuccess(SimplifiedDeclaration)
      case JsString("Z") => JsSuccess(RelatedDocument)
      case _             => JsError("Unknown DocumentCategory")
    }
  }
}
