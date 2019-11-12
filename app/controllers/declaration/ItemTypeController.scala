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
import models.declaration.{ExportItem, ItemType}
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
                item,
                ItemTypeForm.form().fill(fromItemType(itemType)),
                itemType.taricAdditionalCodes,
                itemType.nationalAdditionalCodes
              )
            )
          case None =>
            Ok(itemTypePage(mode, item, ItemTypeForm.form()))
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
        val formAction = FormAction.bindFromRequest()
        formAction match {
          case AddField(field) =>
            handleAddition(mode, item, field, itemTypeInput, itemTypeCache)
          case SaveAndContinue | SaveAndReturn =>
            handleSaveAndContinue(mode, item, itemTypeInput, itemTypeCache)
          case Remove(keys) =>
            handleRemoval(mode, item, keys, itemTypeInput, itemTypeCache)
          case _ =>
            errorHandler.displayErrorPage()
        }
      }
      .getOrElse(errorHandler.displayErrorPage())
  }

  private def handleAddition(mode: Mode, item: ExportItem, field: String, itemTypeInput: ItemTypeForm, itemTypeCache: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {

    val itemTypeUpdated = updateCachedItemType(itemTypeInput, itemTypeCache, Some(field))

    ItemTypeValidator.validateOnAddition(itemTypeUpdated) match {
      case Valid =>
        updateExportsCache(item.id, itemTypeUpdated).map {
          case Some(model) =>
            refreshPage(mode, item.id, itemTypeInputForAdd(field, itemTypeInput), model)
          case None =>
            Redirect(routes.ItemsSummaryController.displayPage(mode))
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemTypeForm.form().fill(itemTypeInput))((form, error) => form.withError(error))
        Future.successful(
          BadRequest(itemTypePage(mode, item, formWithErrors, itemTypeCache.taricAdditionalCodes, itemTypeCache.nationalAdditionalCodes))
        )
    }
  }

  private def itemTypeInputForAdd(field: String, itemTypeInput: ItemTypeForm) = field match {
    case `taricAdditionalCodeKey` =>
      itemTypeInput.copy(taricAdditionalCode = None)
    case `nationalAdditionalCodeKey` =>
      itemTypeInput.copy(nationalAdditionalCode = None)
    case _ => itemTypeInput
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
      taricAdditionalCodes = updatedTaricCodes,
      nationalAdditionalCodes = updatedNationalCodes,
      statisticalValue = itemTypeInput.statisticalValue
    )
  }

  private def refreshPage(mode: Mode, itemId: String, itemTypeInput: ItemTypeForm, model: ExportsDeclaration)(
    implicit request: JourneyRequest[AnyContent]
  ): Result =
    model
      .itemBy(itemId)
      .map { item =>
        item.itemType match {
          case Some(cachedData) =>
            Ok(itemTypePage(mode, item, ItemTypeForm.form().fill(itemTypeInput), cachedData.taricAdditionalCodes, cachedData.nationalAdditionalCodes))
          case _ =>
            Ok(itemTypePage(mode, item, ItemTypeForm.form()))
        }
      }
      .getOrElse(Redirect(routes.ItemsSummaryController.displayPage(mode)))

  private def updateExportsCache(itemId: String, updatedItem: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.updatedItem(itemId, _.copy(itemType = Some(updatedItem))))

  private def handleSaveAndContinue(mode: Mode, item: ExportItem, itemTypeInput: ItemTypeForm, itemTypeCache: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val itemTypeUpdated = updateCachedItemType(itemTypeInput, itemTypeCache, None)
    ItemTypeValidator.validateOnSaveAndContinue(itemTypeUpdated) match {
      case Valid =>
        updateExportsCache(item.id, itemTypeUpdated).map { _ =>
          navigator.continueTo(controllers.declaration.routes.PackageInformationController.displayPage(mode, item.id))
        }
      case Invalid(errors) =>
        val formWithErrors =
          errors.foldLeft(ItemTypeForm.form().fill(itemTypeInput))((form, error) => form.withError(error))
        Future.successful(
          BadRequest(itemTypePage(mode, item, formWithErrors, itemTypeCache.taricAdditionalCodes, itemTypeCache.nationalAdditionalCodes))
        )
    }
  }

  private def handleRemoval(mode: Mode, item: ExportItem, keys: Seq[String], itemTypeInput: ItemTypeForm, itemTypeCached: ItemType)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] = {
    val key = keys.headOption.getOrElse("")
    val label = Label(key)

    val itemTypeUpdated = label.name match {
      case `taricAdditionalCodeKey` =>
        itemTypeCached.copy(taricAdditionalCodes = removeElement(itemTypeCached.taricAdditionalCodes, label.value))
      case `nationalAdditionalCodeKey` =>
        itemTypeCached.copy(nationalAdditionalCodes = removeElement(itemTypeCached.nationalAdditionalCodes, label.value))
    }
    updateExportsCache(item.id, itemTypeUpdated).map {
      case Some(model) =>
        refreshPage(mode, item.id, itemTypeInput, model)
      case None =>
        Redirect(routes.ItemsSummaryController.displayPage(mode))
    }
  }

  private def removeElement(collection: Seq[String], valueToRemove: String): Seq[String] =
    MultipleItemsHelper.remove(collection, (_: String) == valueToRemove)

  private case class Label(name: String, value: String)

  private object Label {

    def apply(str: String): Label = {
      val Array(name, value) = str.split("_")
      Label(name, value)
    }
  }
}
