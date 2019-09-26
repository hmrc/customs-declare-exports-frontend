package unit.base

import org.scalatest.enablers.Containing
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError

trait FormSpec extends WordSpec with MustMatchers {
  implicit val formErrorsContaining: Containing[Seq[FormError]] = ErrorListContaining
}

object ErrorListContaining extends Containing[Seq[FormError]] {
  override def contains(container: Seq[FormError], element: Any): Boolean =
    element match {
      case error: FormError   => container.contains(error)
      case messageKey: String => container.exists(_.message == messageKey)
    }

  override def containsOneOf(container: Seq[FormError], elements: Seq[Any]): Boolean = ???

  override def containsNoneOf(container: Seq[FormError], elements: Seq[Any]): Boolean = ???
}
