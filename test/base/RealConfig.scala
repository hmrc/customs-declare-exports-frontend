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

import com.typesafe.config.{Config, ConfigFactory}
import config.AppConfig
import config.AppConfigSpec.configBareMinimum
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait RealConfig {
  private val validConfig: Config =
    ConfigFactory.parseString(configBareMinimum + """
        |microservice.services.auth.host=localhostauth
        |
        |tracking-consent-frontend.gtm.container=a
        |
        |files.codelists.glc.dep=/code-lists/manyCodes.json
        |files.codelists.additionalProcedureCodes="/code-lists/procedureCodes/additionalProcedureCodes.json"
        |files.codelists.additionalProcedureCodesC21="/code-lists/procedureCodes/additionalProcedureCodesC21.json"
        |files.codelists.countryCodes="/code-lists/countryCodes.json"
        |files.codelists.countryCodeToAliasesLink="/code-lists/linkCountryCodeToAliases.json"
        |files.codelists.countryCodeToShortNameLink="/code-lists/linkCountryCodeToShortName.json"
        |files.codelists.dmsErrorCodes="/code-lists/dmsErrorCodes.json" //this value can be modified by the 'use-improved-error-messages' feature flag to include '-customised' in name before the file extension (e.g. 'dmsErrorCodes-customised.json')
        |files.codelists.holderOfAuthorisationCodes = "/code-lists/holderOfAuthorisationCodes.json"
        |files.codelists.procedureCodes = "/code-lists/procedureCodes/procedureCodes.json"
        |files.codelists.procedureCodesC21 = "/code-lists/procedureCodes/procedureCodesC21.json"
        |files.codelists.procedureCodeToAdditionalProcedureCodesLink="/code-lists/procedureCodes/linkProcedureCodeToAdditionalProcedureCodes.json"
        |files.codelists.procedureCodeToAdditionalProcedureCodesC21Link="/code-lists/procedureCodes/linkProcedureCodeToAdditionalProcedureCodesC21.json"
        |files.codelists.procedureCodesLink = "/code-lists/procedureCodes/linkProcedureCodes.json"
        |files.codelists.additionalDocumentCodeLink = "/code-lists/additionalDocumentCodes/additionalDocumentCodes.json"
        |files.codelists.additionalDocumentStatusCodeLink = "/code-lists/additionalDocumentCodes/additionalDocumentStatusCodes.json"
        |files.codelists.goodsLocationCodeToLocationTypeLink="/code-lists/linkGoodsLocationCodesToLocationType.json"
        |files.codelists.packageTypeCode="/code-lists/packageTypes.json"
        |files.codelists.officeOfExits="/code-lists/officeOfExits.json"
        |files.codelists.customsOffices="/code-lists/customsOffices.json"
        |files.codelists.docTypes="/code-lists/document-type.json"
        |files.codelists.docTypeLinks="/code-lists/linkDocumentTypes.json"
        |
        |files.codelists.glc.acts = "/code-lists/goods-locations-codes/acts-16n-from-15-30-2022.json"
        |files.codelists.glc.airports = "/code-lists/goods-locations-codes/airports-16a-from-03-08-2022.json"
        |files.codelists.glc.approved-dipositories = "/code-lists/goods-locations-codes/approved-dipositories-16h-from-04-05-2022.json"
        |files.codelists.glc.border-inspection-posts = "/code-lists/goods-locations-codes/border-inspection-posts-16g-from-04-05-2022.json"
        |files.codelists.glc.coa-airports = "/code-lists/goods-locations-codes/coa-airports-16b-from-14-04-2022.json"
        |files.codelists.glc.cse = "/code-lists/goods-locations-codes/cse-16l-from-04-05-2022.json"
        |files.codelists.glc.dep = "/code-lists/goods-locations-codes/dep-16k-from-30-11-2022.json"
        |files.codelists.glc.external-itsf = "/code-lists/goods-locations-codes/external-itsf-16f-from-29-20-2022.json"
        |files.codelists.glc.gb-place-names = "/code-lists/goods-locations-codes/gb-place-names-16i.json"
        |files.codelists.glc.gvms = "/code-lists/goods-locations-codes/gvms-16s-from-07-02-2022.json"
        |files.codelists.glc.itsf = "/code-lists/goods-locations-codes/itsf-16d-from-29-11-2022.json"
        |files.codelists.glc.maritime-ports-and-wharves = "/code-lists/goods-locations-codes/maritime-ports-and-wharves-16c-from-30-11-2022.json"
        |files.codelists.glc.other-location-codes = "/code-lists/goods-locations-codes/other-location-codes-16j-from-03-10-2022.json"
        |files.codelists.glc.rail = "/code-lists/goods-locations-codes/rail-16m-from-from-03-10-2022.json"
        |files.codelists.glc.remote-itsf = "/code-lists/goods-locations-codes/remote-itsf-16e-from-15-30-2022.json"
        |files.codelists.glc.roro = "/code-lists/goods-locations-codes/roro-16r-from-12-05-2022.json"
        |
        |files.codelists.hoa.hoa-codes = "/code-lists/holder-of-authorisation-codes/holder-of-authorisation-codes.json"
        |files.codelists.hoa.tagged-hoa-codes = "/code-lists/holder-of-authorisation-codes/tagged-holder-of-authorisation-codes.json"
        |
        |files.codelists.tagged-transport-codes = "/code-lists/tagged-transport-codes.json"
        |
        |list-of-available-journeys="CRT,CAN,SUB"
        |list-of-available-declarations="STANDARD,SUPPLEMENTARY"
        |microservice.services.features.use-improved-error-messages=true
        |microservice.services.customs-declare-exports.host=localhost
        |microservice.services.customs-declare-exports.port=9875
        |microservice.services.customs-declare-exports.submit-declaration=/declaration
        |microservice.services.customs-declare-exports.declarations=/v2/declaration
        |microservice.services.customs-declare-exports.cancel-declaration=/cancellations
        |microservice.services.customs-declare-exports.notifications=/notifications
        |microservice.services.customs-declare-exports.submission=/submission
        |microservice.services.customs-declare-exports.page-of-submissions=/paginated-submissions
        |microservice.services.customs-declare-exports.fetch-ead=/ead
        |microservice.services.customs-declare-exports-movements.host=localhost
        |microservice.services.customs-declare-exports-movements.port=9876
        |microservice.services.customs-declare-exports-movements.save-movement-uri=/save-movement-submission
        |play.frontend.host="self/base-url"
      """.stripMargin)

  val configuration = Configuration(validConfig)

  def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  def appConfig(conf: Configuration, environment: Environment) = new AppConfig(conf, environment, servicesConfig(conf), "AppName")
}
