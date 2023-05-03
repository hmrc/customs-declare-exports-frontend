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

package services

import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExplicitlySequencedObject
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class OriginalAndNewValues[T](originalVal: Option[T], newVal: Option[T]) {}

case class AlteredField(fieldPointer: ExportsFieldPointer, values: OriginalAndNewValues[_]) {
  override def toString: String = s"[$fieldPointer -> ${values.originalVal} :: ${values.newVal}]"
}

object AlteredField {
  def constructAlteredField[T](fieldPointer: String, originalVal: T, newVal: T): AlteredField =
    AlteredField(fieldPointer, OriginalAndNewValues(Some(originalVal), Some(newVal)))

  def constructAlteredField[T](fieldPointer: String, originalVal: Option[T], newVal: Option[T]): AlteredField =
    AlteredField(fieldPointer, OriginalAndNewValues(originalVal, newVal))
}

trait DiffTools[T] {
  def createDiff(original: T, pointerString: ExportsFieldPointer, sequenceId: Option[Int]): ExportsDeclarationDiff

  def createDiff[E <: DiffTools[E]](original: Seq[E], current: Seq[E], pointerString: ExportsFieldPointer): ExportsDeclarationDiff = {
    val elementPairs = current.map(Some(_)).zipAll(original.map(Some(_)), None, None)

    val allDifferences = elementPairs.zipWithIndex.map { pairAndIndex =>
      pairAndIndex match {
        case ((Some(x), Some(y)), i) => x.createDiff(y, pointerString, Some(i + 1))
        case ((None, None), _)       => None
        case ((curr, orig), i)       => Some(AlteredField(combinePointers(pointerString, Some(i + 1)), OriginalAndNewValues(orig, curr)))
      }
    }

    allDifferences.flatten
  }

  def createDiff[S <: DiffTools[S] with ExplicitlySequencedObject[S]](original: Seq[S], current: Seq[S], pointerString: ExportsFieldPointer)(
    implicit esoTag: DummyImplicit
  ): ExportsDeclarationDiff = {
    val intersectingObjectPairs = for {
      originalObject <- original
      currentObject <- current
      if originalObject.sequenceId == currentObject.sequenceId
    } yield (Some(currentObject), Some(originalObject))

    val intersectingObjectDiffs = intersectingObjectPairs.map { case (Some(x), Some(y)) =>
      x.createDiff(y, pointerString, Some(x.sequenceId))
    }

    val originalObjectIds = original.map(_.sequenceId).toSet
    val currentObjectIds = current.map(_.sequenceId).toSet
    val newObjectsIds = currentObjectIds.diff(originalObjectIds)
    val removedObjectsIds = originalObjectIds.diff(currentObjectIds)
    val removedObjectsDiff =
      removedObjectsIds.map(sequenceId =>
        AlteredField(combinePointers(s"$pointerString", Some(sequenceId)), OriginalAndNewValues(original.find(_.sequenceId == sequenceId), None))
      )
    val newObjectsDiff =
      newObjectsIds.map(sequenceId =>
        AlteredField(combinePointers(s"$pointerString", Some(sequenceId)), OriginalAndNewValues(None, current.find(_.sequenceId == sequenceId)))
      )

    intersectingObjectDiffs.flatten ++ removedObjectsDiff ++ newObjectsDiff
  }

  def createDiff[S <: ExplicitlySequencedObject[S]](original: Seq[S], current: Seq[S], pointerString: ExportsFieldPointer)(
    implicit esoTag: DummyImplicit,
    nonDiffEsoTag: DummyImplicit
  ): ExportsDeclarationDiff = {
    val originalObjectIds = original.map(_.sequenceId).toSet
    val currentObjectIds = current.map(_.sequenceId).toSet
    val newObjectsIds = currentObjectIds.diff(originalObjectIds)
    val removedObjectsIds = originalObjectIds.diff(currentObjectIds)
    val removedObjectsDiff =
      removedObjectsIds.map(sequenceId =>
        AlteredField(combinePointers(s"$pointerString", Some(sequenceId)), OriginalAndNewValues(original.find(_.sequenceId == sequenceId), None))
      )
    val newObjectsDiff =
      newObjectsIds.map(sequenceId =>
        AlteredField(combinePointers(s"$pointerString", Some(sequenceId)), OriginalAndNewValues(None, current.find(_.sequenceId == sequenceId)))
      )

    (removedObjectsDiff ++ newObjectsDiff).toSeq
  }

