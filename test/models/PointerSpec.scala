/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import forms.declaration.Seal
import models.declaration.Container
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsString, JsSuccess, Json}
import services.cache.ExportsTestData

class PointerSpec extends WordSpec with MustMatchers with ExportsTestData {

  "PointerSection" should {
    val field = PointerSection("ABC", PointerSectionType.FIELD)
    val sequence = PointerSection("123", PointerSectionType.SEQUENCE)

    "map field to pattern" in {
      field.pattern mustBe "ABC"
    }

    "map sequence to pattern" in {
      sequence.pattern mustBe "$"
    }

    "map field to string" in {
      field.toString mustBe "ABC"
    }

    "map sequence to string" in {
      sequence.toString mustBe "#123"
    }

    "map field from string" in {
      PointerSection("ABC") mustBe field
    }

    "map sequence from string" in {
      PointerSection("#123") mustBe sequence
    }
  }

  "Pointer" should {
    val field1 = PointerSection("ABC", PointerSectionType.FIELD)
    val sequence1 = PointerSection("123", PointerSectionType.SEQUENCE)
    val field2 = PointerSection("000", PointerSectionType.FIELD)
    val sequence2 = PointerSection("321", PointerSectionType.SEQUENCE)
    val pointer = Pointer(List(field1, sequence1, field2, sequence2))

    "map to pattern" in {
      pointer.pattern mustBe "ABC.$.000.$"
    }

    "map to string" in {
      pointer.toString mustBe "ABC.#123.000.#321"
    }

    "serialize to JSON" in {
      Json.toJson(pointer)(Pointer.format) mustBe JsString("ABC.#123.000.#321")
    }

    "deserialize from JSON" in {
      Json.fromJson(JsString("ABC.#123.000.#321"))(Pointer.format) mustBe JsSuccess(pointer)
    }
  }

  "Pointer" should {

    "correctly build url for item" in {

      val sections = Seq(
        PointerSection("declaration", PointerSectionType.FIELD),
        PointerSection("items", PointerSectionType.FIELD),
        PointerSection("2", PointerSectionType.SEQUENCE),
        PointerSection("documentProduced", PointerSectionType.FIELD),
        PointerSection("1", PointerSectionType.SEQUENCE),
        PointerSection("documentStatus", PointerSectionType.FIELD)
      )
      val pointer = Pointer(sections)
      val url = "/customs-declare-exports/declaration/items/ITEM_ID/add-document"
      val expectedItemId = "itemId2"
      val declaration =
        aDeclaration(withItems(anItem(withSequenceId(1), withItemId("itemId1")), anItem(withSequenceId(2), withItemId(expectedItemId))))

      val expectedUrl = s"/customs-declare-exports/declaration/items/$expectedItemId/add-document"

      pointer.url(url, declaration) mustBe expectedUrl
    }

    "return default url for item" when {

      "declaration doesn't have that item" in {

        val sections = Seq(
          PointerSection("declaration", PointerSectionType.FIELD),
          PointerSection("items", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentProduced", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentStatus", PointerSectionType.FIELD)
        )
        val pointer = Pointer(sections)
        val url = "/customs-declare-exports/declaration/items/ITEM_ID/add-document"
        val declaration = aDeclaration()

        val expectedUrl = s"/customs-declare-exports/declaration/export-items"

        pointer.url(url, declaration) mustBe expectedUrl
      }
    }

    "correctly build url for container" in {

      val sections = Seq(
        PointerSection("declaration", PointerSectionType.FIELD),
        PointerSection("containers", PointerSectionType.FIELD),
        PointerSection("2", PointerSectionType.SEQUENCE),
        PointerSection("seals", PointerSectionType.FIELD),
        PointerSection("1", PointerSectionType.SEQUENCE),
        PointerSection("id", PointerSectionType.FIELD)
      )
      val pointer = Pointer(sections)
      val url = "/customs-declare-exports/declaration/containers/CONTAINER_ID/seals"
      val expectedContainerId = "containerId2"
      val declaration = aDeclaration(withContainerData(Seq(Container("containerId2", Seq.empty), Container(expectedContainerId, Seq(Seal("1234"))))))

      val expectedUrl = s"/customs-declare-exports/declaration/containers/$expectedContainerId/seals"

      pointer.url(url, declaration) mustBe expectedUrl
    }

    "return default url for container" when {

      "declaration doesn't have that container" in {

        val sections = Seq(
          PointerSection("declaration", PointerSectionType.FIELD),
          PointerSection("items", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentProduced", PointerSectionType.FIELD),
          PointerSection("1", PointerSectionType.SEQUENCE),
          PointerSection("documentStatus", PointerSectionType.FIELD)
        )
        val pointer = Pointer(sections)
        val url = "/customs-declare-exports/declaration/containers/CONTAINER_ID/seals"
        val declaration = aDeclaration()

        val expectedUrl = s"/customs-declare-exports/declaration/containers"

        pointer.url(url, declaration) mustBe expectedUrl
      }
    }

    "correctly build url page which is not related with items or containers" in {

      val pointer = Pointer(Seq.empty)
      val url = "/customs-declare-exports/declaration/consignmentReferences"
      val declaration = aDeclaration()

      pointer.url(url, declaration) mustBe url
    }
  }
}
