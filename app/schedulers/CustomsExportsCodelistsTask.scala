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

package schedulers

import akka.actor.ActorSystem
import connectors.CustomsExportsCodelistsConnector
import javax.inject.Inject
import play.api.Logger
import repositories.AuthorisationCodesRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CustomsExportsCodelistsTask @Inject()(
  actorSystem: ActorSystem,
  customsExportsCodelistsConnector: CustomsExportsCodelistsConnector,
  authorisationCodesRepository: AuthorisationCodesRepository
)(implicit executionContext: ExecutionContext) {

  private val logger = Logger(this.getClass)

  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 10.minutes) {
    logger.info("Fetching authorisation codes")
    customsExportsCodelistsConnector.authorisationCodes()(HeaderCarrier()).flatMap { codes =>
      logger.info("Fetched codes: " + codes)
      logger.info("Updating authorisation codes")
      authorisationCodesRepository.updateAllCodes(codes)
    }
    logger.info("Updated authorisation codes list")
  }
}
