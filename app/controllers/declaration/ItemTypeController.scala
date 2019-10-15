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
import controllers.navigation.Navigator
import controllers.util._
import forms.declaration.ItemTypeForm
import forms.declaration.ItemTypeForm._
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.ItemType
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.validators.forms.supplementary.ItemTypeValidator
import utils.validators.forms.{Invalid, Valid}
import views.html.declaration.item_type

import scala.concurrent.{ExecutionContext, Future}

class ItemTypeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  itemTypePage: item_type
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel
      .itemBy(itemId)
      .map { item =>
        item.itemType match {
          case Some(itemType) =>
            Ok(
              itemTypePage(
                mode,
                itemId,
                ItemTypeForm.form().fill(ItemTypeForm.convert(itemType)),
                item.hasFiscalReferences,
                itemType.taricAdditionalCodes,
                itemType.nationalAdditionalCodes
              )
            )
          case None =>
            Ok(itemTypePage(mode, itemId, ItemTypeForm.form(), item.hasFiscalReferences))
        }
      }
      .getOrElse(Redirect(routes.ItemsSummaryController.displayPage()))
  }

  def submitItemType(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val inputForm = ItemTypeForm.form().bindFromRequest()
    val itemTypeInput: ItemTypeForm = inputForm.value.getOrElse(ItemTypeForm.empty)

    request.cacheModel
      .itemBy(itemId)
      .map { item =>
        val itemTypeCache = item.itemType.getOrElse(ItemType.empty)
        val hasFiscalReferences = item.hasFiscalReferences
        val formAction = FormAction.bindFromRequest()
        formAction match {
          case Add(field) =>
            handleAddition(mode, itemId, field, itemTypeInput, itemTypeCache, hasFiscalReferences)
          case SaveAndContinue | SaveAndReturn =>
            handleSaveAndContinue(mode, itemId, itemTypeInput, itemTypeCache, hasFiscalReferences)
          case Remove(keys) =>
            handleRemoval(mode, itemId, keys, itemTypeInput, itemTypeCache, hasFiscalReferences)
          case _ =>
            errorHandler.displayErrorPage()
        }
      }
      .getOrElse(errorHandler.displayErrorPage())
  }

  private def handleAddition(
    mode: Mode,
    itemId: String,
    field: Option[String],
    itemTypeInput: ItemTypeForm,
    itemTypeCache: ItemType,
    hasFiscalReferences: Boolean
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {

    val itemTypeUpdated = updateCachedItemType(itemTypeInput, itemTypeCache, field)

    ItemTypeValidator.validateOnAddition(itemTypeUpdated) match {
      case Valid =>
        updateExportsCache(itemId, itemTypeUpdated).map {
          case Some(model) =>
            refreshPage(mode, itemId, itemTypeInputForAdd(field, itemTypeInput), model)
          case None =>
            Redirect(routes.ItemsSummaryController.displayPage(mode))
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemTypeForm.form().fill(itemTypeInput))((form, error) => form.withError(error))
        Future.successful(
          BadRequest(
            itemTypePage(mode, itemId, formWithErrors, hasFiscalReferences, itemTypeCache.taricAdditionalCodes, itemTypeCache.nationalAdditionalCodes)
          )
        )
    }
  }

  private def itemTypeInputForAdd(field: Option[String], itemTypeInput: ItemTypeForm) = field match {
    case Some(`taricAdditionalCodeKey`) =>
      itemTypeInput.copy(taricAdditionalCode = None)
    case Some(`nationalAdditionalCodeKey`) =>
      itemTypeInput.copy(nationalAdditionalCode = None)
    case _ => itemTypeInput
  }

  private def handleSaveAndContinue(mode: Mode, itemId: String, itemTypeInput: ItemTypeForm, itemTypeCache: ItemType, hasFiscalReferences: Boolean)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val itemTypeUpdated = updateCachedItemType(itemTypeInput, itemTypeCache, None)
    ItemTypeValidator.validateOnSaveAndContinue(itemTypeUpdated) match {
      case Valid =>
        updateExportsCache(itemId, itemTypeUpdated).map { _ =>
          navigator.continueTo(controllers.declaration.routes.PackageInformationController.displayPage(mode, itemId))
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemTypeForm.form().fill(itemTypeInput))((form, error) => form.withError(error))
        Future.successful(
          BadRequest(
            itemTypePage(mode, itemId, formWithErrors, hasFiscalReferences, itemTypeCache.taricAdditionalCodes, itemTypeCache.nationalAdditionalCodes)
          )
        )
    }
  }

  private def updateCachedItemType(itemTypeInput: ItemTypeForm, itemTypeCache: ItemType, addField: Option[String]): ItemType = {

    val updatedTaricCodes = addField match {
      case Some(`taricAdditionalCodeKey`) | None =>
        itemTypeInput.taricAdditionalCode.map(code => itemTypeCache.taricAdditionalCodes :+ code).getOrElse(itemTypeCache.taricAdditionalCodes)
      case _ => itemTypeCache.taricAdditionalCodes
    }

    val updatedNationalCodes = addField match {
      case Some(`nationalAdditionalCodeKey`) | None =>
        itemTypeInput.nationalAdditionalCode
          .map(code => itemTypeCache.nationalAdditionalCodes :+ code)
          .getOrElse(itemTypeCache.nationalAdditionalCodes)
      case _ => itemTypeCache.nationalAdditionalCodes
    }

    ItemType(
      combinedNomenclatureCode = itemTypeInput.combinedNomenclatureCode,
      taricAdditionalCodes = updatedTaricCodes,
      nationalAdditionalCodes = updatedNationalCodes,
      descriptionOfGoods = itemTypeInput.descriptionOfGoods,
      cusCode = itemTypeInput.cusCode,
      unDangerousGoodsCode = itemTypeInput.unDangerousGoodsCode,
      statisticalValue = itemTypeInput.statisticalValue
    )
  }

  private def handleRemoval(
    mode: Mode,
    itemId: String,
    keys: Seq[String],
    itemTypeInput: ItemTypeForm,
    itemTypeCached: ItemType,
    hasFiscalReferences: Boolean
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {
    val key = keys.headOption.getOrElse("")
    val label = Label(key)

    val itemTypeUpdated = label.name match {
      case `taricAdditionalCodeKey` =>
        itemTypeCached.copy(taricAdditionalCodes = removeElement(itemTypeCached.taricAdditionalCodes, label.value))
      case `nationalAdditionalCodeKey` =>
        itemTypeCached.copy(nationalAdditionalCodes = removeElement(itemTypeCached.nationalAdditionalCodes, label.value))
    }
    updateExportsCache(itemId, itemTypeUpdated).map {
      case Some(model) =>
        refreshPage(mode, itemId, itemTypeInput, model)
      case None =>
        Redirect(routes.ItemsSummaryController.displayPage(mode))
    }
  }

  private def removeElement(collection: Seq[String], valueToRemove: String): Seq[String] =
    MultipleItemsHelper.remove(collection, (_: String) == valueToRemove)

  private def refreshPage(mode: Mode, itemId: String, itemTypeInput: ItemTypeForm, model: ExportsDeclaration)(
    implicit request: JourneyRequest[AnyContent]
  ): Result =
    model
      .itemBy(itemId)
      .map { item =>
        item.itemType match {
          case Some(cachedData) =>
            Ok(
              itemTypePage(
                mode,
                item.id,
                ItemTypeForm.form().fill(itemTypeInput),
                item.hasFiscalReferences,
                cachedData.taricAdditionalCodes,
                cachedData.nationalAdditionalCodes
              )
            )
          case _ =>
            Ok(itemTypePage(mode, itemId, ItemTypeForm.form(), item.hasFiscalReferences))
        }
      }
      .getOrElse(Redirect(routes.ItemsSummaryController.displayPage(mode)))

  private case class Label(name: String, value: String)
  private object Label {

    def apply(str: String): Label = {
      val Array(name, value) = str.split("_")
      Label(name, value)
    }
  }

  private def updateExportsCache(itemId: String, updatedItem: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSync(model => {
      val itemList = model.items
        .find(item => item.id.equals(itemId))
        .map(_.copy(itemType = Some(updatedItem)))
        .fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)

      Some(model.copy(items = itemList))
    })
}
