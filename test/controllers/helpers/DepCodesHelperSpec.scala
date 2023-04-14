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

package controllers.helpers

import base.{MockExportCacheService, RealConfig, UnitWithMocksSpec}
import config.AppConfig
import connectors.{FileBasedCodeListConnector, GoodsLocationCodesConnector}
import forms.declaration.LocationOfGoods
import play.api.{Environment, Mode}
import utils.JsonFile

class DepCodesHelperSpec extends UnitWithMocksSpec with MockExportCacheService with RealConfig {

  private val appConfig: AppConfig = appConfig(configuration, Environment.simple(mode = Mode.Test))
  private lazy val jsonFile = new JsonFile(Environment.simple(mode = Mode.Test))
  private lazy val glcConnector = new GoodsLocationCodesConnector(appConfig, jsonFile)
  private lazy val codeListConnector = new FileBasedCodeListConnector(appConfig, glcConnector, jsonFile)
  private lazy val depCodesHelper = new DepCodesHelper(codeListConnector)

  "DepCodesHelper on isDesignatedExportPlaceCode" should {

    "return true" when {

      "Goods location code on declaration is a valid DEP code" in {
        val declaration = aDeclaration(withGoodsLocation(LocationOfGoods("GBAUBA5ABDCGD")))

        depCodesHelper.isDesignatedExportPlaceCode(declaration) must be(true)
      }
    }

    "return false" when {

      "Goods location code on declaration is not a valid DEP code" in {
        val declaration = aDeclaration(withGoodsLocation(LocationOfGoods("aaa")))

        depCodesHelper.isDesignatedExportPlaceCode(declaration) must be(false)
      }

      "Goods location code on declaration is missing" in {
        val declaration = aDeclaration()

        depCodesHelper.isDesignatedExportPlaceCode(declaration) must be(false)
      }
    }
  }
}
