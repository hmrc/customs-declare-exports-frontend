package forms

import forms.behaviours.OptionFieldBehaviours
import models.Consignment
import play.api.data.FormError

class ConsignmentFormProviderSpec extends OptionFieldBehaviours {

  val form = new ConsignmentFormProvider()()

  ".value" must {

    val fieldName = "value"
    val requiredKey = "consignment.error.required"

    behave like optionsField[Consignment](
      form,
      fieldName,
      validValues  = Consignment.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
