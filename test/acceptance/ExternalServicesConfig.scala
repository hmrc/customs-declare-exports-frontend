/*
 * Copyright 2018 HM Revenue & Customs
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

package acceptance

object ExternalServicesConfig {
  val Port: Int = sys.env.getOrElse("WIREMOCK_SERVICE_LOCATOR_PORT", "11111").toInt
  val Host = "localhost"
  val sessionCacheDomain = "test-only/keystore"
  val shortLivedCacheDomain = "test-only/save4later"
  val cdsFrontendSource = "cds-frontend"
  val subscriptionEoriNumber = "ZZZ1ZZZZ23ZZZZZZZ"
  val etmpFormBundleId = "077063075008"
}
