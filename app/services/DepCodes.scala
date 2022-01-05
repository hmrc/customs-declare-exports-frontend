/*
 * Copyright 2022 HM Revenue & Customs
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

import models.ExportsDeclaration

import java.nio.charset.StandardCharsets.ISO_8859_1
import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters.asScalaIteratorConverter

object DepCodes {

  def isDesignatedExportPlaceCode(declaration: ExportsDeclaration): Boolean =
    declaration.locations.goodsLocation.exists(location => designatedExportPlaceCodes.contains(location.code))

  private val designatedExportPlaceCodes: Set[String] = {
    val filename = "Designated_Export_Place_codes_for_Data_Element-5-23_of_the_CDS__v2__1_.csv"
    val resource = getClass.getClassLoader.getResource(s"code-lists/$filename")
    val path = Paths.get(resource.toURI)
    Files.lines(path, ISO_8859_1)
      .iterator
      .asScala
      .map(_.split(',').last.trim)
      .toSet
      .filter(_.startsWith("GB"))
  }
}
