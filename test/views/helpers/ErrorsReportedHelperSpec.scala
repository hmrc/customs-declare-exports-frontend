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

package views.helpers

import base.Injector
import connectors.CodeListConnector
import forms.common.Eori
import forms.section2.AdditionalActor
import models.Pointer
import models.declaration.notifications.{Notification, NotificationError}
import models.declaration.submissions.SubmissionStatus
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import views.helpers.PointerPatterns.pointerToDucr
import views.html.components.gds.link
import views.common.UnitViewSpec

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import java.time.ZonedDateTime

class ErrorsReportedHelperSpec extends UnitViewSpec with Injector with MockitoSugar with ExportsDeclarationBuilder with ExportsItemBuilder {

  private val linkComp = instanceOf[link]

  implicit val countryHelper: CountryHelper = mock[CountryHelper]
  when(countryHelper.getShortNameForCountryCode(meq("GB"))(any())).thenReturn(Some("United Kingdom"))
  when(countryHelper.getShortNameForCountryCode(meq("ZA"))(any())).thenReturn(Some("South Africa"))

  implicit val codeListConnector: CodeListConnector = mock[CodeListConnector]

  val errorRepHelper = new ErrorsReportedHelper(linkComp, codeListConnector, countryHelper)
  val validationCode = "CDS12056"

  "ErrorsReportedHelper" should {

    "return 0 ErrorInstances" when {

      "no Notification provided" in {
        val errors = errorRepHelper.generateErrorRows(None, aDeclaration(), None, false)
        errors.size mustBe 0
      }

      "a Notification is provided but with no NotificationErrors" in {
        val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, Seq.empty[NotificationError])
        val errors = errorRepHelper.generateErrorRows(Some(notification), aDeclaration(), None, false)
        errors.size mustBe 0
      }
    }

    "return ErrorInstances grouped together by error code" in {
      val notificationErrors = List(
        NotificationError(validationCode, None, None),
        NotificationError(validationCode, None, None),
        NotificationError(validationCode.reverse, None, None)
      )
      val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, notificationErrors)
      val errors = errorRepHelper.generateErrorRows(Some(notification), aDeclaration(), None, false)

