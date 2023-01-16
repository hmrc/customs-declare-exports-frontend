/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.declaration.declarationHolder.AuthorizationTypeCodes.{CSE, EXRR, FP, MIB, MOU}
import org.mockito.ArgumentMatchers.refEq
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import services.TaggedAuthCodes

trait MockTaggedAuthCodes extends MockitoSugar with BeforeAndAfterEach { this: Suite =>

  protected val codeLinkConnector = mock[CodeLinkConnector]
  protected lazy val taggedAuthCodes: TaggedAuthCodes = new TaggedAuthCodes(codeLinkConnector)

  override protected def beforeEach(): Unit = {
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesMutuallyExclusive))).thenReturn(List(CSE, EXRR))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesOverridingInlandOrBorderSkip))).thenReturn(List(FP))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesNeedingSpecificHintText))).thenReturn(List(CSE, EXRR))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesRequiringDocumentation))).thenReturn(codesRequiringDocumentation)
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesSkippingInlandOrBorder))).thenReturn(List(CSE, EXRR))
    when(codeLinkConnector.getHolderOfAuthorisationCodesForTag(refEq(CodesSkippingLocationOfGoods))).thenReturn(List(MOU))
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
}
