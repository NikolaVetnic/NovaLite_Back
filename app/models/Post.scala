package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Date

case class Post(id: Option[Long], title: String, content: String, dateTime: Date, ownerId: Long)

object Post {

  def tupled = (Post.apply _).tupled

  implicit val format: OFormat[Post] = Json.format[Post]
}