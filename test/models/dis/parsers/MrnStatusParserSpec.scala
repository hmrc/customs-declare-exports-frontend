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

package models.dis.parsers

import org.scalatest.{MustMatchers, OptionValues, WordSpec}

class MrnStatusParserSpec extends WordSpec with MustMatchers with OptionValues {

  "MrnStatusParser" should {
    "create a MrnStatus instance once all data is provided" in {
      val mrnStatus = new MrnStatusParser().parse(MrnStatusParserTestData.mrnStatusWithAllData("20GB2A57QTFF8X8PA0"))
      mrnStatus.mrn mustBe "20GB2A57QTFF8X8PA0"
      mrnStatus.ucr mustBe Some("20GBAKZ81EQJ2WXYZ")
      mrnStatus.eori mustBe "GB123456789012000"
      mrnStatus.versionId mustBe "1"
      mrnStatus.declarationType mustBe "IMZ"
      mrnStatus.acceptanceDateTime mustBe Some("02 July 2019 at 11:07am")
      mrnStatus.receivedDateTime mustBe "02 July 2019 at 11:08am"
      mrnStatus.releasedDateTime mustBe Some("02 July 2019 at 11:09am")
      mrnStatus.createdDateTime mustNot be(empty)
      mrnStatus.roe mustBe "6"
      mrnStatus.ics mustBe "15"
      mrnStatus.irc mustBe Some("000")
      mrnStatus.goodsItemQuantity mustBe "100"
      mrnStatus.totalPackageQuantity mustBe "10"
      mrnStatus.previousDocuments.length mustBe 6
      mrnStatus.previousDocuments(0).id mustBe "18GBAKZ81EQJ2FGVR"
      mrnStatus.previousDocuments(0).typeCode mustBe "DCR"
      mrnStatus.previousDocuments(1).id mustBe "18GBAKZ81EQJ2FGVA"
      mrnStatus.previousDocuments(1).typeCode mustBe "MCR"
      mrnStatus.previousDocuments(2).id mustBe "18GBAKZ81EQJ2FGVB"
      mrnStatus.previousDocuments(2).typeCode mustBe "MCR"
      mrnStatus.previousDocuments(3).id mustBe "18GBAKZ81EQJ2FGVC"
      mrnStatus.previousDocuments(3).typeCode mustBe "DCR"
      mrnStatus.previousDocuments(4).id mustBe "18GBAKZ81EQJ2FGVD"
      mrnStatus.previousDocuments(4).typeCode mustBe "MCR"
      mrnStatus.previousDocuments(5).id mustBe "18GBAKZ81EQJ2FGVE"
      mrnStatus.previousDocuments(5).typeCode mustBe "MCR"
    }

    "create a MrnStatus instance when partial data is provided" in {
      val mrnStatus = new MrnStatusParser().parse(MrnStatusParserTestData.mrnStatusWithSelectedFields("20GB2A57QTFF8X8PA0"))
      mrnStatus.mrn mustBe "20GB2A57QTFF8X8PA0"
      mrnStatus.ucr mustBe None
      mrnStatus.eori mustBe "GB7172755049242"
      mrnStatus.versionId mustBe "1"
      mrnStatus.declarationType mustBe "EXD"
      mrnStatus.acceptanceDateTime mustBe None
      mrnStatus.receivedDateTime mustBe "27 February 2020 at 11:43am"
      mrnStatus.releasedDateTime mustBe None
      mrnStatus.createdDateTime mustNot be(empty)
      mrnStatus.roe mustBe "H"
      mrnStatus.ics mustBe "14"
      mrnStatus.irc mustBe None
      mrnStatus.goodsItemQuantity mustBe "1"
      mrnStatus.totalPackageQuantity mustBe "1.0"
      mrnStatus.previousDocuments.length mustBe 1
      mrnStatus.previousDocuments.head.id mustBe "8GB123456765080-101SHIP1"
      mrnStatus.previousDocuments.head.typeCode mustBe "DCR"
    }

    "create a MrnStatus instance when partial data is provided with no previous documents" in {
      val mrnStatus = new MrnStatusParser().parse(MrnStatusParserTestData.mrnStatusWithNoPreviousDocuments("20GB2A57QTFF8X8PA0"))
      mrnStatus.mrn mustBe "20GB2A57QTFF8X8PA0"
      mrnStatus.ucr mustBe None
      mrnStatus.eori mustBe "GB7172755049242"
      mrnStatus.versionId mustBe "1"
      mrnStatus.declarationType mustBe "EXD"
      mrnStatus.acceptanceDateTime mustBe None
      mrnStatus.receivedDateTime mustBe "27 February 2020 at 11:43am"
      mrnStatus.releasedDateTime mustBe None
      mrnStatus.createdDateTime mustNot be(empty)
      mrnStatus.roe mustBe "H"
      mrnStatus.ics mustBe "14"
      mrnStatus.irc mustBe None
      mrnStatus.goodsItemQuantity mustBe "1"
      mrnStatus.totalPackageQuantity mustBe "1.0"
      mrnStatus.previousDocuments.length mustBe 0
    }
  }
}
