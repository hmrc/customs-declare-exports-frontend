package controllers.declaration

import models.requests.JourneyRequest
import play.api.data.Form

trait SubmissionErrors {

  class SubmissionForm[A](form: Form[A]) {
    def withSubmissionErrors()(implicit request: JourneyRequest[_]): Form[A] = form.copy(errors = request.submissionErrors)
  }

  implicit def formToForm[A](form: Form[A]): SubmissionForm[A] = new SubmissionForm[A](form)
}
