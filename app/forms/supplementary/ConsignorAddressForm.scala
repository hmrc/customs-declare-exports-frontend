package forms.supplementary

case class ConsignorAddressForm(
  eori: String, // alphanumeric, max length 17 characters
  fullName: String, // alphanumeric length 1 - 70
  address: String, // alphanumeric length 1 - 70
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // two characters
)
