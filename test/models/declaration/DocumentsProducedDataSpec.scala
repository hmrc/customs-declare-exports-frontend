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

import forms.declaration.DocumentsProducedSpec._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsValue}

class DocumentsProducedDataSpec extends WordSpec with MustMatchers {

  "Documents Produced Data object" should {
    "contain correct limit value" in {
      DocumentsProducedData.maxNumberOfItems must be(99)
    }
  }
}

object DocumentsProducedDataSpec {

  val correctDocumentsProducedData = DocumentsProducedData(Seq(correctDocumentsProduced))

  val correctDocumentsProducedDataJSON: JsValue = JsObject(Map("documents" -> JsArray(Seq(correctDocumentsProducedJSON))))

  val emptyDocumentsProducedDataJSON: JsValue = JsObject(Map("documents" -> JsArray(Seq(correctDocumentsProducedJSON))))
}
