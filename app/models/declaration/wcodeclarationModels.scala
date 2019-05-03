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

package models.declaration

case class BorderTransportMeans(
  name: Option[String] = None, // max 35 chars,
  id: Option[String] = None, // max 35 chars,
  identificationTypeCode: Option[String] = None, // max 17 chars
  typeCode: Option[String] = None, // max 4 chars
  registrationNationalityCode: Option[String] = None, // 2 chars [a-zA-Z] when present; presumably ISO 3166-1 alpha2
  modeCode: Option[Int] = None
) // 0-9
