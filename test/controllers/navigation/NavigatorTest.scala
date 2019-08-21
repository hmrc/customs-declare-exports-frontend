package controllers.navigation

import java.time.{LocalDate, ZoneOffset}
import java.util.concurrent.TimeUnit

import config.AppConfig
import models.SignedInUser
import models.requests.{AuthenticatedRequest, ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.cache.ExportsDeclarationBuilder

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class NavigatorTest extends WordSpec with Matchers with MockitoSugar with ExportsDeclarationBuilder {

  private val config = mock[AppConfig]
  private val navigator = new Navigator(config)

  "Go to Draft Confirmation" should {
    val updatedDate = LocalDate.of(2020, 1, 1)
    val expiryDate = LocalDate.of(2020, 1, 1).plusDays(10)

    val declaration = aDeclaration(withUpdateDate(updatedDate))
    val authenticatedRequest = AuthenticatedRequest(FakeRequest("GET", "uri")
      .withSession(ExportsSessionKeys.declarationId -> "declarationId"), mock[SignedInUser])
    val request = JourneyRequest(authenticatedRequest, declaration)

    "Redirect with flash" in {
      given(config.draftTimeToLive).willReturn(FiniteDuration(10, TimeUnit.DAYS))

      val result = navigator.goToDraftConfirmation()(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.declaration.routes.ConfirmationController.displayDraftConfirmation().url)
      flash(result).get(FlashKeys.expiryDate) shouldBe Some(expiryDate.atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli.toString)
      session(result).get(ExportsSessionKeys.declarationId) shouldBe None
    }
  }

  private implicit def result2future: Result => Future[Result] = Future.successful

}