      errors.size mustBe 2
      errors.head.errorCode mustBe validationCode
      errors.drop(1).head.errorCode mustBe validationCode.reverse
    }

    "return only the original value" when {
      "No draft value is defined" in {
        val fieldPointer = Pointer("declaration.consignmentReferences.lrn")
        val notificationErrors = List(NotificationError(validationCode, Some(fieldPointer), None))
        val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, notificationErrors)
        val originalDec = aDeclaration(withConsignmentReferences("DUCR", "LRN"))
        val updatedDec = None
        val errors = errorRepHelper.generateErrorRows(Some(notification), originalDec, updatedDec, false)

        errors.size mustBe 1
        errors.head.fieldsInvolved.head.pointer mustBe fieldPointer
        errors.head.fieldsInvolved.head.originalValue mustBe originalDec.consignmentReferences.flatMap(_.lrn.map(_.lrn))
        errors.head.fieldsInvolved.head.draftValue mustBe None
      }

      "draft value is defined but is same as original value" in {
        val fieldPointer = Pointer("declaration.consignmentReferences.lrn")
        val notificationErrors = List(NotificationError(validationCode, Some(fieldPointer), None))
        val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, notificationErrors)
        val originalDec = aDeclaration(withConsignmentReferences("DUCR", "LRN"))
        val updatedDec = Some(aDeclaration(withConsignmentReferences("DUCR2", "LRN")))
        val errors = errorRepHelper.generateErrorRows(Some(notification), originalDec, updatedDec, false)

        errors.size mustBe 1
        errors.head.fieldsInvolved.head.pointer mustBe fieldPointer
        errors.head.fieldsInvolved.head.originalValue mustBe originalDec.consignmentReferences.flatMap(_.lrn.map(_.lrn))
        errors.head.fieldsInvolved.head.draftValue mustBe None
      }
    }

    "return both original and draft value" when {
      "a draft value is defined" in {
        val fieldPointer = Pointer("declaration.consignmentReferences.lrn")
        val notificationErrors = List(NotificationError(validationCode, Some(fieldPointer), None))
        val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, notificationErrors)
        val originalDec = aDeclaration(withConsignmentReferences("DUCR", "LRN"))
        val updatedDec = Some(aDeclaration(withConsignmentReferences("DUCR2", "LRN2")))
        val errors = errorRepHelper.generateErrorRows(Some(notification), originalDec, updatedDec, false)

        errors.size mustBe 1
        errors.head.fieldsInvolved.head.pointer mustBe fieldPointer
        errors.head.fieldsInvolved.head.originalValue mustBe originalDec.consignmentReferences.flatMap(_.lrn.map(_.lrn))
        errors.head.fieldsInvolved.head.draftValue mustBe updatedDec.flatMap(_.consignmentReferences.flatMap(_.lrn.map(_.lrn)))
      }
    }

    "return a count of the number of Additional Parties as the values" when {
      "Additional Parties is the field in error" in {
        val fieldPointer = Pointer("declaration.parties.additionalActors")
        val notificationErrors = List(NotificationError(validationCode, Some(fieldPointer), None))
        val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, notificationErrors)
        val originalDec = aDeclaration(withAdditionalActors(AdditionalActor(Some(Eori("eori")), Some("partyType"))))
        val updatedDec = Some(
          aDeclaration(
            withAdditionalActors(AdditionalActor(Some(Eori("eori")), Some("partyType")), AdditionalActor(Some(Eori("eori2")), Some("partyType2")))
          )
        )
        val errors = errorRepHelper.generateErrorRows(Some(notification), originalDec, updatedDec, false)

        errors.size mustBe 1
        errors.head.fieldsInvolved.head.pointer mustBe fieldPointer
        errors.head.fieldsInvolved.head.originalValue mustBe Some("1")
        errors.head.fieldsInvolved.head.draftValue mustBe Some("2")
      }
    }

    "return short country names for the values" when {
      "a field that has a cached country code is in error" in {
        val fieldPointer = Pointer("declaration.transport.transportCrossingTheBorderNationality.countryCode")
        val notificationErrors = List(NotificationError(validationCode, Some(fieldPointer), None))
        val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, notificationErrors)
        val originalDec = aDeclaration(withTransportCountry(Some("ZA")))
        val updatedDec = Some(aDeclaration(withTransportCountry(Some("GB"))))
        val errors = errorRepHelper.generateErrorRows(Some(notification), originalDec, updatedDec, false)

        errors.size mustBe 1
        errors.head.fieldsInvolved.head.pointer mustBe fieldPointer
        errors.head.fieldsInvolved.head.originalValue mustBe Some("South Africa")
        errors.head.fieldsInvolved.head.draftValue mustBe Some("United Kingdom")
      }
    }

    "return the expected ErrorInstance on error code CDS12062" in {
      val errorCode = "CDS12062"
      val notificationErrors = List(NotificationError(errorCode, None, None))
      val notification = Notification("actionId", "mrn", ZonedDateTime.now(), SubmissionStatus.ACCEPTED, notificationErrors)
      val errors = errorRepHelper.generateErrorRows(Some(notification), aDeclaration(withConsignmentReferences()), None, false)

      errors.size mustBe 1
      errors.head.errorCode mustBe errorCode

      errors.head.fieldsInvolved.size mustBe 1

      val fieldInvolved = errors.head.fieldsInvolved.head
      fieldInvolved.pointer.sections.head.value mustBe pointerToDucr
      fieldInvolved.originalValue.value mustBe DUCR

      val document = Jsoup.parse(fieldInvolved.changeLink.value.toString)
      val link = document.body.getElementById("item-header-action")
      val href = URLDecoder.decode(link.attr("href"), UTF_8)
      assert(href.endsWith("/ducr-entry"))
    }
  }
}
