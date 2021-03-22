/*
 * Copyright 2021 HM Revenue & Customs
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

package views.components.gds

import base.{Injector, MockAuthAction, OverridableInjector}
import com.typesafe.config.ConfigFactory
import config.{SecureMessagingConfig, SfusConfig, TimeoutDialogConfig}
import forms.Choice
import views.helpers.CommonMessages
import org.mockito.Mockito.when
import org.scalatest.Matchers._
import play.api.data.Form
import play.api.inject.bind
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.declaration.spec.UnitViewSpec
import views.html.choice_page
import scala.collection.JavaConverters.asScalaIteratorConverter

class TimeoutDialogSpec extends UnitViewSpec with CommonMessages with MockAuthAction {

  "Timeout Dialog" should {

    "display the timeout dialog when user is signed in" in {
      val form: Form[Choice] = Choice.form()

      val serviceConfig = new ServicesConfig(Configuration(ConfigFactory.parseString(s"""
          timeoutDialog.timeout="100 millis"
          timeoutDialog.countdown="200 millis"
          """)))
      val timeoutDialogConfig = new TimeoutDialogConfig(serviceConfig)
      val secureMessagingConfig = mock[SecureMessagingConfig]

      val injector = new OverridableInjector(
        bind[TimeoutDialogConfig].toInstance(timeoutDialogConfig),
        bind[SecureMessagingConfig].toInstance(secureMessagingConfig)
      )
      val choicePage = injector.instanceOf[choice_page]

      when(secureMessagingConfig.isSfusSecureMessagingEnabled).thenReturn(false)
      val view = choicePage(form, Seq.empty[String])(getAuthenticatedRequest(), messages)

      val metas = view.getElementsByTag("meta").iterator.asScala.toList.filter(_.attr("name") == "hmrc-timeout-dialog")

      assert(metas.nonEmpty)
      metas.head.dataset.get("sign-out-url") mustBe controllers.routes.SignOutController.signOut(models.SignOutReason.SessionTimeout).url
    }
  }

}
