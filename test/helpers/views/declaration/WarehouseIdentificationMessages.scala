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

package helpers.views.declaration

trait WarehouseIdentificationMessages {

  val warehouse: String = "supplementary.warehouse"

  val title: String = warehouse + ".title"
  val titleHint: String = warehouse + ".title.hint"
  val identificationNumber: String = warehouse + ".identificationNumber"
  val identificationNumberError: String = warehouse + ".identificationNumber.error"
  val identificationNumberHint: String = warehouse + ".identificationNumber.hint"
  val supervisingCustomsOffice: String = warehouse + ".supervisingCustomsOffice"
  val supervisingCustomsOfficeHint: String = warehouse + ".supervisingCustomsOffice.hint"
  val supervisingCustomsOfficeError: String = warehouse + ".supervisingCustomsOffice.error"
  val inlandTransportMode: String = warehouse + ".inlandTransportMode.header"
  val inlandTransportModeHint: String = warehouse + ".inlandTransportMode.header.hint"
  val inlandTransportModeError: String = warehouse + ".inlandTransportMode.error.incorrect"

  val transportMode: String = "supplementary.transportInfo.transportMode"

  val sea: String = transportMode + ".sea"
  val rail: String = transportMode + ".rail"
  val road: String = transportMode + ".road"
  val air: String = transportMode + ".air"
  val postalOrMail: String = transportMode + ".postalOrMail"
  val fixedTransportInstallations: String = transportMode + ".fixedTransportInstallations"
  val inlandWaterway: String = transportMode + ".inlandWaterway"
  val unknown: String = transportMode + ".unknown"
}
