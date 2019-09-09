package models

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

import org.scalatest.{MustMatchers, WordSpec}
import services.cache.ExportsDeclarationBuilder

class ExportsDeclarationSpec extends WordSpec with MustMatchers with ExportsDeclarationBuilder {

  "Amend" should {
    val currentTime = Instant.now()
    val clock = Clock.fixed(currentTime, ZoneOffset.UTC)

    "override required fields" in {
      val amendedDeclaration = aDeclaration(
        withStatus(DeclarationStatus.COMPLETE),
        withCreatedDate(LocalDate.of(2019, 1, 1)),
        withUpdateDate(LocalDate.of(2019, 1, 1))
      ).amend("source-id")(clock)

      amendedDeclaration.id mustBe None
      amendedDeclaration.status mustBe DeclarationStatus.DRAFT
      amendedDeclaration.createdDateTime mustBe currentTime
      amendedDeclaration.updatedDateTime mustBe currentTime
      amendedDeclaration.sourceId mustBe Some("source-id")
    }
  }

}
