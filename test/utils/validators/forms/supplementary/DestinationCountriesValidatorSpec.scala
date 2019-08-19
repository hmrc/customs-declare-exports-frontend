package utils.validators.forms.supplementary
import forms.declaration.destinationCountries.DestinationCountries
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import utils.validators.forms.{Invalid, Valid}

class DestinationCountriesValidatorSpec extends WordSpec with MustMatchers {

  "DestinationCountriesValidator validateOnAddition" should {
    "return valid" when {
      "routing countries empty" in {

        val destinationCountries = DestinationCountries("PL", Seq.empty, "FR")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }

      "routing countries populated" in {

        val destinationCountries = DestinationCountries("PL", Seq("YE"), "FR")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }

      "country of dispatch empty" in {

        val destinationCountries = DestinationCountries("", Seq.empty, "FR")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }

      "country of destination empty" in {

        val destinationCountries = DestinationCountries("", Seq.empty, "")
        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(Valid)
      }
    }
    "return invalid" when {
      "routing country does not exist" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "XX"), "FR")

        val expectedValidationResult = Invalid(
          errors = Seq(FormError("countriesOfRouting[1]", "declaration.destinationCountries.countriesOfRouting.error"))
        )

        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(expectedValidationResult)
      }

      "routing country duplicated" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "YE"), "FR")

        val expectedValidationResult =
          Invalid(errors = Seq(FormError("countriesOfRouting", "supplementary.duplication")))

        DestinationCountriesValidator.validateOnAddition(destinationCountries) must be(expectedValidationResult)
      }
    }
  }

  "DestinationCountriesValidator validateOnSaveAndContinue" should {
    "return valid" when {
      "routing countries empty" in {

        val destinationCountries = DestinationCountries("PL", Seq.empty, "FR")
        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(Valid)
      }

      "routing countries populated" in {

        val destinationCountries = DestinationCountries("PL", Seq("YE"), "FR")
        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(Valid)
      }
    }

    "return invalid" when {
      "routing country does not exist" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "XX"), "FR")

        val expectedValidationResult = Invalid(
          errors = Seq(FormError("countriesOfRouting[1]", "declaration.destinationCountries.countriesOfRouting.error"))
        )

        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(expectedValidationResult)
      }

      "routing country duplicated" in {
        val destinationCountries = DestinationCountries("PL", Seq("YE", "YE"), "FR")

        val expectedValidationResult =
          Invalid(errors = Seq(FormError("countriesOfRouting", "supplementary.duplication")))

        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(expectedValidationResult)
      }

      "country of dispatch and destination empty" in {

        val destinationCountries = DestinationCountries("", Seq.empty, "")

        val expectedValidationResult = Invalid(
          errors = Seq(
            FormError("countryOfDispatch", "declaration.destinationCountries.countryOfDispatch.empty"),
            FormError("countryOfDestination", "declaration.destinationCountries.countryOfDestination.empty")
          )
        )

        DestinationCountriesValidator.validateOnSaveAndContinue(destinationCountries) must be(expectedValidationResult)
      }
    }
  }
}
