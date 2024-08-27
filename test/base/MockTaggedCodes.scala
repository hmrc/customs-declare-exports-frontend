/*
 * Copyright 2024 HM Revenue & Customs
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

package base

import connectors.CodeLinkConnector
import connectors.Tag._
import forms.section2.authorisationHolder.AuthorizationTypeCodes.{CSE, EXRR, FP, MIB, MOU}
import org.mockito.ArgumentMatchers.refEq
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import services.{TaggedAdditionalDocumentCodes, TaggedAuthCodes}

trait MockTaggedCodes extends MockitoSugar with BeforeAndAfterEach { this: Suite =>

  protected val codeLinkConnector = mock[CodeLinkConnector]
  protected implicit lazy val taggedAuthCodes: TaggedAuthCodes = new TaggedAuthCodes(codeLinkConnector)
  protected implicit lazy val taggedAdditionalDocumentCodes: TaggedAdditionalDocumentCodes = new TaggedAdditionalDocumentCodes(codeLinkConnector)

  override protected def beforeEach(): Unit = {
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesMutuallyExclusive))).thenReturn(List(CSE, EXRR))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesOverridingInlandOrBorderSkip))).thenReturn(List(FP))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesNeedingSpecificHintText))).thenReturn(List(CSE, EXRR))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesRequiringDocumentation))).thenReturn(codesRequiringDocumentation)
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesSkippingInlandOrBorder))).thenReturn(List(CSE, EXRR))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesSkippingLocationOfGoods))).thenReturn(List(MOU))
    when(codeLinkConnector.getAdditionalDocumentCodesForTag(refEq(DocumentCodesRequiringAReason))).thenReturn(documentCodesRequiringAReason)
    when(codeLinkConnector.getAdditionalDocumentStatusCodeForTag(refEq(StatusCodesRequiringAReason))).thenReturn(statusCodesRequiringAReason)
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(codeLinkConnector)
  }

  val codesRequiringDocumentation = List(
    "ACE",
    "ACP",
    "ACR",
    "ACT",
    "AEOC",
    "AEOF",
    "AEOS",
    MIB,
    "BOI",
    "BTI",
    "CCL",
    "CGU",
    CSE,
    "CVA",
    "CW1",
    "CW2",
    "CWP",
    "DEP",
    "DPO",
    "EIR",
    "EPSS",
    "ETD",
    "EUS",
    "EXW",
    "EXWH",
    "FAS",
    "FZ",
    "GGA",
    "IPO",
    "MOU",
    "OPO",
    "REP",
    "REX",
    "RSS",
    "SDE",
    "SIVA",
    "SSE",
    "TEA",
    "TEAH",
    "TRD",
    "TST",
    "UKCS"
  )

  val statusCodesRequiringAReason = List("UA", "UE", "UP", "US", "XX", "XW")

  val documentCodesRequiringAReason = List(
    "Y036",
    "Y037",
    "Y082",
    "Y083",
    "Y105",
    "Y107",
    "Y108",
    "Y109",
    "Y115",
    "Y200",
    "Y201",
    "Y202",
    "Y203",
    "Y204",
    "Y205",
    "Y206",
    "Y207",
    "Y208",
    "Y209",
    "Y210",
    "Y211",
    "Y212",
    "Y213",
    "Y214",
    "Y215",
    "Y216",
    "Y217",
    "Y218",
    "Y219",
    "Y220",
    "Y221",
    "Y222",
    "Y300",
    "Y301",
    "Y900",
    "Y901",
    "Y902",
    "Y903",
    "Y904",
    "Y906",
    "Y907",
    "Y909",
    "Y916",
    "Y917",
    "Y918",
    "Y920",
    "Y921",
    "Y922",
    "Y923",
    "Y924",
    "Y927",
    "Y932",
    "Y934",
    "Y935",
    "Y939",
    "Y945",
    "Y946",
    "Y947",
    "Y948",
    "Y949",
    "Y952",
    "Y953",
    "Y957",
    "Y961",
    "Y966",
    "Y967",
    "Y968",
    "Y969",
    "Y970",
    "Y971"
  )
}
