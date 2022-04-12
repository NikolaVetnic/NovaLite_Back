package models

import play.api.libs.json.{Json, OFormat}

case class Befriends(userId0: Long, userId1: Long, status: Int)

object Befriends {

  def tupled = (Befriends.apply _).tupled

  implicit val format: OFormat[Befriends] = Json.format[Befriends]
}

case class BefriendsDto(userId1: Long)

object BefriendsDto {

  def tupled = (Befriends.apply _).tupled

  implicit val format: OFormat[BefriendsDto] = Json.format[BefriendsDto]
}
