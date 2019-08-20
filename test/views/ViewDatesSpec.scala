package views
import java.time.LocalDateTime

import org.scalatest.{MustMatchers, WordSpec}

class ViewDatesSpec extends WordSpec with MustMatchers {

  "ViewDates" should {

    "format basic date time correctly" in {

      val date = LocalDateTime.of(2019, 8, 20, 13, 55, 15)
      ViewDates.format(date) must equal("2019-08-20 13:55")
    }

    "format date at time correctly" in {

      val date = LocalDateTime.of(2019, 8, 20, 13, 55, 15)
      ViewDates.formatDateAtTime(date) must equal("20 Aug 2019 at 13:55")
    }
  }
}
