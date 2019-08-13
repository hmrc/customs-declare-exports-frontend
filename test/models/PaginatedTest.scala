package models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

class PaginatedTest extends PlaySpec {

  private val results = Paginated(results = Seq("value"), Page(index = 1, size = 2), total = 3)
  private val json = Json.obj(
    "results" -> Json.arr("value"),
    "page" -> Json.obj(
      "index" -> 1,
      "size" -> 2
    ),
    "total" -> 3
  )

  "Paginated" should {
    "convert to JSON" in {
      Json.toJson[Paginated[String]](results) mustBe json
    }

    "convert from JSON" in {
      Json.fromJson[Paginated[String]](json) mustBe JsSuccess(results)
    }
  }

}
