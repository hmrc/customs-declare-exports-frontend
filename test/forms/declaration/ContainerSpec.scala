/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.declaration

import forms.common.DeclarationPageBaseSpec
import models.ExportsDeclaration
import models.declaration.{Container, Transport}
import services.AlteredField
import services.AlteredField.constructAlteredField

class ContainerSpec extends DeclarationPageBaseSpec {

  "Container.createDiff" should {
    val baseFieldPointer = s"${ExportsDeclaration.pointer}.${Transport.pointer}.${Container.pointer}"

    val seals = Seq(Seal(1, "one"), Seal(2, "two"), Seal(3, "three"))

    "produce the expected ExportsDeclarationDiff instance" when {
      "no differences exist between the two versions" in {
        withClue("when no seals are present") {
          val container = Container(1, "latest", Seq.empty[Seal])
          container.createDiff(container, Container.pointer, Some(1)) mustBe Seq.empty[AlteredField]
        }

        withClue("when seals are present") {
          val container = Container(1, "latest", seals)
          container.createDiff(container, Container.pointer, Some(1)) mustBe Seq.empty[AlteredField]
        }
      }

      "when seals are present but not equal" in {
        val fieldPointer = s"$baseFieldPointer.#1.${Seal.pointer}"
        withClue("original container's seals are not present") {
          val container = Container(1, "latest", seals)
          container.createDiff(container.copy(seals = Seq.empty[Seal]), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", None, Some(seals(0))),
            constructAlteredField(s"${fieldPointer}.#2", None, Some(seals(1))),
            constructAlteredField(s"${fieldPointer}.#3", None, Some(seals(2)))
          )
        }

        withClue("this container's seals are not present") {
          val container = Container(1, "latest", Seq.empty[Seal])
          container.createDiff(container.copy(seals = seals), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(seals(0)), None),
            constructAlteredField(s"${fieldPointer}.#2", Some(seals(1)), None),
            constructAlteredField(s"${fieldPointer}.#3", Some(seals(2)), None)
          )
        }

        withClue("both container seals contain different number of elements") {
          val container = Container(1, "latest", seals.drop(1))
          container.createDiff(container.copy(seals = seals), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(seals(0)), None)
          )
        }

        withClue("container seals contain elements with different values") {
          val newSeal = Seal(4, "other")
          val container = Container(1, "latest", newSeal +: seals.drop(1))
          container.createDiff(container.copy(seals = seals), baseFieldPointer, Some(1)) mustBe Seq(
            constructAlteredField(s"${fieldPointer}.#1", Some(seals(0)), None),
            constructAlteredField(s"${fieldPointer}.#4", None, Some(newSeal))
          )
        }
      }
    }
  }
}