  def createDiff[E <: DiffTools[E]](original: Option[Seq[E]], current: Option[Seq[E]], pointerString: ExportsFieldPointer): ExportsDeclarationDiff =
    createDiff(original.getOrElse(Seq.empty[E]), current.getOrElse(Seq.empty[E]), pointerString)

  def createDiff[S <: DiffTools[S] with ExplicitlySequencedObject[S]](
    original: Option[Seq[S]],
    current: Option[Seq[S]],
    pointerString: ExportsFieldPointer
  )(implicit esoTag: DummyImplicit): ExportsDeclarationDiff =
    createDiff(original.getOrElse(Seq.empty[S]), current.getOrElse(Seq.empty[S]), pointerString)

  def createDiffOfOptions[E <: DiffTools[E]](original: Option[E], current: Option[E], pointerString: ExportsFieldPointer): ExportsDeclarationDiff =
    (original, current) match {
      case (None, None)             => Seq.empty[AlteredField]
      case (orig, None)             => Seq(AlteredField(pointerString, OriginalAndNewValues(orig, None)))
      case (None, curr)             => Seq(AlteredField(pointerString, OriginalAndNewValues(None, curr)))
      case (Some(orig), Some(curr)) => curr.createDiff(orig, pointerString, None)
    }
}

object DiffTools {

  type ExportsDeclarationDiff = Seq[AlteredField]

  def compareBooleanDifference(original: Boolean, current: Boolean, pointerString: ExportsFieldPointer): Option[AlteredField] =
    Option.when(!current.compare(original).equals(0))(AlteredField(pointerString, OriginalAndNewValues(Some(original), Some(current))))

  def compareBooleanDifference(original: Option[Boolean], current: Option[Boolean], pointerString: ExportsFieldPointer): Option[AlteredField] =
    (original, current) match {
      case (Some(x), Some(y)) => compareBooleanDifference(x, y, pointerString)
      case (None, None)       => None
      case _                  => Some(AlteredField(pointerString, OriginalAndNewValues(original, current)))
    }

  def compareStringDifference(original: String, current: String, pointerString: ExportsFieldPointer): Option[AlteredField] =
    Option.when(!current.compare(original).equals(0))(AlteredField(pointerString, OriginalAndNewValues(Some(original), Some(current))))

  def compareStringDifference(original: Option[String], current: Option[String], pointerString: ExportsFieldPointer): Option[AlteredField] =
    (original, current) match {
      case (Some(x), Some(y)) => compareStringDifference(x, y, pointerString)
      case (None, None)       => None
      case _                  => Some(AlteredField(pointerString, OriginalAndNewValues(original, current)))
    }

  def compareStringDifference(original: Seq[String], current: Seq[String], pointerString: ExportsFieldPointer): ExportsDeclarationDiff = {
    val elementPairs = original.map(Some(_)).zipAll(current.map(Some(_)), None, None)

    val allDifferences = elementPairs.zipWithIndex.map { pairAndIndex =>
      pairAndIndex match {
        case ((Some(x), Some(y)), i) => compareStringDifference(x, y, combinePointers(pointerString, Some(i + 1)))
        case ((None, None), _)       => None
        case ((orig, curr), i)       => Some(AlteredField(combinePointers(pointerString, Some(i + 1)), OriginalAndNewValues(orig, curr)))
      }
    }

    allDifferences.flatten
  }

  def compareIntDifference(original: Int, current: Int, pointerString: ExportsFieldPointer): Option[AlteredField] =
    Option.when(!current.compare(original).equals(0))(AlteredField(pointerString, OriginalAndNewValues(Some(original), Some(current))))

  def compareIntDifference(original: Option[Int], current: Option[Int], pointerString: ExportsFieldPointer): Option[AlteredField] =
    (original, current) match {
      case (Some(x), Some(y)) => compareIntDifference(x, y, pointerString)
      case (None, None)       => None
      case _                  => Some(AlteredField(pointerString, OriginalAndNewValues(original, current)))
    }

