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

import forms.declaration._
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen, Shrink}
import services.Countries.allCountries
import services.PackageTypes

trait Generators {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  def intGreaterThan(min: Int): Gen[Int] =
    choose(min + 1, Int.MaxValue)

  def intLessThan(max: Int): Gen[Int] =
    choose(Int.MinValue, max - 1)

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsLongerThan(minLength: Int): Gen[String] =
    arbitrary[String] suchThat (_.length > minLength)

  implicit val arbitraryPackaging: Arbitrary[PackageInformation] = Arbitrary {
    for {
      noOfPackages <- option(choose[Int](1, 99999))
      typeOfPackages <- option(oneOf(PackageTypes.all.map(_.code)))
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

  def transportCodesGen: Gen[String] = oneOf(TransportCodes.allowedModeOfTransportCodes.toSeq)
  def transportTypeCodesGen: Gen[String] = oneOf(TransportCodes.allowedMeansOfTransportTypeCodes.toSeq)
  def countryCodesGen: Gen[String] = oneOf(allCountries.map(_.countryName))
  def paymentMethodsGen: Gen[String] = oneOf(TransportCodes.paymentMethods.keySet.toSeq)

  implicit val borderTransportArbitrary: Arbitrary[BorderTransport] = Arbitrary {
    for {
      borderModeOfTransportCode <- transportCodesGen
      meansOfTransportOnDepartureType <- transportTypeCodesGen
      meansOfTransportOnDepartureIDNumber <- option(alphaNumStr.suchThat(_.nonEmpty).map(_.take(25)))
    } yield
      BorderTransport(borderModeOfTransportCode, meansOfTransportOnDepartureType, meansOfTransportOnDepartureIDNumber)
  }
  implicit val transportDetailsArbitrary: Arbitrary[TransportDetails] = Arbitrary {
    for {
      code <- countryCodesGen
      hasContainer <- Arbitrary(oneOf(true, false)).arbitrary
      meansOfTransportCrossingTheBorderType <- transportTypeCodesGen
      meansOfTransportCrossingTheBorderIDNumber <- option(alphaNumStr.suchThat(_.nonEmpty).map(_.take(25)))
      paymentMethod <- option(paymentMethodsGen)
    } yield
      TransportDetails(
        Some(code),
        hasContainer,
        meansOfTransportCrossingTheBorderType,
        meansOfTransportCrossingTheBorderIDNumber,
        paymentMethod
      )
  }

  implicit val sealArbitrary: Arbitrary[Seal] = Arbitrary {
    for {
      id <- alphaNumStr.suchThat(_.nonEmpty).map((_.take(15)))
    } yield Seal(id)
  }

  def minStringLength(length: Int): Gen[String] =
    for {
      i <- choose(length, length + 500)
      n <- listOfN(i, arbitrary[Char])
    } yield n.mkString

  implicit val chooseBigInt: Choose[BigInt] =
    Choose.xmap[Long, BigInt](BigInt(_), _.toLong)

  def decimal(minSize: Int, maxSize: Int, scale: Int): Gen[BigDecimal] = {
    val min = if (minSize <= 0) BigInt(0) else BigInt("1" + ("0" * (minSize - 1)))
    choose[BigInt](min, BigInt("9" * maxSize)).map(BigDecimal(_, scale))
  }
  def posDecimal(precision: Int, scale: Int): Gen[BigDecimal] =
    decimal(0, precision, scale)
}
