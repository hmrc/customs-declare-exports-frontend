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

package services.cache

import javax.inject.Singleton
import org.apache.commons.lang3.RandomStringUtils

@Singleton
class ExportItemIdGeneratorService {
  // http://commons.apache.org/proper/commons-lang/javadocs/api-3.6/org/apache/commons/lang3/RandomStringUtils.html
  // no longer deprecated http://commons.apache.org/proper/commons-lang/javadocs/api-3.9/org/apache/commons/lang3/RandomStringUtils.html
  // https://issues.apache.org/jira/browse/LANG-1346
  def generateItemId(): String = RandomStringUtils.random(8, "0123456789abcdefg")
}
