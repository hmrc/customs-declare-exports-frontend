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

package base

import com.codahale.metrics.SharedMetricRegistries
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}

import scala.reflect.ClassTag

class OverridableInjector(overrides: GuiceableModule*) extends Injector {

  /**
    * Clearing shared metrics registries to avoid `A metric named jvm.attribute.vendor already exists` error.
    *
    * It appears very often with places with injector. This is enough solution for this problem.
    *
    * Reference and other solutions: https://github.com/kenshoo/metrics-play/issues/74
    */
  SharedMetricRegistries.clear()

  private val injector = GuiceApplicationBuilder().overrides(overrides: _*).injector()

  override def instanceOf[T <: AnyRef](implicit classTag: ClassTag[T]): T = injector.instanceOf[T]
}