  def compareBigDecimalDifference(original: BigDecimal, current: BigDecimal, pointerString: ExportsFieldPointer): Option[AlteredField] =
    Option.when(!current.compare(original).equals(0))(AlteredField(pointerString, OriginalAndNewValues(Some(original), Some(current))))

  def compareBigDecimalDifference(
    original: Option[BigDecimal],
    current: Option[BigDecimal],
    pointerString: ExportsFieldPointer
  ): Option[AlteredField] =
    (original, current) match {
      case (Some(x), Some(y)) => compareBigDecimalDifference(x, y, pointerString)
      case (None, None)       => None
      case _                  => Some(AlteredField(pointerString, OriginalAndNewValues(original, current)))
    }

  def compareDifference[T <: Ordered[T]](original: T, current: T, pointerString: ExportsFieldPointer): Option[AlteredField] =
    Option.when(!current.compare(original).equals(0))(AlteredField(pointerString, OriginalAndNewValues(Some(original), Some(current))))

  def compareDifference[T <: Ordered[T]](original: Option[T], current: Option[T], pointerString: ExportsFieldPointer): Option[AlteredField] =
    (original, current) match {
      case (Some(x), Some(y)) => compareDifference(x, y, pointerString)
      case (None, None)       => None
      case _                  => Some(AlteredField(pointerString, OriginalAndNewValues(original, current)))
    }

  def compareDifference[T <: Ordered[T]](original: Seq[T], current: Seq[T], pointerString: ExportsFieldPointer): ExportsDeclarationDiff = {
    val elementPairs = original.map(Some(_)).zipAll(current.map(Some(_)), None, None)

    val allDifferences = elementPairs.zipWithIndex.map { pairAndIndex =>
      pairAndIndex match {
        case ((Some(x), Some(y)), i) => compareDifference(x, y, combinePointers(pointerString, Some(i + 1)))
        case ((None, None), _)       => None
        case ((orig, curr), i)       => Some(AlteredField(combinePointers(pointerString, Some(i + 1)), OriginalAndNewValues(orig, curr)))
      }
    }

    allDifferences.flatten
  }

  def compareDifference[T <: Ordered[T]](
    original: Option[Seq[T]],
    current: Option[Seq[T]],
    pointerString: ExportsFieldPointer
  ): ExportsDeclarationDiff =
    (original, current) match {
      case (Some(x), Some(y)) => compareDifference(x, y, pointerString)
      case (None, None)       => Seq.empty
      case _                  => Seq(AlteredField(pointerString, OriginalAndNewValues(original, current)))
    }

  def combinePointers(parent: ExportsFieldPointer, childIndex: Option[Int]): ExportsFieldPointer = {
    val sequenceIndexPart = childIndex.fold("")(idx => s".#$idx") // Hash is used to denote following Int is a sequenceId, as opposed to a TagId
    s"$parent$sequenceIndexPart"
  }

  def combinePointers(parent: ExportsFieldPointer, child: ExportsFieldPointer, childIndex: Option[Int] = None): ExportsFieldPointer =
    s"${combinePointers(parent, childIndex)}.$child"

  def compareSequences[A](seq1: Seq[A], seq2: Seq[A]): Boolean =
    seq1.size == seq2.size && seq1.zip(seq2).forall { case (x, y) => x == y }

  def removeTrailingSequenceNbr(field: AlteredField): AlteredField =
    if (field.fieldPointer.length > 0 && field.fieldPointer.takeRight(1).charAt(0).isDigit) {
      val lastPeriodIndex = field.fieldPointer.lastIndexOf('.')
      val dropCount = field.fieldPointer.length - lastPeriodIndex
      field.copy(fieldPointer = field.fieldPointer.dropRight(dropCount))
    } else field

  def compareOptionalString(optionOne: Option[String], optionTwo: Option[String]): Int =
    (optionOne, optionTwo) match {
      case (None, None)                    => 0
      case (_, None)                       => 1
      case (None, _)                       => -1
      case (Some(current), Some(original)) => current.compare(original)
    }
}
