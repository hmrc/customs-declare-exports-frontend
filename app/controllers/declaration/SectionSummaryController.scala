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

package controllers.declaration

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.ActionItemBuilder.lastUrlPlaceholder
import views.helpers.summary.sections.Card1ForReferencesSection
import views.html.declaration.summary.sections._

import javax.inject.Inject

class SectionSummaryController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  references_section: section_summary,
  card1ForReferencesSection: Card1ForReferencesSection
)(implicit appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable {

  def displayPage(sectionNumber: Int): Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    val section = sectionNumber match {
      case 1 => card1ForReferencesSection
    }
    Ok(Html(references_section(section).toString.replace(s"?$lastUrlPlaceholder", "")))
  }

}
