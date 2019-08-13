package models

import play.api.libs.json._

import scala.util.Try

case class Paginated[T](results: Seq[T], page: Page, total: Long) {
  def map[A](mapper: T => A): Paginated[A] = Paginated(results.map(mapper), page, total)
}
object Paginated {
  def apply[T](results: T*): Paginated[T] = Paginated[T](results, Page(), results.size)
  def empty[T](page: Page) = Paginated(Seq.empty[T], page, 0)

  implicit def reads[T](implicit fmt: Reads[T]): Reads[Paginated[T]] = new Reads[Paginated[T]] {
    override def reads(json: JsValue): JsResult[Paginated[T]] = Try { new Paginated[T](
      (json \ "results") match {
        case JsDefined(JsArray(results)) => results.map(_.as[T])
        case _ => throw new IllegalArgumentException("Invalid result set")
      },
      (json \ "page").as[Page],
      (json \ "total").as[Long]
    )}.map(JsSuccess(_)).recover {
      case t: Throwable => JsError(t.getMessage)
    }.get
  }

  implicit def writes[T](implicit fmt: Writes[T]): Writes[Paginated[T]] = new Writes[Paginated[T]] {
    override def writes(paged: Paginated[T]): JsValue = Json.obj(
      "results" -> JsArray(paged.results.map(fmt.writes)),
      "page" -> Json.toJson(paged.page),
      "total" -> JsNumber(paged.total)
    )
  }
}
