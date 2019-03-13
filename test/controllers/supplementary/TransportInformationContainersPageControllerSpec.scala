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
import models.declaration.supplementary.TransportInformationContainerData
import models.declaration.supplementary.TransportInformationContainerData.formId
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class TransportInformationContainersPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/add-transport-containers")

  before {
    authorizedUser()
    withCaching[TransportInformationContainerData](None, formId)
  }

  "Transport Information Containers Page Controller on display" should {

    "display the whole content" in {
      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("supplementary.transportInfo.containers.title"))
      resultAsString must include(messages("supplementary.transportInfo.containerId"))
    }

    "display 'Save and continue' button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "display 'Back' button that links to 'Office-of-exit' page" in {
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("/declaration/supplementary/transport-information")
    }
  }

}
