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

package test

import java.io.StringReader

import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.wco.dec.MetaData

import scala.xml.{Elem, SAXException, XML}

object XmlBehaviours extends PlaySpec {

  val importDeclarationSchemaResources = Seq(
    "/wco-declaration-schemas/declaration/DocumentMetaData_2_DMS.xsd",
    "/wco-declaration-schemas/declaration/WCO_DEC_2_DMS.xsd"
  )

  def validXmlScenario(schemas: Seq[String] = Seq.empty)(test: => Elem): Unit =
    validateAgainstSchemaResources(test.mkString, schemas)

  def validDeclarationXmlScenario()(test: => Elem): Unit =
    validXmlScenario(importDeclarationSchemaResources)(test)

  def isValidImportDeclarationXml(xml: String): Boolean =
    try {
      validateAgainstSchemaResources(xml, importDeclarationSchemaResources)
      true
    } catch {
      case _: SAXException => false
    }

  private def validateAgainstSchemaResources(xml: String, schemas: Seq[String]): Unit = {
    val schema: Schema = {
      val sources = schemas
        .map(res => getClass.getResource(res).toString)
        .map(systemId => new StreamSource(systemId))
        .toArray[Source]
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(sources)
    }
    val validator = schema.newValidator()
    validator.validate(new StreamSource(new StringReader(xml)))
  }

  def hasExpectedOutput[T](meta: MetaData, expected: T)(extractor: Elem => T): Elem = {
    val xml = XML.loadString(meta.toXml)
    extractor(xml) must be(expected)
    xml
  }

  def hasExpectedInput[T](meta: MetaData, expected: T)(extractor: MetaData => T): Unit =
    extractor(MetaData.fromXml(meta.toXml)) must be(expected)

}
