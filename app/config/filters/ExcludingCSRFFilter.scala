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

package config.filters

import akka.util.ByteString
import javax.inject.Inject
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.filters.csrf._

class ExcludingCSRFFilter @Inject()(filter: CSRFFilter) extends EssentialFilter {

  override def apply(nextFilter: EssentialAction): EssentialAction = new EssentialAction {

    override def apply(rh: RequestHeader): Accumulator[ByteString, Result] = {
      val chainedFilter = filter.apply(nextFilter)
      if (rh.tags.getOrElse("ROUTE_COMMENTS", "").contains("NOCSRF")) {
        nextFilter(rh)
      } else {
        chainedFilter(rh)
      }
    }
  }
}
