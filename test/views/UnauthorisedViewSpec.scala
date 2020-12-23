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

package views

import base.Injector
import play.twirl.api.Html
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.unauthorised
import views.tags.ViewTest

@ViewTest
class UnauthorisedViewSpec extends UnitViewSpec with Stubs with Injector {

  private val unauthorisedPage = instanceOf[unauthorised]
  private def view: Html = unauthorisedPage()(request, messages)

  "Unauthorised Page view" should {

    "display page header" in {
      view.getElementsByTag("h1").first() must containMessage("unauthorised.heading")
    }

    "display get EORI link" in {
      val link = view.getElementById("get_eori_link")

      link must containMessage("unauthorised.paragraph.1.bullet.1.link")
      link must haveHref("https://www.gov.uk/eori")
      link.attr("target") mustBe "_self"
    }

    "display access CDS link" in {
      val link = view.getElementById("access_cds_link")

      link must containMessage("unauthorised.paragraph.1.bullet.2.link")
      link must haveHref("https://www.gov.uk/guidance/get-access-to-the-customs-declaration-service")
      link.attr("target") mustBe "_self"
    }

    "display check CDS application status link" in {
      val link = view.getElementById("check_cds_application_status_link")

      link must containMessage("unauthorised.paragraph.2.link")
      link must haveHref("https://www.tax.service.gov.uk/customs/register-for-cds/are-you-based-in-uk")
      link.attr("target") mustBe "_self"
    }
  }
}
