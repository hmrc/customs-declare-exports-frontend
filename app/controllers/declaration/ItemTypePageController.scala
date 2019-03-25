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

package controllers.declaration

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.goodsItemCacheId
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.ItemType
import forms.declaration.ItemType._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.validators.forms.supplementary.ItemTypeValidator
import utils.validators.forms.{Invalid, Valid}
import views.html.declaration.item_type

import scala.concurrent.{ExecutionContext, Future}

class ItemTypePageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[ItemType](goodsItemCacheId, ItemType.id).map {
      case Some(data) =>
        Ok(item_type(appConfig, ItemType.form.fill(data), data.taricAdditionalCodes, data.nationalAdditionalCodes))
      case _ => Ok(item_type(appConfig, ItemType.form))
    }
  }

  def submitItemType(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val inputForm = ItemType.form.bindFromRequest()
    val itemTypeInput: ItemType = inputForm.value.getOrElse(ItemType.empty)

    customsCacheService.fetchAndGetEntry[ItemType](goodsItemCacheId, ItemType.id).flatMap { itemTypeCacheOpt =>
      val itemTypeCache = itemTypeCacheOpt.getOrElse(ItemType.empty)
      extractActionType() match {
        case Some(Add)             => handleAddition(itemTypeInput, itemTypeCache)
        case Some(SaveAndContinue) => handleSaveAndContinue(itemTypeInput, itemTypeCache)
        case Some(Remove(keys))    => handleRemoval(keys, itemTypeCache)
        case _                     => errorHandler.displayErrorPage()
      }
    }
  }

  private def extractActionType()(implicit request: Request[AnyContent]): Option[FormAction] =
    request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))

  private def handleAddition(itemTypeInput: ItemType, itemTypeCache: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val itemTypeUpdated = updateCachedItemTypeAddition(itemTypeInput, itemTypeCache)
    ItemTypeValidator.validateOnAddition(itemTypeUpdated) match {
      case Valid =>
        customsCacheService.cache[ItemType](goodsItemCacheId, ItemType.id, itemTypeUpdated).map { _ =>
          Redirect(controllers.declaration.routes.ItemTypePageController.displayPage())
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemType.form.fill(itemTypeInput))((form, error) => form.withError(adjustErrorKey(error)))
        Future.successful(
          BadRequest(
            item_type(
              appConfig,
              adjustDataKeys(formWithErrors),
              itemTypeCache.taricAdditionalCodes,
              itemTypeCache.nationalAdditionalCodes
            )
          )
        )
    }
  }

  private def updateCachedItemTypeAddition(itemTypeInput: ItemType, itemTypeCache: ItemType): ItemType =
    itemTypeCache.copy(
      taricAdditionalCodes = itemTypeCache.taricAdditionalCodes ++ itemTypeInput.taricAdditionalCodes,
      nationalAdditionalCodes = itemTypeCache.nationalAdditionalCodes ++ itemTypeInput.nationalAdditionalCodes
    )

  private def handleSaveAndContinue(itemTypeInput: ItemType, itemTypeCache: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val itemTypeUpdated = updateCachedItemTypeSaveAndContinue(itemTypeInput, itemTypeCache)
    ItemTypeValidator.validateOnSaveAndContinue(itemTypeUpdated) match {
      case Valid =>
        customsCacheService.cache[ItemType](goodsItemCacheId, ItemType.id, itemTypeUpdated).map { _ =>
          Redirect(controllers.declaration.routes.PackageInformationController.displayForm())
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemType.form.fill(itemTypeInput))((form, error) => form.withError(adjustErrorKey(error)))
        Future.successful(
          BadRequest(
            item_type(
              appConfig,
              adjustDataKeys(formWithErrors),
              itemTypeCache.taricAdditionalCodes,
              itemTypeCache.nationalAdditionalCodes
            )
          )
        )
    }
  }

  private def updateCachedItemTypeSaveAndContinue(itemTypeInput: ItemType, itemTypeCache: ItemType): ItemType =
    ItemType(
      combinedNomenclatureCode = itemTypeInput.combinedNomenclatureCode,
      taricAdditionalCodes = itemTypeCache.taricAdditionalCodes ++ itemTypeInput.taricAdditionalCodes,
      nationalAdditionalCodes = itemTypeCache.nationalAdditionalCodes ++ itemTypeInput.nationalAdditionalCodes,
      descriptionOfGoods = itemTypeInput.descriptionOfGoods,
      cusCode = itemTypeInput.cusCode,
      statisticalValue = itemTypeInput.statisticalValue
    )

  private def adjustErrorKey(error: FormError): FormError =
    if (error.key.contains(taricAdditionalCodesKey) || error.key.contains(nationalAdditionalCodesKey))
      error.copy(key = error.key.replaceAll("\\[.*?\\]", "").concat("[]"))
    else
      error

  private def adjustDataKeys(form: Form[ItemType]): Form[ItemType] =
    form.copy(data = form.data.map {
      case (key, value) if key.contains(taricAdditionalCodesKey)    => (taricAdditionalCodesKey + "[]", value)
      case (key, value) if key.contains(nationalAdditionalCodesKey) => (nationalAdditionalCodesKey + "[]", value)
      case (key, value)                                             => (key, value)
    })

  private def handleRemoval(keys: Seq[String], itemTypeCached: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    if (keys.nonEmpty) {
      val fieldName = keys.head.split("_")(0)
      val index = keys.head.split("_")(1).toInt

      val itemTypeUpdated = fieldName match {
        case `taricAdditionalCodesKey` =>
          itemTypeCached.copy(taricAdditionalCodes = itemTypeCached.taricAdditionalCodes.patch(index, Nil, 1))
        case `nationalAdditionalCodesKey` =>
          itemTypeCached.copy(nationalAdditionalCodes = itemTypeCached.nationalAdditionalCodes.patch(index, Nil, 1))
      }
      customsCacheService.cache[ItemType](goodsItemCacheId, ItemType.id, itemTypeUpdated).map { _ =>
        Redirect(controllers.declaration.routes.ItemTypePageController.displayPage())
      }
    } else Future.successful(Redirect(controllers.declaration.routes.ItemTypePageController.displayPage()))

}
