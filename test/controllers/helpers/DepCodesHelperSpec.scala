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
import org.mockito.Mockito.{reset, when}
import play.api.{Environment, Mode}
import utils.JsonFile

class DepCodesHelperSpec extends UnitWithMocksSpec with MockExportCacheService with RealConfig {

  private val appConfig = mock[AppConfig]
  private lazy val jsonFile = new JsonFile(Environment.simple(mode = Mode.Test))
  private lazy val glcConnector = new GoodsLocationCodesConnector(appConfig, jsonFile)
  private lazy val codeListConnector = new FileBasedCodeListConnector(appConfig, glcConnector, jsonFile)
  private lazy val depCodesHelper = new DepCodesHelper(codeListConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.glcDep16k).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.holderOfAuthorisationCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodesListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.procedureCodesForC21ListFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.additionalProcedureCodesForC21).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.dmsErrorCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.countryCodes).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.packageTypeCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.officeOfExitsCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.customsOfficesCodeFile).thenReturn("/code-lists/manyCodes.json")
    when(appConfig.documentTypeCodeFile).thenReturn("/code-lists/manyCodes.json")
  }

  "DepCodesHelper on isDesignatedExportPlaceCode" should {

    "return true" when {

      "Goods location code on declaration is a valid DEP code" in {
        val declaration = aDeclaration(withGoodsLocation(LocationOfGoods("001")))

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
