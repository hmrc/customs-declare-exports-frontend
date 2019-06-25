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

package models.declaration.submissions

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json._
import uk.gov.hmrc.wco.dec.{Response, ResponseStatus}

class SubmissionStatusSpec extends WordSpec with MustMatchers {

  "Reads for status" should {
    "correctly read a value for every scenario" in {
      SubmissionStatus.StatusFormat.reads(JsString("01")) must be(JsSuccess(Accepted))
      SubmissionStatus.StatusFormat.reads(JsString("02")) must be(JsSuccess(Received))
      SubmissionStatus.StatusFormat.reads(JsString("03")) must be(JsSuccess(Rejected))
      SubmissionStatus.StatusFormat.reads(JsString("05")) must be(JsSuccess(UndergoingPhysicalCheck))
      SubmissionStatus.StatusFormat.reads(JsString("06")) must be(JsSuccess(AdditionalDocumentsRequired))
      SubmissionStatus.StatusFormat.reads(JsString("07")) must be(JsSuccess(Amended))
      SubmissionStatus.StatusFormat.reads(JsString("08")) must be(JsSuccess(Released))
      SubmissionStatus.StatusFormat.reads(JsString("09")) must be(JsSuccess(Cleared))
      SubmissionStatus.StatusFormat.reads(JsString("10")) must be(JsSuccess(Cancelled))
      SubmissionStatus.StatusFormat.reads(JsString("1139")) must be(JsSuccess(CustomsPositionGranted))
      SubmissionStatus.StatusFormat.reads(JsString("1141")) must be(JsSuccess(CustomsPositionDenied))
      SubmissionStatus.StatusFormat.reads(JsString("16")) must be(JsSuccess(GoodsHaveExitedTheCommunity))
      SubmissionStatus.StatusFormat.reads(JsString("17")) must be(JsSuccess(DeclarationHandledExternally))
      SubmissionStatus.StatusFormat.reads(JsString("18")) must be(JsSuccess(AwaitingExitResults))
      SubmissionStatus.StatusFormat.reads(JsString("WrongStatus")) must be(JsSuccess(UnknownStatus))
      SubmissionStatus.StatusFormat.reads(JsString("UnknownStatus")) must be(JsSuccess(UnknownStatus))
    }

    "correctly write a value for every scenario" in {
      Json.toJson(Accepted) must be(JsString("01"))
      Json.toJson(Received) must be(JsString("02"))
      Json.toJson(Rejected) must be(JsString("03"))
      Json.toJson(UndergoingPhysicalCheck) must be(JsString("05"))
      Json.toJson(AdditionalDocumentsRequired) must be(JsString("06"))
      Json.toJson(Amended) must be(JsString("07"))
      Json.toJson(Released) must be(JsString("08"))
      Json.toJson(Cleared) must be(JsString("09"))
      Json.toJson(Cancelled) must be(JsString("10"))
      Json.toJson(CustomsPositionGranted) must be(JsString("1139"))
      Json.toJson(CustomsPositionDenied) must be(JsString("1141"))
      Json.toJson(GoodsHaveExitedTheCommunity) must be(JsString("16"))
      Json.toJson(DeclarationHandledExternally) must be(JsString("17"))
      Json.toJson(AwaitingExitResults) must be(JsString("18"))
      Json.toJson(UnknownStatus) must be(JsString("UnknownStatus"))
    }
  }

  "Retrieve from Response method" should {
    "correctly retrieve Accepted status" in {
      val acceptedResponse = Response("01")

      SubmissionStatus.retrieveFromResponse(acceptedResponse) must be(Accepted)
    }

    "correctly retrieve Received status" in {
      val receivedResponse = Response("02")

      SubmissionStatus.retrieveFromResponse(receivedResponse) must be(Received)
    }

    "correctly retrieve Rejected status" in {
      val rejectedResponse = Response("03")

      SubmissionStatus.retrieveFromResponse(rejectedResponse) must be(Rejected)
    }

    "correctly retrieve UndergoingPhysicalCheck status" in {
      val undergoingPhysicalCheckResponse = Response("05")

      SubmissionStatus.retrieveFromResponse(undergoingPhysicalCheckResponse) must be(UndergoingPhysicalCheck)
    }

    "correctly retrieve AdditionalDocumentsRequired status" in {
      val additionalDocumentsRequiredResponse = Response("06")

      SubmissionStatus.retrieveFromResponse(additionalDocumentsRequiredResponse) must be(AdditionalDocumentsRequired)
    }

    "correctly retrieve Amended status" in {
      val amendedResponse = Response("07")

      SubmissionStatus.retrieveFromResponse(amendedResponse) must be(Amended)
    }

    "correctly retrieve Released status" in {
      val releasedResponse = Response("08")

      SubmissionStatus.retrieveFromResponse(releasedResponse) must be(Released)
    }

    "correctly retrieve Cleared status" in {
      val clearedResponse = Response("09")

      SubmissionStatus.retrieveFromResponse(clearedResponse) must be(Cleared)
    }

    "correctly retrieve Cancelled status" in {
      val cancelledResponse = Response("10")

      SubmissionStatus.retrieveFromResponse(cancelledResponse) must be(Cancelled)
    }

    "correctly retrieve CustomsPositionGranted status" in {
      val customsPositionGrantedResponse =
        Response(functionCode = "11", status = Seq(ResponseStatus(nameCode = Some("39"))))

      SubmissionStatus.retrieveFromResponse(customsPositionGrantedResponse) must be(CustomsPositionGranted)
    }

    "correctly retrieve CustomsPositionDenied status" in {
      val customsPositionDeniedResponse =
        Response(functionCode = "11", status = Seq(ResponseStatus(nameCode = Some("41"))))

      SubmissionStatus.retrieveFromResponse(customsPositionDeniedResponse) must be(CustomsPositionDenied)
    }

    "correctly retrieve GoodsHaveExitedTheCommunity status" in {
      val goodsHaveExitedTheCommunityResponse = Response("16")

      SubmissionStatus.retrieveFromResponse(goodsHaveExitedTheCommunityResponse) must be(GoodsHaveExitedTheCommunity)
    }

    "correctly retrieve DeclarationHandledExternally status" in {
      val declarationHandledExternallyResponse = Response("17")

      SubmissionStatus.retrieveFromResponse(declarationHandledExternallyResponse) must be(DeclarationHandledExternally)
    }

    "correctly retrieve AwaitingExitResults status" in {
      val awaitingExitResultsResponse = Response("18")

      SubmissionStatus.retrieveFromResponse(awaitingExitResultsResponse) must be(AwaitingExitResults)
    }

    "correctly retrieve UnknownStatus status" in {
      val unknownStatusResponse = Response("20")

      SubmissionStatus.retrieveFromResponse(unknownStatusResponse) must be(UnknownStatus)
    }
  }
}
