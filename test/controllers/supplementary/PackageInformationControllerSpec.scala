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

package controllers.supplementary

import base.CustomExportsBaseSpec
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.supplementary.PackageInformation
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.supplementary.package_information

class PackageInformationControllerSpec
    extends CustomExportsBaseSpec with Generators with PropertyChecks with OptionValues {

  val uri = uriWithContextPath("/declaration/supplementary/package-information")

  val form = PackageInformation.form()
  def view(form: Form[PackageInformation] = form, charges: Seq[PackageInformation] = Seq.empty): Html =
    package_information(form, charges)(fakeRequest, messages, appConfig)
  val formId = "PackageInformation"

  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  "PackageInformationController" should {

    "displayForm" should {

      "return UNAUTHORIZED" when {

        "user does not have" in {
          userWithoutEori()
          withCaching[PackageInformation](None)

          val result = route(app, getRequest(uri)).value
          intercept[InsufficientEnrolments](status(result))

        }
      }
      "return OK" when {

        "user is signed in" in {
          authorizedUser()
          withCaching[PackageInformation](None)

          val result = route(app, getRequest(uri)).value
          val stringResult = contentAsString(result)

          status(result) must be(OK)
          stringResult must include(messages("supplementary.packageInformation.title"))
          stringResult must include(messages("supplementary.packageInformation.typesOfPackages"))
          stringResult must include(messages("supplementary.packageInformation.typesOfPackages.hint"))
          stringResult must include(messages("supplementary.packageInformation.numberOfPackages"))
        }
      }

      "load data from cache" when {

        "packageInformation is added before" in {
          authorizedUser()

          val cachedData = arbitraryPackagingSeq.sample
          withCaching[List[PackageInformation]](cachedData, formId)

          val result = route(app, getRequest(uri)).value
          status(result) must be(OK)

          val stringResult = contentAsString(result)

          cachedData.map(res => stringResult.contains(s"${res.size} Packages added"))

        }
      }
    }

    ".onSubmit" should {

      "return UNAUTHORIZED" when {

        "user does not have an eori" in {
          userWithoutEori()
          val body = Seq(("typesOfPackages", "A1"), addActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          intercept[InsufficientEnrolments](status(result))
        }
      }

      "return BAD_REQUEST" when {

        "invalid data is submitted" in {
          authorizedUser()
          withCaching[PackageInformation](None, formId)

          val body = Seq(("typesOfPackages", "1234"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include("Type of package should be a 2 character code")

        }
      }

      "add packageInformation to the cache" when {

        "with valid data and on click of  add" in {

          forAll(arbitrary[PackageInformation]){packaging =>
            authorizedUser()
            withCaching[Seq[PackageInformation]](Some(Seq(packaging.copy())), formId)

            val input = for ((k, Some(v)) <- packaging.getClass.getDeclaredFields.map(_.getName).zip(packaging.productIterator.to).toMap)
              yield k -> v.asInstanceOf[Any].toString

            val payload = input.toSeq :+ addActionUrlEncoded
            val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
            contentAsString(result) must include(s"1 Package added")
            //status(result) must be(SEE_OTHER)
          }
        }
      }

      "remove packageInformation from the cache" when {

        "when valid index is submitted" in {}
      }
      "navigate to additionalInformation" when {

        "on click of continue" in {}
      }

    }
  }

}
