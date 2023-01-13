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
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.declaration.declarationHolder.AuthorizationTypeCodes.isAuthCode
import forms.declaration.declarationHolder.DeclarationHolder
import models.ExportsDeclaration

import javax.inject.{Inject, Singleton}

@Singleton
class TaggedAuthCodes @Inject()(codeLinkConnector: CodeLinkConnector) {

  lazy val codesMutuallyExclusive = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesMutuallyExclusive)

  lazy val codesOverridingInlandOrBorderSkip = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesOverridingInlandOrBorderSkip)

  private lazy val codesNeedingSpecificHintText = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesNeedingSpecificHintText)

  private lazy val codesRequiringDocumentation = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesRequiringDocumentation)

  private lazy val codesSkippingInlandOrBorder = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesSkippingInlandOrBorder)

  private lazy val codesSkippingLocationOfGoods = codeLinkConnector.getHolderOfAuthorisationCodesForTag(CodesSkippingLocationOfGoods)

  def authCodesRequiringAdditionalDocs(declaration: ExportsDeclaration): Seq[DeclarationHolder] =
    declaration.parties.declarationHoldersData
      .map(_.holders.filter(isAdditionalDocumentationRequired))
      .getOrElse(List.empty)

  def filterAuthCodesNeedingHintText(authorisationTypeCodes: Seq[String]): Seq[String] =
    authorisationTypeCodes.filter(codesNeedingSpecificHintText.contains(_))

  def hasAuthCodeRequiringAdditionalDocs(declaration: ExportsDeclaration): Boolean =
    declaration.parties.declarationHoldersData.exists(_.holders.exists(isAdditionalDocumentationRequired))

  def skipInlandOrBorder(declarationHolder: DeclarationHolder): Boolean =
    declarationHolder.authorisationTypeCode.exists(codesSkippingInlandOrBorder.contains)

  def skipLocationOfGoods(declaration: ExportsDeclaration): Boolean =
    declaration.isAdditionalDeclarationType(SUPPLEMENTARY_EIDR) && isAuthCode(declaration, codesSkippingLocationOfGoods)

  private def isAdditionalDocumentationRequired(declarationHolder: DeclarationHolder): Boolean =
    declarationHolder.authorisationTypeCode.exists(codesRequiringDocumentation.contains)
}