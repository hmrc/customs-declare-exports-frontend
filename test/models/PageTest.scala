package models

import org.scalatestplus.play.PlaySpec

class PageTest extends PlaySpec {

  "Page" should {
    "bind" when {
      "both params populated" in {
        Page.bindable.bind("page", Map("page-index" -> Seq("10"), "page-size" -> Seq("20"))) mustBe Some(Right(Page(10, 20)))
      }

      "index only populated" in {
        Page.bindable.bind("page", Map("page-index" -> Seq("10"))) mustBe Some(Right(Page(index = 10)))
      }

      "size only populated" in {
        Page.bindable.bind("page", Map("page-size" -> Seq("20"))) mustBe Some(Right(Page(size = 20)))
      }

      "nothing populated" in {
        Page.bindable.bind("page", Map()) mustBe Some(Right(Page()))
      }
    }

    "unbind" in {
      Page.bindable.unbind("page", Page(1, 2)) mustBe "page-index=1&page-size=2"
    }
  }

}
