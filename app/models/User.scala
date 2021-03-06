package models

import play.api.libs.json.{Json, OFormat}

case class User(id: Option[Long], username: String, firstName: String, lastName: String, password: String, imgUrl: String, roleId: Long)

object User {

  // this is because defining a companion object shadows the case class function tupled
  // see: https://stackoverflow.com/questions/22367092/using-tupled-method-when-companion-object-is-in-class
  def tupled = (User.apply _).tupled

  // provides implicit json mapping
  implicit val format: OFormat[User] = Json.format[User]
}

case class UserDto(firstName: String, lastName: String, imgUrl: String)

object UserDto {

  def tupled = (User.apply _).tupled

  implicit val format: OFormat[UserDto] = Json.format[UserDto]
}