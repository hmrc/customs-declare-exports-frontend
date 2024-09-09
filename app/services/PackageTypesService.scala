/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import connectors.CodeListConnector
import play.api.i18n.Messages
import services.model.PackageType

import javax.inject.{Inject, Singleton}

@Singleton
class PackageTypesService @Inject() (codeListConnector: CodeListConnector) {

  import PackageTypesService._

  def all(implicit messages: Messages): List[PackageType] =
    packageTypesMap(codeListConnector).values.toList.sortBy(_.description)

  def findByCode(code: String)(implicit messages: Messages): PackageType =
    PackageTypesService.findByCode(codeListConnector, code)

  def typesOfPackagesText(typesOfPackages: Option[String])(implicit messages: Messages): Option[String] =
    typesOfPackages.map(types => findByCode(types).asText)
}

object PackageTypesService {

  def findByCode(codeListConnector: CodeListConnector, code: String)(implicit messages: Messages): PackageType =
    packageTypesMap(codeListConnector).getOrElse(code, PackageType(code, "Unknown package type"))

  def packageTypesMap(codeListConnector: CodeListConnector)(implicit messages: Messages): Map[String, PackageType] =
    codeListConnector.getPackageTypes(messages.lang.toLocale)
}
