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

package services

import services.model.PackageType
import utils.FileReader

import scala.util.matching.Regex

object PackageTypes {

  private val regex: Regex = """^(\w{2}),"?([^"\n]+)"?$""".r

  lazy val all: List[PackageType] =
    FileReader("code-lists/package-types.csv").tail.map {
      case regex(code: String, description: String) =>
        PackageType(code, description)
    }.sortBy(_.description)

  val packageTypeCodeMap: Map[String, PackageType] = all.map(packageType => (packageType.code, packageType)).toMap

  def getPackageType(code: String): PackageType = packageTypeCodeMap(code)
}
