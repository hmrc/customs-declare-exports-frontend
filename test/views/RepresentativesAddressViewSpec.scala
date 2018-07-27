/*
 * Copyright 2018 HM Revenue & Customs
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

package views

import forms.RepresentativesAddressFormProvider
import models.{Address, NormalMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.representativesAddress

class RepresentativesAddressViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "representativesAddress"

  val form: Form[Address] = new RepresentativesAddressFormProvider()()

  def createView = () => representativesAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[Address]) => representativesAddress(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "RepresentativesAddress view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)
  }
}
