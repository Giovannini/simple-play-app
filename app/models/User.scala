package models

final case class User(id: String, starredProductIds: Set[String])