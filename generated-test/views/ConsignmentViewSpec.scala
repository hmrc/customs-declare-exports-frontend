package views

import play.api.data.Form
import forms.ConsignmentFormProvider
import models.NormalMode
import models.Consignment
import views.behaviours.ViewBehaviours
import views.html.consignment

class ConsignmentViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "consignment"

  val form = new ConsignmentFormProvider()()

  def createView = () => consignment(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => consignment(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "Consignment view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "Consignment view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- Consignment.options) {
          assertContainsRadioButton(doc, option.id, "value", option.value, false)
        }
      }
    }

    for(option <- Consignment.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, option.id, "value", option.value, true)

          for(unselectedOption <- Consignment.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
