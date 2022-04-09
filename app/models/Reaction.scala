package models

import play.api.libs.json.{Json, OFormat}

case class Reaction(id: Option[Long], reaction: String)

object Reaction {

  def tupled = (Reaction.apply _).tupled

  implicit val format: OFormat[Reaction] = Json.format[Reaction]
}
