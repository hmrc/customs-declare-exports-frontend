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

package services.mapping.declaration
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsDeclarationBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class TypeCodeBuilderSpec extends WordSpec with Matchers with ExportsDeclarationBuilder {

  "TypeCodeBuilder" should {
    "correctly map to the WCO-DEC Type Code Code instance" in {

      val builder = new TypeCodeBuilder

      val declaration = new Declaration
      val model = aDeclaration(withDispatchLocation("EX"), withAdditionalDeclarationType("Y"))
      builder.buildThenAdd(model, declaration)

      declaration.getTypeCode.getValue should be("EXY")
    }
  }
}
