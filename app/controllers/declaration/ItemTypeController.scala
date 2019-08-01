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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.ItemType._
import forms.declaration.{FiscalInformation, ItemType}
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.collections.Removable.RemovableSeq
import utils.validators.forms.supplementary.ItemTypeValidator
import utils.validators.forms.{Invalid, Valid}
import views.html.declaration.item_type

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ItemTypeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  itemTypePage: item_type
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    hasAdditionalFiscalReferencesFor(itemId).map { hasFiscalReferences =>
      request.cacheModel.itemBy(itemId).flatMap(_.itemType) match {
        case Some(itemType) =>
          Ok(
            itemTypePage(
              itemId,
              ItemType.form().fill(itemType),
              hasFiscalReferences,
              itemType.taricAdditionalCodes,
              itemType.nationalAdditionalCodes
            )
          )
        case None =>
          Ok(itemTypePage(itemId, ItemType.form(), hasFiscalReferences))
      }
    }
  }

  def submitItemType(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      val inputForm = ItemType.form().bindFromRequest()
      val itemTypeInput: ItemType = inputForm.value.getOrElse(ItemType.empty)

      exportsCacheService
        .getItemByIdAndSession(itemId, journeySessionId)
        .map(_.flatMap(_.itemType))
        .zip(hasAdditionalFiscalReferencesFor(itemId))
        .flatMap {
          case (itemTypeCacheOpt, hasFiscalReferences) =>
            val itemTypeCache = itemTypeCacheOpt.getOrElse(ItemType.empty)

            FormAction.bindFromRequest() match {
              case Some(Add) => handleAddition(itemId, itemTypeInput, itemTypeCache, hasFiscalReferences)
              case Some(SaveAndContinue) =>
                handleSaveAndContinue(itemId, itemTypeInput, itemTypeCache, hasFiscalReferences)
              case Some(Remove(keys)) => handleRemoval(itemId, keys, itemTypeCache, hasFiscalReferences)
              case _                  => errorHandler.displayErrorPage()
            }
        }
  }

  private def handleAddition(
    itemId: String,
    itemTypeInput: ItemType,
    itemTypeCache: ItemType,
    hasFiscalReferences: Boolean
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val itemTypeUpdated = updateCachedItemTypeAddition(itemId, itemTypeInput, itemTypeCache)
    ItemTypeValidator.validateOnAddition(itemTypeUpdated) match {
      case Valid =>
        updateExportsCache(itemId, journeySessionId, itemTypeUpdated).flatMap { _ =>
          refreshPage(itemId, itemTypeInput, hasFiscalReferences)
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemType.form().fill(itemTypeInput))((form, error) => form.withError(adjustErrorKey(error)))
        Future.successful(
          BadRequest(
            itemTypePage(
              itemId,
              adjustDataKeys(formWithErrors),
              hasFiscalReferences,
              itemTypeCache.taricAdditionalCodes,
              itemTypeCache.nationalAdditionalCodes
            )
          )
        )
    }
  }

  private def updateCachedItemTypeAddition(itemId: String, itemTypeInput: ItemType, itemTypeCache: ItemType): ItemType =
    itemTypeCache.copy(
      taricAdditionalCodes = itemTypeCache.taricAdditionalCodes ++ itemTypeInput.taricAdditionalCodes,
      nationalAdditionalCodes = itemTypeCache.nationalAdditionalCodes ++ itemTypeInput.nationalAdditionalCodes
    )

  private def handleSaveAndContinue(
    itemId: String,
    itemTypeInput: ItemType,
    itemTypeCache: ItemType,
    hasFiscalReferences: Boolean
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val itemTypeUpdated = updateCachedItemTypeSaveAndContinue(itemTypeInput, itemTypeCache)
    ItemTypeValidator.validateOnSaveAndContinue(itemTypeUpdated) match {
      case Valid =>
        updateExportsCache(itemId, journeySessionId, itemTypeUpdated).map { _ =>
          Redirect(controllers.declaration.routes.PackageInformationController.displayPage(itemId))
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemType.form().fill(itemTypeInput))((form, error) => form.withError(adjustErrorKey(error)))
        Future.successful(
          BadRequest(
            itemTypePage(
              itemId,
              adjustDataKeys(formWithErrors),
              hasFiscalReferences,
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
      unDangerousGoodsCode = itemTypeInput.unDangerousGoodsCode,
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

  private def handleRemoval(itemId: String, keys: Seq[String], itemTypeCached: ItemType, hasFiscalReferences: Boolean)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val key = keys.headOption.getOrElse("")
    val label = Label(key)

    val itemTypeUpdated = label.name match {
      case `taricAdditionalCodesKey` =>
        itemTypeCached.copy(taricAdditionalCodes = removeElement(itemTypeCached.taricAdditionalCodes, label.index))
      case `nationalAdditionalCodesKey` =>
        itemTypeCached.copy(
          nationalAdditionalCodes = removeElement(itemTypeCached.nationalAdditionalCodes, label.index)
        )
    }
    updateExportsCache(itemId, journeySessionId, itemTypeUpdated).flatMap { _ =>
      val itemTypeInput: ItemType = ItemType.form().bindFromRequest().value.getOrElse(ItemType.empty)
      refreshPage(itemId, itemTypeInput, hasFiscalReferences)
    }
  }

  private def removeElement(collection: Seq[String], indexToRemove: Int): Seq[String] =
    collection.removeByIdx(indexToRemove)

  private def refreshPage(itemId: String, itemTypeInput: ItemType, hasFiscalReferences: Boolean)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map(_.flatMap(_.itemType)).map {
      case Some(cachedData) =>
        Ok(
          itemTypePage(
            itemId,
            ItemType.form().fill(itemTypeInput),
            hasFiscalReferences,
            cachedData.taricAdditionalCodes,
            cachedData.nationalAdditionalCodes
          )
        )
      case _ =>
        Ok(itemTypePage(itemId, ItemType.form(), hasFiscalReferences))
    }

  private case class Label(name: String, index: Int)
  private object Label {

    def apply(str: String): Label =
      if (isFormatCorrect(str)) {
        val name = str.split("_")(0)
        val idx = str.split("_")(1)
        new Label(name, idx.toInt)
      } else throw new IllegalArgumentException("Data format for removal request is incorrect")

    private def isFormatCorrect(str: String): Boolean = {
      val labelElements = str.split("_")
      (labelElements.length == 2) && Try(labelElements(1).toInt).isSuccess
    }
  }

  private def hasAdditionalFiscalReferencesFor(itemId: String)(implicit request: JourneyRequest[_]): Future[Boolean] =
    exportsCacheService
      .getItemByIdAndSession(itemId, journeySessionId)
      .map(_.flatMap(_.fiscalInformation))
      .map(_.fold(false)(_.onwardSupplyRelief == FiscalInformation.AllowedFiscalInformationAnswers.yes))

  private def updateExportsCache(
    itemId: String,
    sessionId: String,
    updatedItem: ItemType
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val itemList = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(itemType = Some(updatedItem)))
          .fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)

        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
