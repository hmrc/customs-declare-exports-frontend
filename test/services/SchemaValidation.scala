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

import java.io.StringReader

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.{Source => XmlSource}
import javax.xml.validation.{Schema, SchemaFactory}
import play.api.Logging

trait SchemaValidation extends Logging {

  private val schemas =
    Seq("/wco-declaration-schemas/declaration/DocumentMetaData_2_DMS.xsd", "/wco-declaration-schemas/declaration/WCO_DEC_2_DMS.xsd")

  def validateXmlAgainstSchema(xml: String): Unit = {
    val schema: Schema = {
      val sources = schemas.map { res =>
        getClass.getResource(res).toString
      }.map { systemId =>
        new StreamSource(systemId)
      }.toArray[XmlSource]

      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(sources)
    }
    val validator = schema.newValidator()

    try {
      validator.validate(new StreamSource(new StringReader(xml)))
    } catch {
      case e: Exception =>
        logger.error(s"Invalid XML: ${e.getMessage}\n$xml", e)
        throw e
    }
    logger.debug("schema validation passed")
  }
}
