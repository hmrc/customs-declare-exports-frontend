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

package controllers.actions

import config.featureFlags.SecureMessagingConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.requests.VerifiedEmailRequest
import play.api.mvc.{ActionFunction, Result}

@Singleton
class SecureMessagingAction @Inject() (secureMessagingConfig: SecureMessagingConfig)(implicit ec: ExecutionContext)
    extends ActionFunction[VerifiedEmailRequest, VerifiedEmailRequest] {

  override def invokeBlock[A](request: VerifiedEmailRequest[A], block: VerifiedEmailRequest[A] => Future[Result]): Future[Result] =
    if (secureMessagingConfig.isSecureMessagingEnabled) block(request)
    else throw new IllegalStateException("Secure Messaging not enabled")

  override protected def executionContext: ExecutionContext = ec
}
