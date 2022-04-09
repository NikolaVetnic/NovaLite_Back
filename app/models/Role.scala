package models

import play.api.libs.json.{Json, OFormat}

case class Role(id: Option[Long], role: String)

object Role {

  def tupled = (Role.apply _).tupled

  implicit val format: OFormat[Role] = Json.format[Role]
}


