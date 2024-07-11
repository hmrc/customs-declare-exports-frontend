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

package services

import connectors.CodeLinkConnector
import connectors.Tag._
import forms.section1.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.section2.authorisationHolder.AuthorisationHolder
import forms.section2.authorisationHolder.AuthorizationTypeCodes.isAuthCode
import models.ExportsDeclaration

import javax.inject.{Inject, Singleton}

@Singleton
class TaggedAuthCodes @Inject() (codeLinkConnector: CodeLinkConnector) {

  lazy val codesMutuallyExclusive = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesMutuallyExclusive)

  lazy val codesOverridingInlandOrBorderSkip = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesOverridingInlandOrBorderSkip)

  lazy val codesNeedingSpecificHintText = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesNeedingSpecificHintText)

  lazy val codesRequiringDocumentation = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesRequiringDocumentation)

  lazy val codesSkippingInlandOrBorder = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesSkippingInlandOrBorder)

  lazy val codesSkippingLocationOfGoods = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesSkippingLocationOfGoods)

  def authCodesRequiringAdditionalDocs(declaration: ExportsDeclaration): Seq[AuthorisationHolder] =
    declaration.parties.declarationHoldersData
      .map(_.holders.filter(isAdditionalDocumentationRequired))
      .getOrElse(List.empty)

  def filterAuthCodesNeedingHintText(authorisationTypeCodes: Seq[String]): Seq[String] =
    authorisationTypeCodes.filter(codesNeedingSpecificHintText.contains(_))

  def hasAuthCodeRequiringAdditionalDocs(declaration: ExportsDeclaration): Boolean =
    declaration.parties.declarationHoldersData.exists(_.holders.exists(isAdditionalDocumentationRequired))

  def isAdditionalDocumentationRequired(authorisationHolder: AuthorisationHolder): Boolean =
    authorisationHolder.authorisationTypeCode.exists(codesRequiringDocumentation.contains)

  def skipInlandOrBorder(authorisationHolder: AuthorisationHolder): Boolean =
    authorisationHolder.authorisationTypeCode.exists(codesSkippingInlandOrBorder.contains)

  def skipLocationOfGoods(declaration: ExportsDeclaration): Boolean =
    declaration.isAdditionalDeclarationType(SUPPLEMENTARY_EIDR) && isAuthCode(declaration, codesSkippingLocationOfGoods)
}
