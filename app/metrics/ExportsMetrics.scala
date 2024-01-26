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

package metrics

import com.codahale.metrics.{Counter, Timer}
import com.codahale.metrics.Timer.Context
import uk.gov.hmrc.play.bootstrap.metrics.Metrics
import javax.inject.{Inject, Singleton}
import metrics.MetricIdentifiers._

@Singleton
class ExportsMetrics @Inject() (metrics: Metrics) {

  def timerName(feature: String): String = s"$feature.timer"

  private val timers: Map[String, Timer] = List(submissionMetric, submissionAmendmentMetric, cancellationMetric, tariffCommoditiesMetric)
    .map(feature => feature -> metrics.defaultRegistry.timer(timerName(feature)))
    .toMap

  def counterName(feature: String): String = s"$feature.counter"

  private val counters: Map[String, Counter] = List(submissionMetric, submissionAmendmentMetric, cancellationMetric, tariffCommoditiesMetric)
    .map(feature => feature -> metrics.defaultRegistry.counter(counterName(feature)))
    .toMap

  def startTimer(feature: String): Context = timers(feature).time()

  def incrementCounter(feature: String): Unit = counters(feature).inc()
}

object MetricIdentifiers {

  val submissionMetric = "submission"
  val submissionAmendmentMetric = "submissionAmendment"
  val cancellationMetric = "cancellation"
  val tariffCommoditiesMetric = "commodities"
}
