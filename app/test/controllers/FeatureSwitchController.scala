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

package test.controllers

import config.AppConfig
import features.Feature.Feature
import features.FeatureStatus.FeatureStatus
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.Future

@Singleton
class FeatureSwitchController @Inject()(implicit val appConfig: AppConfig, cc: ControllerComponents) extends BackendController(cc) {

  def set(feature: Feature, status: FeatureStatus): Action[AnyContent] = Action.async { implicit req =>
    appConfig.setFeatureStatus(feature, status)
    Future.successful(Ok(s"${feature} ${status}"))
  }
}
