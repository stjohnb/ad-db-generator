package net.bstjohn.ad.generator.generators

case class Name(first: String, last: String) {
  def fullName: String = s"$first $last"

  def shortName: String = s"${first.charAt(0)}$last"
}
