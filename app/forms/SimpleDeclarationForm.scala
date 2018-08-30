package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import uk.gov.voa.play.form.ConditionalMappings._


case class SimpleDeclarationForm(ducr:String, isConsolidateDucrtoWiderShipment:Boolean, mucr:Option[String]) extends DataFormats{

  def apply(): Form[SimpleDeclarationForm] =
    Form(
      mapping("ducr" -> nonEmptyText.verifying(pattern(correctDucrFormat.r, error="error.ducr")),
        "isConsolidateDucrtoWiderShipment" -> boolean,
        "mucr" -> mandatoryIfTrue("isConsolidateDucrtoWiderShipment",
          nonEmptyText.verifying(pattern("""^[A-Za-z0-9 \-,.&'\/]{1,65}$""".r, error="error.ducr")))
      )(SimpleDeclarationForm.apply)(SimpleDeclarationForm.unapply)
    )

}


trait DataFormats {
  val correctDucrFormat = "^\\d[A-Z]{2}\\d{12}-[0-9A-Z]{1,19}$"

  val mucrFormats: Seq[String] = Seq(
    "^A:[A-Z]{3}[0-9A-Z]{0,8}$",
    "^C:[A-Z]{3}[0-9A-Z]{4}$",
    "^[A-Z]{2}\\/[A-Z]{3}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[A-Z]{4}-[0-9A-Z]{5,}",
    "^[A-Z]{2}\\/[0-9]{12}-[0-9A-Z]{1,}"
  )

}