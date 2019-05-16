package services

import java.io.StringReader

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.{Source => XmlSource}
import javax.xml.validation.{Schema, SchemaFactory}
import play.api.Logger


trait SchemaValidation {
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
        Logger.error(s"Invalid XML: ${e.getMessage}\n$xml", e)
       throw e
    }
    Logger.debug("schema validation passed")
  }
}
