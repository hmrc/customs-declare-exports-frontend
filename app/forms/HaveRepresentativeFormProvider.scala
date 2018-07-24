/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import connectors.DataCacheConnector
import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import models.HaveRepresentative
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class HaveRepresentativeFormProvider @Inject()(dataCacheConnector: DataCacheConnector)
  extends FormErrorHelper with Mappings {

  def apply(): Form[HaveRepresentative] =
    Form(
      "value" -> enumerable[HaveRepresentative]("haveRepresentative.error.required")
    )

  def clearRepresentativeCache(cacheMap: CacheMap): Future[CacheMap] =
    dataCacheConnector.removeAndRetrieveEntries(cacheMap, Seq("enterEORI", "representativesAddress"))
}
