package services.mapping.goodsshipment
import forms.declaration.{DeclarationAdditionalActors, DeclarationAdditionalActorsSpec}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class AEOMutualRecognitionPartiesBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "AEOMutualRecognitionPartiesBuilder " should {
    "correctly map to a WCO-DEC GoodsShipment.AEOMutualRecognitionParties instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(DeclarationAdditionalActors.formId -> DeclarationAdditionalActorsSpec.correctAdditionalActorsJSON)
        )
      val actors = AEOMutualRecognitionPartiesBuilder.build(cacheMap)
      actors.size should be(1)
      actors.get(0).getID.getValue should be("DocumentReference")
      actors.get(0).getRoleCode.getValue should be("ABC")
    }

    "handle empty documents when mapping to WCO-DEC GoodsShipment.AEOMutualRecognitionParties" in {
      implicit val cacheMap: CacheMap = mock[CacheMap]
      when(cacheMap.getEntry[DeclarationAdditionalActors](DeclarationAdditionalActors.formId))
        .thenReturn(None)

      val actors = AEOMutualRecognitionPartiesBuilder.build(cacheMap)
      actors.isEmpty shouldBe true
    }
  }
}
