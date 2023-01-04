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

package base

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{declarationType, AdditionalDeclarationType}
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.mvc.AnyContent
import services.cache.ExportsTestHelper

trait JourneyTypeTestRunner extends UnitSpec with ExportsTestHelper {

  val simpleStandardDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.STANDARD))
  val simpleSupplementaryDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
  val simpleSimplifiedDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.SIMPLIFIED))
  val simpleOccasionalDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.OCCASIONAL))
  val simpleClearanceDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.CLEARANCE))

  val standardRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.STANDARD)
  val supplementaryRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.SUPPLEMENTARY)
  val simplifiedRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.SIMPLIFIED)
  val occasionalRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.OCCASIONAL)
  val clearanceRequest: JourneyRequest[AnyContent] = journeyRequest(DeclarationType.CLEARANCE)

  def onEveryDeclarationJourney(modifiers: ExportsDeclarationModifier*)(f: JourneyRequest[_] => Unit): Unit =
    onJourney(DeclarationType.values.toSeq: _*)(aDeclaration(modifiers: _*))(f)

  def onJourney(types: DeclarationType*) = new JourneyRunner(types: _*)

  class JourneyRunner(types: DeclarationType*) {

    def apply(f: JourneyRequest[_] => Unit): Unit = apply(aDeclaration())(f)

    def apply(declaration: ExportsDeclaration)(f: JourneyRequest[_] => Unit): Unit = {
      if (types.isEmpty) {
        throw new RuntimeException("Attempt to test against no types - please provide at least one declaration type")
      }
      types.foreach {
        case kind @ DeclarationType.STANDARD      => onStandard(aDeclarationAfter(declaration, withType(kind)))(f)
        case kind @ DeclarationType.SUPPLEMENTARY => onSupplementary(aDeclarationAfter(declaration, withType(kind)))(f)
        case kind @ DeclarationType.SIMPLIFIED    => onSimplified(aDeclarationAfter(declaration, withType(kind)))(f)
        case kind @ DeclarationType.OCCASIONAL    => onOccasional(aDeclarationAfter(declaration, withType(kind)))(f)
        case kind @ DeclarationType.CLEARANCE     => onClearance(aDeclarationAfter(declaration, withType(kind)))(f)
        case _ => throw new RuntimeException("Unrecognized declaration type - you could have to implement helper methods")
      }
    }
  }

  def onEveryAdditionalType(additionalModifiers: ExportsDeclarationModifier*)(f: JourneyRequest[_] => Unit): Unit =
    AdditionalDeclarationType.values.toList.foreach { additionalDeclarationType =>
      val declarationType = AdditionalDeclarationType.declarationType(additionalDeclarationType)
      val modifiers = withAdditionalDeclarationType(additionalDeclarationType) :: additionalModifiers.toList
      onJourney(List(declarationType): _*)(aDeclaration(modifiers: _*))(f)
    }

  def onStandard(f: JourneyRequest[_] => Unit): Unit =
    onStandard(simpleStandardDeclaration)(f)

  def onStandard(declaration: ExportsDeclaration)(f: JourneyRequest[_] => Unit): Unit =
    s"on Standard journey${additionalType(declaration)}" when {
      f(journeyRequest(declaration))
    }

  def onSimplified(f: JourneyRequest[_] => Unit): Unit =
    onSimplified(simpleSimplifiedDeclaration)(f)

  def onSimplified(declaration: ExportsDeclaration)(f: JourneyRequest[_] => Unit): Unit =
    s"on Simplified journey${additionalType(declaration)}" when {
      f(journeyRequest(declaration))
    }

  def onSupplementary(f: JourneyRequest[_] => Unit): Unit =
    onSupplementary(simpleSupplementaryDeclaration)(f)

  def onSupplementary(declaration: ExportsDeclaration)(f: JourneyRequest[_] => Unit): Unit =
    s"on Supplementary journey${additionalType(declaration)}" when {
      f(journeyRequest(declaration))
    }

  def onOccasional(f: JourneyRequest[_] => Unit): Unit =
    onOccasional(simpleOccasionalDeclaration)(f)

  def onOccasional(declaration: ExportsDeclaration)(f: JourneyRequest[_] => Unit): Unit =
    s"on Occasional journey${additionalType(declaration)}" when {
      f(journeyRequest(declaration))
    }

  def onClearance(f: JourneyRequest[_] => Unit): Unit =
    onClearance(simpleClearanceDeclaration)(f)

  def onClearance(declaration: ExportsDeclaration)(f: JourneyRequest[_] => Unit): Unit =
    s"on Clearance journey${additionalType(declaration)}" when {
      f(journeyRequest(declaration))
    }

  def additionalType(declaration: ExportsDeclaration): String =
    declaration.additionalDeclarationType.fold("")(at => s" and $at as additional declaration type")

  def withRequestOfType(declarationType: DeclarationType, modifiers: ExportsDeclarationModifier*): JourneyRequest[AnyContent] =
    journeyRequest(aDeclaration((List(withType(declarationType)) ++ modifiers.toList): _*))

  def withRequest(additionalType: AdditionalDeclarationType, modifiers: ExportsDeclarationModifier*): JourneyRequest[AnyContent] =
    journeyRequest(
      aDeclaration((List(withType(declarationType(additionalType)), withAdditionalDeclarationType(additionalType)) ++ modifiers.toList): _*)
    )
}
