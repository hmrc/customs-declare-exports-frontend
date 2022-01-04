/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.actions

import models.requests.{ItemRequest, JourneyRequest}
import play.api.mvc.{ActionRefiner, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ItemAction(itemId: String)(implicit val executionContext: ExecutionContext) extends ActionRefiner[JourneyRequest, ItemRequest] {

  import play.api.mvc.Results._

  private val itemsController = controllers.declaration.routes.ItemsSummaryController

  override protected def refine[A](request: JourneyRequest[A]): Future[Either[Result, ItemRequest[A]]] =
    Future.successful {
      request.cacheModel
        .itemBy(itemId)
        .map(item => Right(new ItemRequest[A](item, request)))
        .getOrElse(Left(Redirect(itemsController.displayItemsSummaryPage())))
    }
}

class ItemActionBuilder @Inject()(authorized: AuthAction, journeyAction: JourneyAction)(implicit val executionContext: ExecutionContext) {

  def apply(itemId: String) = authorized andThen journeyAction andThen ItemAction(itemId)
}
