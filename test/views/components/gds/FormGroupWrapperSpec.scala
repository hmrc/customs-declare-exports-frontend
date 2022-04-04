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

package views.components.gds

import base.Injector
import org.jsoup.nodes.Document
import play.api.data.Forms._
import play.api.data.{Field, Form}
import play.twirl.api.{Html, HtmlFormat}
import views.declaration.spec.UnitViewSpec
import views.html.components.gds.formGroupWrapper

import scala.collection.JavaConverters.{asScalaIterator, asScalaSet}

class FormGroupWrapperSpec extends UnitViewSpec with Injector {

  private val testFieldKey = "testKey"
  private val testErrorMessageKey = "error.required"
  private val formWithoutError = Form(single(testFieldKey -> text()))
  private val formWithError = Form(single(testFieldKey -> text())).withError(testFieldKey, testErrorMessageKey)

  private val formGroupWrapper = instanceOf[formGroupWrapper]
  private def createViewComponent(field: Field)(content: Html): Document =
    formGroupWrapper(field)(content)(messages)

  private val htmlContent = HtmlFormat.raw("HTML Test Content")

  "FormGroupWrapper" when {

    "provided with field without errors" should {

      val formGroupWrapperView = createViewComponent(formWithoutError(testFieldKey))(htmlContent)

      "contain correct form group classes" in {

        val expectedClassNames = Seq("govuk-form-group")

        val actualClassNames = asScalaSet(formGroupWrapperView.getElementById(testFieldKey).classNames()).toSeq

        actualClassNames mustBe expectedClassNames
      }

      "contain no span element with error message" in {

        asScalaIterator(formGroupWrapperView.getElementsByTag("span").iterator()).toSeq mustBe empty
      }

      "contain provided content" in {

        formGroupWrapperView must containHtml(htmlContent.body)
      }
    }

    "provided with field containing errors" should {

      val formGroupWrapperViewWithError = createViewComponent(formWithError(testFieldKey))(htmlContent)

      "contain correct form group classes" in {

        val expectedClassNames = Seq("govuk-form-group", "govuk-form-group--error")

        val actualClassNames = asScalaSet(formGroupWrapperViewWithError.getElementById(testFieldKey).classNames()).toSeq

        actualClassNames mustBe expectedClassNames
      }

      "contain paragraph element with error message" in {

        val paragraphElement = formGroupWrapperViewWithError.getElementById(s"$testFieldKey-error")

        asScalaSet(paragraphElement.classNames()).toSeq mustBe Seq("govuk-error-message")
        paragraphElement must containMessage(testErrorMessageKey)
      }

      "contain provided content" in {

        formGroupWrapperViewWithError must containHtml(htmlContent.body)
      }
    }
  }

}
