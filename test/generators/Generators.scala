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

package generators

import forms.supplementary.{CommodityMeasure, PackageInformation}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen, Shrink}

trait Generators {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield {
      seq1.toSeq.zip(seq2).foldRight("") {
        case ((n, Some(v)), m) =>
          m + n + v
        case ((n, _), m) =>
          m + n
      }
    }
  }

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max)
    genIntersperseString(numberGen.toString, ",")
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x > Int.MaxValue)

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x < Int.MinValue)

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map(_.formatted("%f"))

  def intGreaterThan(min: Int): Gen[Int] =
    choose(min + 1, Int.MaxValue)

  def intLessThan(max: Int): Gen[Int] =
    choose(Int.MinValue, max - 1)

  def intBetweenRange(min: Int, max: Int): Gen[Int] =
    choose(min, max)

  def intOutsideRange(min: Int, max: Int): Gen[Int] =
    oneOf(intLessThan(min), intGreaterThan(max))

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsLongerThan(minLength: Int): Gen[String] =
    arbitrary[String] suchThat (_.length > minLength)

  def stringsExceptSpecificValues(excluded: Set[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  implicit val arbitraryPackaging: Arbitrary[PackageInformation] = Arbitrary {
    for {
      noOfPackages <- option(choose[Int](1, 99999))
      typeOfPackages <- option(alphaNumStr.map(_.take(2)))
      marksNumbersId <- alphaNumStr.map(_.take(40))
      if typeOfPackages.exists(_.size == 2) && marksNumbersId.size > 0
    } yield PackageInformation(typeOfPackages, noOfPackages, Some(marksNumbersId))
  }

  implicit val arbitraryCommodityMeasure: Arbitrary[CommodityMeasure] = Arbitrary {
    for {
      supplementaryUnits <- posDecimal(16, 2)
      netMass <- posDecimal(11, 3)
      grossMass <- posDecimal(16, 2)
    } yield CommodityMeasure(Some(supplementaryUnits.toString), netMass.toString, grossMass.toString)
  }
  implicit val arbitraryPackagingSeq = listOfN[PackageInformation](5, arbitraryPackaging.arbitrary)
  implicit val arbitraryPackagingMaxSeq = listOfN[PackageInformation](99, arbitraryPackaging.arbitrary)

  def caseClassToSeq(cc: AnyRef) =
    (Map[String, String]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc).toString)
    }.toSeq

  def minStringLength(length: Int): Gen[String] =
    for {
      i <- choose(length, length + 500)
      n <- listOfN(i, arbitrary[Char])
    } yield n.mkString

  def numStrLongerThan(minLength: Int): Gen[String] =
    numStr suchThat (_.length > minLength)

  implicit val chooseBigInt: Choose[BigInt] =
    Choose.xmap[Long, BigInt](BigInt(_), _.toLong)

  def decimal(minSize: Int, maxSize: Int, scale: Int): Gen[BigDecimal] = {
    val min = if (minSize <= 0) BigInt(0) else BigInt("1" + ("0" * (minSize - 1)))
    choose[BigInt](min, BigInt("9" * maxSize)).map(BigDecimal(_, scale))
  }
  def posDecimal(precision: Int, scale: Int): Gen[BigDecimal] =
    decimal(0, precision, scale)
}
