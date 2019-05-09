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

package services.mapping.goodsshipment

import java.util

import forms.declaration.DeclarationAdditionalActors
import models.declaration.DeclarationAdditionalActorsData
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.declaration_ds.dms._2._

import scala.collection.JavaConverters._

object AEOMutualRecognitionPartiesBuilder {

  def build(implicit cacheMap: CacheMap): util.List[GoodsShipment.AEOMutualRecognitionParty] =
    cacheMap
      .getEntry[DeclarationAdditionalActorsData](DeclarationAdditionalActors.formId)
      .filter(_.actors.nonEmpty)
      .map(
        _.actors
          .filter(actor => isDefined(actor))
          .map(createAdditionalActors)
      )
      .getOrElse(Seq.empty)
      .toList
      .asJava

  private def isDefined(actor: DeclarationAdditionalActors): Boolean =
    actor.eori.getOrElse("").nonEmpty || actor.partyType.getOrElse("").nonEmpty

  private def createAdditionalActors(actor: DeclarationAdditionalActors): GoodsShipment.AEOMutualRecognitionParty = {
    val previousDocument = new GoodsShipment.AEOMutualRecognitionParty()

    if (actor.eori.getOrElse("").nonEmpty) {
      val id = new AEOMutualRecognitionPartyIdentificationIDType()
      id.setValue(actor.eori.get)
      previousDocument.setID(id)
    }

    if (actor.partyType.getOrElse("").nonEmpty) {
      val roleCode = new AEOMutualRecognitionPartyRoleCodeType()
      roleCode.setValue(actor.partyType.orNull)
      previousDocument.setRoleCode(roleCode)
    }
    
    previousDocument
  }
}
