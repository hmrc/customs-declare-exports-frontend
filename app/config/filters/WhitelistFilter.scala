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

package config.filters

import akka.stream.Materializer
import config.AppConfig
import javax.inject.Inject
import play.api.mvc.{Call, RequestHeader, Result}
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

import scala.concurrent.Future

class WhitelistFilter @Inject()(val appConfig: AppConfig, implicit val mat: Materializer)
    extends AkamaiWhitelistFilter {

  override val whitelist: Seq[String] = appConfig.whitelistIps
  override val destination: Call = Call("GET", appConfig.shutterPage)
  override val excludedPaths: Seq[Call] = appConfig.whitelistExcludedPaths

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] =
    if (appConfig.whiteListEnabled) {
      super.apply(f)(rh)
    } else {
      f(rh)
    }
}
