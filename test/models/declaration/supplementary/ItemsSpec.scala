/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.declaration.supplementary

import forms.supplementary._
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.{MustMatchers, WordSpec}

class ItemsSpec extends WordSpec with MustMatchers {

  private trait SimpleTest {
    val totalNumberOfItemsMock = mock(classOf[TotalNumberOfItems])
    val transactionTypeMock = mock(classOf[TransactionType])
    val itemNumberMock = mock(classOf[GoodsItemNumber])
    val items = Items(
      totalNumberOfItems = Some(totalNumberOfItemsMock),
      transactionType = Some(transactionTypeMock),
      goodsItemNumber = Some(itemNumberMock)
    )

    when(totalNumberOfItemsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(transactionTypeMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(itemNumberMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
  }

  private trait TestMapConcatenation extends SimpleTest {
    val totalNumberOfItemsMap = Map("TotalNumberOfItems" -> "TotalNumberOfItemsValue")
    val transactionTypeMap = Map("TransactionType" -> "TransactionTypeValue")
    val itemNumberMap = Map("GoodItemNumber" -> "GoodItemNumberValue")
    when(totalNumberOfItemsMock.toMetadataProperties()).thenReturn(totalNumberOfItemsMap)
    when(transactionTypeMock.toMetadataProperties()).thenReturn(transactionTypeMap)
    when(itemNumberMock.toMetadataProperties()).thenReturn(itemNumberMap)
  }

  "Items" when {

    "method toMetadataProperties is invoked" should {
      "call the toMetadataProperties on all contained objects" in new SimpleTest {
        items.toMetadataProperties()

        verify(totalNumberOfItemsMock, times(1)).toMetadataProperties()
        verify(transactionTypeMock, times(1)).toMetadataProperties()
        verify(itemNumberMock, times(1)).toMetadataProperties()
      }

      "return Map being sum of all Maps from sub-objects" in new TestMapConcatenation {
        items.toMetadataProperties() must equal(totalNumberOfItemsMap ++ transactionTypeMap ++ itemNumberMap)
      }
    }
  }

}
