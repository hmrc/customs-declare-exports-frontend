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

package views

import helpers.views.declaration.StartMessages
import play.twirl.api.Html
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.start_page
import views.tags.ViewTest

@ViewTest
class StartViewSpec extends UnitViewSpec with StartMessages with Stubs {

  private val startPage = new start_page(mainTemplate)
  private def createView(): Html = startPage()

  "Start View on empty page" when {

    "display page title" in {

      createView().select("title").text() mustBe messages(title)
    }

    "display 'Export' header" in {

      createView().select("h1").text() mustBe messages(heading)
    }

    "display 'Export' description" in {

      createView().select("article>div>div>p:nth-child(2)").text() mustBe messages(description)
    }

    "display list header" in {

      createView().select("h3").text() mustBe messages(listHeading)
    }

    "display list with elements" should {

      "first element" in {

        createView().select("article>div>div>ul>li:nth-child(1)").text() mustBe messages(listItemOne)
      }

      "second element" in {

        createView().select("article>div>div>ul>li:nth-child(2)").text() mustBe
          messages(listItemTwoPreUrl) + " " + messages(listItemTwoUrl) + " " + messages(listItemTwoPostUrl)
      }

      "link in second element to 'EORI' page" in {

        val link = createView().select("article>div>div>ul>li:nth-child(2)>a")

        link.text() mustBe messages(listItemTwoUrl)
        link.attr("href") mustBe "http://www.gov.uk/eori"
      }

      "third element" in {

        createView().select("article>div>div>ul>li:nth-child(3)").text() mustBe messages(listItemThree)
      }

      "fourth element" in {

        createView().select("article>div>div>ul>li:nth-child(4)").text() mustBe messages(listItemFour)
      }

      "fifth element" in {

        createView().select("article>div>div>ul>li:nth-child(5)").text() mustBe messages(listItemFive)
      }

      "sixth element" in {

        createView().select("article>div>div>ul>li:nth-child(6)").text() mustBe
          messages(listItemSix) + " " + messages(listItemSixUrl)
      }

      "link in sixth element to 'Commodity codes' page" in {

        val link = createView().select("article>div>div>ul>li:nth-child(6)>a")

        link.text() mustBe messages(listItemSixUrl)
        link.attr("href") mustBe "https://www.gov.uk/trade-tariff"
      }

      "seventh element" in {

        createView().select("article>div>div>ul>li:nth-child(7)").text() mustBe
          messages(listItemSevenUrl) + " " + messages(listItemSeven)
      }

      "link in seventh element to'Customs procedure codes' page" in {

        val link = createView().select("article>div>div>ul>li:nth-child(7)>a")

        link.text() mustBe messages(listItemSevenUrl)
        link.attr("href") mustBe
          "https://www.gov.uk/government/publications/uk-trade-tariff-customs-procedure-codes/customs-procedure-codes-box-37"
      }
    }

    "display 'Start Now' button" in {

      createView().select("article>div>div>p:nth-child(5)>a").text() mustBe messages(buttonName)
    }

    "display message under button" in {

      createView().select("article>div>div>p:nth-child(6)").text() mustBe messages(additionalInformation)
    }

    "display 'Help and Support' header" in {

      createView().select("article>div>div>div>h2").text() mustBe messages(referenceTitle)
    }

    "display 'Help and Support' description" in {

      createView().select("#content > article > div > div > div > p:nth-child(2)").text() mustBe
        messages(reference) + " " + messages(referenceNumber) + " " + messages(referenceText)
    }

    "display link to 'General Enquires page'" in {

      val link = createView().select("article>div>div>div>p:nth-child(3)>a")

      link.text() mustBe messages(enquiries)
      link.attr("href") mustBe
        "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/customs-international-trade-and-excise-enquiries"
    }
  }
}
