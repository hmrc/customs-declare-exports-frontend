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

package mock

import base.MockExportCacheService
import config.featureFlags._
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar

trait FeatureFlagMocks extends MockExportCacheService with BeforeAndAfterEach {
  self: MockitoSugar with Suite =>

  val mockDeclarationAmendmentsConfig: DeclarationAmendmentsConfig = mock[DeclarationAmendmentsConfig]
  val mockMerchandiseInBagConfig: MerchandiseInBagConfig = mock[MerchandiseInBagConfig]
  val mockSecureMessagingConfig: SecureMessagingConfig = mock[SecureMessagingConfig]
  val mockSecureMessagingInboxConfig: SecureMessagingInboxConfig = mock[SecureMessagingInboxConfig]
  val mockSfusConfig: SfusConfig = mock[SfusConfig]
  val mockTariffApiConfig: TariffApiConfig = mock[TariffApiConfig]
  val mockTdrUnauthorisedMsgConfig: TdrUnauthorisedMsgConfig = mock[TdrUnauthorisedMsgConfig]

  val mockFeatureSwitchConfig: FeatureSwitchConfig = mock[FeatureSwitchConfig]

}
