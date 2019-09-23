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

package connectors

import config.AppConfig
import connectors.exchange.ExportsDeclarationExchange
import forms.CancelDeclaration
import javax.inject.{Inject, Singleton}
import models._
import models.declaration.notifications.Notification
import models.declaration.submissions.Submission
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDeclareExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {
  private val logger = Logger(this.getClass)

  private def logPayload[T](prefix: String, payload: T)(implicit wts: Writes[T]): T = {
    logger.debug(s"$prefix: ${Json.toJson(payload)}")
    payload
  }

  def deleteDraftDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    httpClient
      .DELETE(s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}/$id")
      .map(_ => ())

  def createDeclaration(
    declaration: ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Create Declaration Request", declaration)
    httpClient
      .POST[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}",
        ExportsDeclarationExchange(declaration)
      )
      .map(logPayload("Create Declaration Response", _))
      .map(_.toExportsDeclaration)
  }

  def updateDeclaration(
    declaration: ExportsDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ExportsDeclaration] = {
    logPayload("Update Declaration Request", declaration)
    httpClient
      .PUT[ExportsDeclarationExchange, ExportsDeclarationExchange](
        s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}/${declaration.id
          .getOrElse(throw new IllegalArgumentException("Cannot update a declaration which hasn't been created first"))}",
        ExportsDeclarationExchange(declaration)
      )
      .map(logPayload("Update Declaration Response", _))
      .map(_.toExportsDeclaration)
  }

  def findDeclarations(
    page: Page
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = Page.bindable.unbind("page", page)
    httpClient
      .GET[Paginated[ExportsDeclarationExchange]](
        s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}?$pagination"
      )
      .map(_.map(_.toExportsDeclaration))
  }

  def findSavedDeclarations(
    page: Page
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Paginated[ExportsDeclaration]] = {
    val pagination = Page.bindable.unbind("page", page)
    val sort = DeclarationSort.bindable.unbind("sort", DeclarationSort(SortBy.UPDATED, SortDirection.DES))
    httpClient
      .GET[Paginated[ExportsDeclarationExchange]](
        s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}?status=DRAFT&$pagination&$sort"
      )
      .map(_.map(_.toExportsDeclaration))
  }

  def findDeclaration(
    id: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ExportsDeclaration]] =
    httpClient
      .GET[Option[ExportsDeclarationExchange]](s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}/$id")
      .map(_.map(_.toExportsDeclaration))

  def submitDeclaration(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Submission] =
    httpClient
      .POSTEmpty[Submission](s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}/$id/submission")

  def findSubmission(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Submission]] =
    httpClient
      .GET[Option[Submission]](s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}/$id/submission")

  def findNotifications(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient
      .GET[Seq[Notification]](
        s"${appConfig.customsDeclareExports}${appConfig.declarationsV2}/$id/submission/notifications"
      )

  def fetchNotifications()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Notification]] =
    httpClient.GET[Seq[Notification]](s"${appConfig.customsDeclareExports}${appConfig.fetchNotifications}")

  def fetchSubmissions()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Submission]] =
    httpClient.GET[Seq[Submission]](s"${appConfig.customsDeclareExports}${appConfig.fetchSubmissions}").map {
      response =>
        logger.debug(s"CUSTOMS_DECLARE_EXPORTS fetch submission response is --> ${response.toString}")
        response
    }

  def createCancellation(
    cancellation: CancelDeclaration
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    logPayload("Create Cancellation Request", cancellation)
    httpClient.POST[CancelDeclaration, HttpResponse](
      s"${appConfig.customsDeclareExports}${appConfig.cancelDeclaration}",
      cancellation
    ) filter (_.status == Status.OK) map (_ => (): Unit)
  }
}
