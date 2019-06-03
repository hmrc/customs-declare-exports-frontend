package services.mapping.declaration.consignment
import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.declaration.destinationCountries.{DestinationCountries, DestinationCountriesStandard}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class IteneraryBuilderSpec extends WordSpec with Matchers {

  "IteneraryBuilder" should {
    "correctly map to the WCO-DEC Itenerary instance" when {
      "only one routing country has been submitted" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(DestinationCountries.formId -> Json.toJson(DestinationCountriesStandard("GB", Seq("FR"), "DE")))
          )
        val itineraries = IteneraryBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        itineraries.size() should be(1)
        itineraries.get(0).getSequenceNumeric.intValue() should be(0)
        itineraries.get(0).getRoutingCountryCode.getValue should be("FR")
      }

      "multiple routing countries have been submitted" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(DestinationCountries.formId -> Json.toJson(DestinationCountriesStandard("GB", Seq("FR", "DE"), "PL")))
          )
        val itineraries = IteneraryBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        itineraries.size() should be(2)
        itineraries.get(0).getSequenceNumeric.intValue() should be(0)
        itineraries.get(0).getRoutingCountryCode.getValue should be("FR")
        itineraries.get(1).getSequenceNumeric.intValue() should be(1)
        itineraries.get(1).getRoutingCountryCode.getValue should be("DE")
      }
    }

    "not map to the WCO-DEC Itenerary instance" when {
      "none routing country has been submitted" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(DestinationCountries.formId -> Json.toJson(DestinationCountriesStandard("GB", Seq(), "FR")))
          )
        val itineraries = IteneraryBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        itineraries.size() should be(0)
      }
    }
  }
}
