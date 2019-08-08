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
import forms.common.Address
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsDeclarationBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class DeclarantBuilderSpec extends WordSpec with Matchers with MockitoSugar with ExportsDeclarationBuilder {

  "DeclarantBuilder" should {

    "build then add" when {
      "no declarant details" in {
        val model = aDeclaration(withoutDeclarantDetails())
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getDeclarant should be(null)
      }

      "no eori" in {
        val model = aDeclaration(
          withDeclarantDetails(
            eori = None,
            address = Some(Address("name", "line", "city", "postcode", "United Kingdom"))
          )
        )
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getDeclarant.getID should be(null)
      }

      "no address" in {
        val model = aDeclaration(withDeclarantDetails(eori = Some("eori"), address = None))
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getDeclarant.getAddress should be(null)
      }

      "unknown country" in {
        val model = aDeclaration(
          withDeclarantDetails(
            eori = Some("eori"),
            address = Some(Address("name", "line", "city", "postcode", "unknown"))
          )
        )
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getDeclarant.getAddress.getCountryCode.getValue should be("")
      }

      "populated" in {
        val model = aDeclaration(
          withDeclarantDetails(
            eori = Some("eori"),
            address = Some(Address("name", "line", "city", "postcode", "United Kingdom"))
          )
        )
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getDeclarant.getAddress.getLine.getValue should be("line")
        declaration.getDeclarant.getAddress.getCityName.getValue should be("city")
        declaration.getDeclarant.getAddress.getPostcodeID.getValue should be("postcode")
        declaration.getDeclarant.getAddress.getCountryCode.getValue should be("GB")
        declaration.getDeclarant.getName.getValue should be("name")
        declaration.getDeclarant.getID.getValue should be("eori")
      }
    }
  }

  private def builder = new DeclarantBuilder
}
