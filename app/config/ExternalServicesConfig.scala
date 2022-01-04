/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class ExternalServicesConfig @Inject()(val configuration: Configuration) {

  private def loadUrl(key: String): String =
    configuration.getOptional[String](s"urls.$key").getOrElse(throw new IllegalStateException(s"Missing configuration key: urls.$key"))

  val commodityCodesUrl: String = loadUrl("commodityCodes")
  val nactCodesUrl: String = loadUrl("nactCodes")
  val relevantLicensesUrl: String = loadUrl("relevantLicenses")
  val serviceAvailabilityUrl: String = loadUrl("serviceAvailability")
  val customsMovementsFrontendUrl: String = loadUrl("customsMovementsFrontend")

  val govUkUrl: String = loadUrl("govUk")
  val tradeTariffUrl: String = loadUrl("tradeTariff")
  val ecicsToolUrl: String = loadUrl("ecicsTool")

  val eoriService: String = loadUrl("eoriService")
  val cdsRegister: String = loadUrl("cdsRegister")
  val cdsCheckStatus: String = loadUrl("cdsCheckStatus")
  val organisationsUrl: String = loadUrl("organisationsLink")
  val importExports: String = loadUrl("importExports")
}
