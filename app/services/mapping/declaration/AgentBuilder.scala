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

package services.mapping.declaration

import forms.declaration.{EntityDetails, RepresentativeDetails}
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.Agent
import wco.datamodel.wco.declaration_ds.dms._2._

object AgentBuilder {

  def build(implicit cacheMap: CacheMap): Declaration.Agent =
    cacheMap
      .getEntry[RepresentativeDetails](RepresentativeDetails.formId)
      .map(data => createAgent(data.details))
      .orNull

  private def createAgent(details: EntityDetails): Declaration.Agent = {
    val agent = new Declaration.Agent()

    val agentId = new AgentIdentificationIDType()
    agentId.setValue(details.eori.orNull)
    agent.setID(agentId)

    val agentAddress = new Agent.Address()

    details.address.map(address => {

      if (!Option(address.fullName).getOrElse("").isEmpty) {
        val agentName = new AgentNameTextType()
        agentName.setValue(address.fullName)
        agent.setName(agentName)
      }

      val line = new AddressLineTextType()
      line.setValue(address.addressLine)

      val city = new AddressCityNameTextType
      city.setValue(address.townOrCity)

      val postcode = new AddressPostcodeIDType()
      postcode.setValue(address.postCode)

      val countryCode = new AddressCountryCodeType
      countryCode.setValue(
        allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
      )

      agentAddress.setLine(line)
      agentAddress.setCityName(city)
      agentAddress.setCountryCode(countryCode)
      agentAddress.setPostcodeID(postcode)
    })

    agent.setAddress(agentAddress)
    agent
  }
}
