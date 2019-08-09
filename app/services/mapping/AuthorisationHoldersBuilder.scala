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

package services.mapping

import forms.declaration.DeclarationHolder
import javax.inject.Inject
import models.ExportsDeclaration
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.AuthorisationHolder
import wco.datamodel.wco.declaration_ds.dms._2.{
  AuthorisationHolderCategoryCodeType,
  AuthorisationHolderIdentificationIDType
}

import scala.collection.JavaConverters._

class AuthorisationHoldersBuilder @Inject()() extends ModifyingBuilder[ExportsDeclaration, Declaration] {
  override def buildThenAdd(model: ExportsDeclaration, declaration: Declaration): Unit = {

    val holders = model.parties.declarationHoldersData.map {
      _.holders
        .filter(holder => isDefined(holder))
        .map(holder => mapToAuthorisationHolder(holder))
    }.getOrElse(Seq.empty)
    declaration.getAuthorisationHolder.addAll(holders.toList.asJava)
  }

  private def isDefined(holder: DeclarationHolder): Boolean =
    holder.authorisationTypeCode.isDefined && holder.eori.nonEmpty

  private def mapToAuthorisationHolder(holder: DeclarationHolder): AuthorisationHolder = {
    val authorisationHolder = new AuthorisationHolder()

    val authorisationHolderIdentificationIDType = new AuthorisationHolderIdentificationIDType
    authorisationHolderIdentificationIDType.setValue(holder.eori.orNull)

    val authorisationHolderCategoryCodeType = new AuthorisationHolderCategoryCodeType
    authorisationHolderCategoryCodeType.setValue(holder.authorisationTypeCode.orNull)

    authorisationHolder.setID(authorisationHolderIdentificationIDType)
    authorisationHolder.setCategoryCode(authorisationHolderCategoryCodeType)
    authorisationHolder
  }

}
