package net.bstjohn.ad.csv

import cats.effect.IO
import net.bstjohn.ad.generator.generators.model.Name
import net.bstjohn.ad.generator.generators.model.NameGenerator.generateName

object AdCsvImportGenerator {

  case class CsvRow(
    firstname: String,
    middleInitial: String,
    lastname: String,
    username: String,
    email: String,
    streetaddress: String,
    city: String,
    zipcode: String,
    state: String,
    country: String,
    description: String,
    office: String,
    department: String,
    password: String,
    telephone: String,
    jobtitle: String,
    company: String,
    ou: String
  ) {
    def toCsvRow: String = {
      s""" "$firstname","$middleInitial","$lastname","$username","$email","$streetaddress","$city","$zipcode","$state","$country","$description","$office","$department","$password","$telephone","$jobtitle","$company","$ou" """
    }
  }

  object CsvRow {
    def apply(name: Name): CsvRow = CsvRow(
      name.first,
      "",
      name.last,
      s"${name.first.charAt(0)}${name.last}",
      s"${name.first.charAt(0)}${name.last}@addc2.local",
      "123 Fake St",
      "Seattle",
      "98101",
      "WA",
      "United States",
      "",
      "C137",
      "Research",
      "Ch@ng3Me!",
      "206-876-5309",
      "",
      "",
      "CN=Users,DC=addc2,DC=local"
    )
  }

  def gen: IO[Unit] = IO.delay {
    (1 to 200).foreach(_ => {
      println(CsvRow(generateName()).toCsvRow)
    })
  }
}
