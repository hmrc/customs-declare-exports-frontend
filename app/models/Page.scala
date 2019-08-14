package models

import play.api.libs.json.{Json, OFormat}
import play.api.mvc.QueryStringBindable

case class Page(index: Int = Page.DEFAULT_INDEX, size: Int = Page.DEFAULT_SIZE)

object Page {
  val DEFAULT_INDEX = 1
  val DEFAULT_SIZE = 50

  implicit val format: OFormat[Page] = Json.format[Page]
  implicit val bindable: QueryStringBindable[Page] =
    new QueryStringBindable[Page] {
      private val intBinder = implicitly[QueryStringBindable[Int]]
      private def queryParamIndex(key: String) = key + "-index"
      private def queryParamSize(key: String) = key + "-size"

      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Page]] = {
        val index = params.get(queryParamIndex(key)).flatMap(_.headOption).map(_.toInt).getOrElse(DEFAULT_INDEX)
        val size = params.get(queryParamSize(key)).flatMap(_.headOption).map(_.toInt).getOrElse(DEFAULT_SIZE)
        Some(Right(Page(index, size)))
      }

      override def unbind(key: String, page: Page): String =
        intBinder.unbind(queryParamIndex(key), page.index) + "&" + intBinder.unbind(queryParamSize(key), page.size)
    }
}
