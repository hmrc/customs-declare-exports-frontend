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

package controllers.supplementary

import base.CustomExportsBaseSpec
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfter

class DeclarationTypePageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/type")

  before {
    authorizedUser()
    reset(mockCustomsCacheService)
  }

  "DeclarationTypePageController on displayDeclarationTypePage" when {

    "cannot read data from cache" should {
      "return 500 code" in {

      }

      "display error page" in {

      }
    }

    "can read data from cache" should {
      "return 200 code" in {

      }

      "display page  title" in {

      }

      "display page  header" in {

      }

      "display radio button with question text for declaration type" in {

      }

      "display radio button with question text for additional declaration type" in {

      }

      "display information content" in {

      }

      "display \"Save and continue\" button" in {

      }

      "not populate the form fields if cache is empty" in {

      }

      "populate the form fields with data from cache" in {

      }
    }
  }


  "DeclarationTypePageController on submitDeclarationType" when {

    "no value provided for declaration type" should {
      "display the form page with error" in {

      }
    }

    "no value provided for additional declaration type" should {
      "display the form page with error" in {

      }
    }

    "input data provided" should {
      "map the data onto DeclarationTypePageForm" in {

      }

      "save the data to the cache" in {

      }

      "return 200 code" in {

      }

      "redirect to \"Consignment Reference\" page" in {

      }
    }
  }

}
