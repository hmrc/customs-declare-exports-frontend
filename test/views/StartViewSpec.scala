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
import views.declaration.spec.ViewSpec
import views.html.start_page
import views.tags.ViewTest

@ViewTest
class StartViewSpec extends ViewSpec with StartMessages {

  private def createView(): Html = start_page(appConfig)

  "Start View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Register for customs services")
      assertMessage(heading, "Make an export declaration")
      assertMessage(description, "An export declaration must be completed for any export leaving the UK. This includes goods being exported through the EU to a third (non-EU) country.")
      assertMessage(listHeading, "You will need:")
      assertMessage(listItemOne, "your Government Gateway details")
      assertMessage(listItemTwoPreUrl, "your")
      assertMessage(listItemTwoPostUrl, "number")
      assertMessage(listItemTwoUrl, "Economic Operator Registration and Identification (EORI)")
      assertMessage(listItemThree, "any licences you may need, for example, for military controlled items and highly sensitive dual-use goods")
      assertMessage(listItemFour, "details of where you’re sending the export")

      // for some reason it does not pick up the ' in message - possibly some java/scala issue
      assertMessage(listItemFive, "a description of the item, including the value, weight, size and type of package its in")
      assertMessage(listItemSix, "your")
      assertMessage(listItemSixUrl, "commodity code(s)")
      assertMessage(listItemSeven, "for your item")
      assertMessage(listItemSevenUrl, "customs procedure code(s)")
      assertMessage(buttonName, "Start now")
      assertMessage(additionalInformation, "Your information is saved automatically.")
      assertMessage(referenceTitle, "Help and support")
      assertMessage(reference, "If you are having problems registering, phone:")
      assertMessage(referenceNumber, "0300 200 3700")
      assertMessage(referenceText, "Open 8am to 6pm, Monday to Friday (closed bank holidays)")
      assertMessage(enquiries, "General enquires help")
    }
  }

  "Start View on empty page" when {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display \"Export\" header" in {

      getElementByCss(createView(), "h1").text() must be(messages(heading))
    }

    "display \"Export\" description" in {

      getElementByCss(createView(), "article>div>div>p:nth-child(2)").text() must be(messages(description))
    }

    "display list header" in {

      getElementByCss(createView(), "h3").text() must be(messages(listHeading))
    }

    "display list with elements" should {

      "first element" in {

        getElementByCss(createView(), "article>div>div>ul>li:nth-child(1)").text() must be(messages(listItemOne))
      }

      "second element" in {

        getElementByCss(createView(), "article>div>div>ul>li:nth-child(2)").text() must be(
          messages(listItemTwoPreUrl) + " " + messages(listItemTwoUrl) + " " + messages(listItemTwoPostUrl)
        )
      }

      "link in second element to \"EORI\" page" in {

        val link = getElementByCss(createView(), "article>div>div>ul>li:nth-child(2)>a")

        link.text() must be(messages(listItemTwoUrl))
        link.attr("href") must be("http://www.gov.uk/eori")
      }

      "third element" in {

        getElementByCss(createView(), "article>div>div>ul>li:nth-child(3)").text() must be(messages(listItemThree))
      }

      "fourth element" in {

        getElementByCss(createView(), "article>div>div>ul>li:nth-child(4)").text() must be(messages(listItemFour))
      }

      "fifth element" in {

        getElementByCss(createView(), "article>div>div>ul>li:nth-child(5)").text() must be(messages(listItemFive))
      }

      "sixth element" in {

        getElementByCss(createView(), "article>div>div>ul>li:nth-child(6)").text() must be(
          messages(listItemSix) + " " + messages(listItemSixUrl)
        )
      }

      "link in sixth element to \"Commodity codes\" page" in {

        val link = getElementByCss(createView(), "article>div>div>ul>li:nth-child(6)>a")

        link.text() must be(messages(listItemSixUrl))
        link.attr("href") must be("https://www.gov.uk/trade-tariff")
      }

      "seventh element" in {

        getElementByCss(createView(), "article>div>div>ul>li:nth-child(7)").text() must be(
          messages(listItemSevenUrl) + " " + messages(listItemSeven)
        )
      }

      "link in seventh element to\"Customs procedure codes\" page" in {

        val link = getElementByCss(createView(), "article>div>div>ul>li:nth-child(7)>a")

        link.text() must be(messages(listItemSevenUrl))
        link.attr("href") must be(
          "https://www.gov.uk/government/publications/uk-trade-tariff-customs-procedure-codes/customs-procedure-codes-box-37"
        )
      }
    }

    "display \"Start Now\" button" in {

      getElementByCss(createView(), "article>div>div>p:nth-child(5)>a").text() must be(messages(buttonName))
    }

    "display message under button" in {

      getElementByCss(createView(), "article>div>div>p:nth-child(6)").text() must be(messages(additionalInformation))
    }

    "display \"Help and Support\" header" in {

      getElementByCss(createView(), "article>div>div>div>h2").text() must be(messages(referenceTitle))
    }

    "display \"Help and Support\" description" in {

      getElementByCss(createView(), "#content > article > div > div > div > p:nth-child(2)").text() must be(messages(reference) + " " + messages(referenceNumber) + " " + messages(referenceText))
    }

    "display link to \"General Enquires page\"" in {

      val link = getElementByCss(createView(), "article>div>div>div>p:nth-child(3)>a")

      link.text() must be(messages(enquiries))
      link.attr("href") must be("https://www.gov.uk/government/organisations/hm-revenue-customs/contact/customs-international-trade-and-excise-enquiries")
    }
  }
}
