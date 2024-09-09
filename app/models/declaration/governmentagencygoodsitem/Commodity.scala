/*
 * Copyright 2024 HM Revenue & Customs
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

package models.declaration.governmentagencygoodsitem

case class Commodity(
  description: Option[String] = None,
  classifications: Seq[Classification] = Seq.empty,
  dangerousGoods: Seq[DangerousGoods] = Seq.empty,
  goodsMeasure: Option[GoodsMeasure] = None
)

case class DangerousGoods(undgid: Option[String] = None)

case class GoodsMeasure(grossMassMeasure: Option[Measure] = None, netWeightMeasure: Option[Measure] = None, tariffQuantity: Option[Measure] = None)

case class Measure(unitCode: Option[String] = None, value: Option[BigDecimal] = None)
