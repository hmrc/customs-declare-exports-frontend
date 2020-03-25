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

package utils

import models.DeclarationType
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import org.scalatest.WordSpec
import views.declaration.spec.UnitViewSpec

trait JourneyRequestHelper extends WordSpec {

  def onEveryDeclarationJourney(f: JourneyRequest[_] => Unit): Unit =
    onJourney(DeclarationType.values.toSeq: _*)(f)

  def onJourney(types: DeclarationType*)(f: JourneyRequest[_] => Unit): Unit = {
    if (types.isEmpty) {
      throw new RuntimeException("Provide at lest one journey to test")
    }
    types.foreach {
      case DeclarationType.STANDARD      => onStandard(f)
      case DeclarationType.SUPPLEMENTARY => onSupplementary(f)
      case DeclarationType.SIMPLIFIED    => onSimplified(f)
      case DeclarationType.OCCASIONAL    => onOccasional(f)
      case DeclarationType.CLEARANCE     => onClearance(f)
      case _                             => throw new RuntimeException("Unrecognized declaration type - you could have to implement helper methods")
    }
  }

  def onStandard(f: JourneyRequest[_] => Unit): Unit =
    "on Standard journey render view" that {
      f(UnitViewSpec.standardRequest)
    }

  def onSimplified(f: JourneyRequest[_] => Unit): Unit =
    "on Simplified journey render view" that {
      f(UnitViewSpec.simplifiedRequest)
    }

  def onSupplementary(f: JourneyRequest[_] => Unit): Unit =
    "on Supplementary journey render view" that {
      f(UnitViewSpec.supplementaryRequest)
    }

  def onOccasional(f: JourneyRequest[_] => Unit): Unit =
    "on Occasional journey render view" that {
      f(UnitViewSpec.occasionalRequest)
    }

  def onClearance(f: JourneyRequest[_] => Unit): Unit =
    "on Clearance journey render view" that {
      f(UnitViewSpec.clearanceRequest)
    }
}
