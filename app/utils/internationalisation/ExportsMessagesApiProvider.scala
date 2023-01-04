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

package utils.internationalisation

import play.api.http.HttpConfiguration
import play.api.i18n.{DefaultMessagesApiProvider, Langs}
import play.api.{Configuration, Environment}

import javax.inject.{Inject, Singleton}

@Singleton
class ExportsMessagesApiProvider @Inject() (environment: Environment, config: Configuration, langs: Langs, httpConfiguration: HttpConfiguration)
    extends DefaultMessagesApiProvider(environment, config, langs, httpConfiguration) {

  override protected def loadAllMessages: Map[String, Map[String, String]] =
    (langs.availables
      .map(_.code)
      .map { code =>
        (code, loadMessageFiles(s".$code"))
      }
      .toMap: Map[String, Map[String, String]]) + ("default" -> loadMessageFiles("")) + ("default.play" -> loadMessageFiles(".default"))

  private def loadMessageFiles(suffix: String): Map[String, String] =
    config.get[Seq[String]]("messages.file.names").foldLeft(Map.empty[String, String]) { case (acc, name) =>
      acc ++ loadMessages(name + suffix)
    }
}
