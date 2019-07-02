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

package controllers.declaration

import base.{CustomExportsBaseSpec, TestHelper, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.PackageInformation
import generators.Generators
import helpers.views.declaration.{CommonMessages, PackageInformationMessages}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.declaration.package_information
import base.TestHelper._
import services.cache.ExportsCacheModel

class PackageInformationControllerSpec
    extends CustomExportsBaseSpec with Generators with PropertyChecks with OptionValues with ViewValidator
    with PackageInformationMessages with CommonMessages with BeforeAndAfterEach {

  val cacheModel = createModel()
  private val uri = uriWithContextPath(s"/declaration/items/${cacheModel.items.head.id}/package-information")
  private val form = PackageInformation.form()
  private val formId = "PackageInformation"

  def view(form: Form[PackageInformation] = form, charges: Seq[PackageInformation] = Seq.empty): Html =
    package_information(cacheModel.items.head.id, form, charges)(fakeRequest, messages, appConfig)

  val generatePackage: Arbitrary[PackageInformation] = Arbitrary {
    for {
      noOfPackages <- choose[Int](1, 99999)
      typeOfPackages <- alphaNumStr.map(_.take(2))
      marksNumbersId <- alphaNumStr.map(_.take(40))
    } yield PackageInformation(Some(typeOfPackages), Some(noOfPackages), Some(marksNumbersId))
  }

  "Package Information Controller on GET" should {

    "return 200 with a success" in {

      authorizedUser()
      withCaching[List[PackageInformation]](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read data from cache" in {

      authorizedUser()
      withCaching[List[PackageInformation]](
        Some(List(PackageInformation(Some("XX"), Some(101), Some("Secret Mark")))),
        formId
      )

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      page must include("XX")
      page must include("101")
      page must include("Secret Mark")
    }

    "read two items from cache and display it" in {

      authorizedUser()
      withCaching[List[PackageInformation]](
        Some(
          List(
            PackageInformation(Some("XX"), Some(101), Some("Secret Mark")),
            PackageInformation(Some("YX"), Some(102), Some("Even More Secret Mark"))
          )
        ),
        formId
      )

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      page must include("XX")
      page must include("101")
      page must include("Secret Mark")

      page must include("YX")
      page must include("102")
      page must include("Even More Secret Mark")
    }
  }

  "Package Information Controller on POST" when {

    "adding" should {

      "display error when item has incorrect type of packages" in {

        authorizedUser()
        withCaching[List[PackageInformation]](None, formId)

        val payload = Map("typesOfPackages" -> "123").toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, typesOfPackagesError, "#typesOfPackages")
      }

      "display error when item has incorrect number of packages" in {

        authorizedUser()
        withCaching[List[PackageInformation]](None, formId)

        val payload = Map("numberOfPackages" -> "-1").toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, numberOfPackagesError, "#numberOfPackages")
      }

      "display error when item with incorrect shipping marks" in {

        authorizedUser()
        withCaching[List[PackageInformation]](None, formId)

        val payload = Map("shippingMarks" -> TestHelper.createRandomAlphanumericString(50)).toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, shippingMarksLengthError, "#shippingMarks")
      }

      "display error when item already exist" in {

        authorizedUser()
        withCaching[List[PackageInformation]](
          Some(List(PackageInformation(Some("AB"), Some(100), Some("Test")))),
          formId
        )

        val payload = Map("typesOfPackages" -> "AB", "numberOfPackages" -> "100", "shippingMarks" -> "Test").toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, piGlobalDuplicate, "#")
      }

      "display error when having more then 99 items" in {

        authorizedUser()
        val cached = listOfN[PackageInformation](99, generatePackage.arbitrary).sample
        withCaching[List[PackageInformation]](cached, formId)

        val payload = Map("typesOfPackages" -> "AB", "numberOfPackages" -> "100", "shippingMarks" -> "Test").toSeq :+ addActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, piGlobalLimit, "#")
      }
    }

    "saving" should {

      "display errors when item has incorrect type of packages" in {

        authorizedUser()
        withCaching[List[PackageInformation]](None, formId)

        val payload = Map("typesOfPackages" -> "123").toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, typesOfPackagesError, "#typesOfPackages")
        checkErrorLink(page, 2, piGlobalUseAdd, "#")
      }

      "display errors when item has incorrect number of packages" in {

        authorizedUser()
        withCaching[List[PackageInformation]](None, formId)

        val payload = Map("numberOfPackages" -> "-1").toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, numberOfPackagesError, "#numberOfPackages")
        checkErrorLink(page, 2, piGlobalUseAdd, "#")
      }

      "display errors when item has incorrect shipping marks" in {

        authorizedUser()
        withCaching[List[PackageInformation]](None, formId)

        val payload = Map("shippingMarks" -> TestHelper.createRandomAlphanumericString(50)).toSeq :+ saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, shippingMarksLengthError, "#shippingMarks")
        checkErrorLink(page, 2, piGlobalUseAdd, "#")
      }
    }
  }

  "Package Information Controller" should {

    "displayForm" should {

      "return UNAUTHORIZED" when {

        "user is not signed in" in {

          userWithoutEori()
          withCaching[List[PackageInformation]](None, formId)

          val result = route(app, getRequest(uri)).value
          intercept[InsufficientEnrolments](status(result))
        }
      }

      "return OK" when {

        "user is signed in" in {

          authorizedUser()
          withCaching[List[PackageInformation]](None, formId)

          val result = route(app, getRequest(uri)).value
          val stringResult = contentAsString(result)

          status(result) must be(OK)
          stringResult must include(messages(title))
          stringResult must include(messages(typesOfPackages))
          stringResult must include(messages(numberOfPackages))
          stringResult must include(messages(shippingMarks))
          stringResult must include(messages(shippingMarksHint))
        }
      }

      "load data from cache" when {

        "item is added before" in {
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

        "user does not have an EORI" in {

          userWithoutEori()
          withCaching[List[PackageInformation]](None, formId)

          val body = Seq(("typesOfPackages", "A1"), addActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          intercept[InsufficientEnrolments](status(result))
        }
      }

      "return BAD_REQUEST" when {

        "invalid data is submitted" in {

          authorizedUser()
          withCaching[List[PackageInformation]](None, formId)

          val body = Seq(("typesOfPackages", "1234"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(typesOfPackagesError))
        }
      }

      "add item to the cache" when {

        "with valid data and user press 'Add'" in {

          forAll(arbitrary[PackageInformation]) { packaging =>
            reset(mockExportsCacheService)
            authorizedUser()
            withNewCaching(cacheModel)
            withCaching[List[PackageInformation]](None, formId)

            val payload = toMap(packaging).toSeq :+ addActionUrlEncoded
            val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
            status(result) must be(SEE_OTHER)

            result.futureValue.header.headers.get("Location") must be(
              Some(s"/customs-declare-exports/declaration/items/${cacheModel.items.head.id}/package-information")
            )

            verify(mockExportsCacheService, times(1)).get(any[String])
            verify(mockExportsCacheService, times(1)).update(any[String], any[ExportsCacheModel])
            verify(mockCustomsCacheService)
              .cache[Seq[PackageInformation]](any(), ArgumentMatchers.eq(formId), ArgumentMatchers.eq(Seq(packaging)))(
                any(),
                any(),
                any()
              )
          }
        }
      }

      "show global error" when {

        "when no packages added and user press 'Save and continue'" in {

          authorizedUser()
          withCaching[List[PackageInformation]](None, formId)

          val result = route(app, postRequestFormUrlEncoded(uri, Seq(saveAndContinueActionUrlEncoded): _*)).value
          val page = contentAsString(result)
          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)

          // this message is not in messages.en
          val errorLink = getElementByCss(page, "div.error-summary.error-summary--show>ul>li:nth-child(1)>a")

          errorLink.text() must be(
            "You must provide 6/9 item packaged, 6/10 Shipping Marks, 6/11 Number of Packages for a package to be added"
          )
          errorLink.attr("href") must be("#")

          checkErrorLink(page, 2, piGlobalAddOne, "#")
        }

        "when user entered data and press 'Save and continue'" in {

          forAll(arbitrary[PackageInformation]) { packaging =>
            authorizedUser()
            withCaching[Seq[PackageInformation]](Some(Seq(packaging)), formId)

            val payload = toMap(packaging).toSeq :+ saveAndContinueActionUrlEncoded
            val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
            val page = contentAsString(result)
            status(result) must be(BAD_REQUEST)

            checkErrorsSummary(page)
            checkErrorLink(page, 1, piGlobalUseAdd, "#")
          }
        }
      }

      "remove PackageInformation from the cache" when {

        "when valid index is submitted" in {
          forAll(arbitraryPackagingSeq) { packagingSeq =>
            authorizedUser()

            whenever(packagingSeq.nonEmpty) {
              withCaching[List[PackageInformation]](Some(packagingSeq), formId)
              val packaging = packagingSeq.head
              val payload = toMap(packaging).toSeq :+ removeActionUrlEncoded("0")
              val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
              status(result) must be(SEE_OTHER)
              result.futureValue.header.headers.get("Location") must be(
                Some(s"/customs-declare-exports/declaration/items/${cacheModel.items.head.id}/package-information")
              )
              verify(mockCustomsCacheService)
                .cache[Seq[PackageInformation]](
                  any(),
                  ArgumentMatchers.eq(formId),
                  ArgumentMatchers.eq(packagingSeq.filterNot(_ == packaging))
                )(any(), any(), any())
            }
          }
        }
      }

      "navigate to Additional Information" when {

        "user press 'Save and continue' and a record has already been added" in {

          forAll(arbitrary[PackageInformation]) { packaging =>
            authorizedUser()
            withCaching[Seq[PackageInformation]](Some(Seq(packaging)), formId)

            val result = route(app, postRequestFormUrlEncoded(uri, Seq(saveAndContinueActionUrlEncoded): _*)).value
            status(result) must be(SEE_OTHER)
            result.futureValue.header.headers.get("Location") must be(
              Some(s"/customs-declare-exports/declaration/items/${cacheModel.items.head.id}/commodity-measure")
            )
          }
        }
      }
    }
  }

  private def toMap(packaging: PackageInformation) =
    for ((k, Some(v)) <- packaging.getClass.getDeclaredFields
           .map(_.getName)
           .zip(packaging.productIterator.to)
           .toMap)
      yield k -> v.asInstanceOf[Any].toString
}
