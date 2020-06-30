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

package connectors

import config.CustomsExportsCodelistsConfig
import javax.inject.Inject
import services.HolderOfAuthorisationCode
import services.model.Country
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class CustomsExportsCodelistsConnector @Inject()(codelistsConfig: CustomsExportsCodelistsConfig, httpClient: HttpClient)(
  implicit ec: ExecutionContext
) {

  def countries()(implicit hc: HeaderCarrier): Future[List[Country]] =
    httpClient.GET[List[Country]](codelistsConfig.fetchCountries)

  def authorisationCodes()(implicit hc: HeaderCarrier): Future[List[HolderOfAuthorisationCode]] =
    httpClient.GET[List[HolderOfAuthorisationCode]](codelistsConfig.fetchAuthorisationCodes)
}