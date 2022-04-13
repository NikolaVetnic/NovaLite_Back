package models

import play.api.libs.json.{Json, OFormat}

import java.sql.Date

// Post entity
case class Post(id: Option[Long], title: String, content: String, dateTime: Date, ownerId: Long)

object Post {

  def tupled = (Post.apply _).tupled

  implicit val format: OFormat[Post] = Json.format[Post]
}

// DTO used for persisting Posts
case class PostInsertDto(title: String, content: String, ownerId: Long)

object PostInsertDto {

  def tupled= (Post.apply _).tupled

  implicit val format: OFormat[PostInsertDto] = Json.format[PostInsertDto]
}

// DTO used for inputting Posts (PostInputDto -> PostInsertDto), ownerId taken from current user
case class PostInputDto(title: String, content: String)

object PostInputDto {

  def tupled= (Post.apply _).tupled

  implicit val format: OFormat[PostInsertDto] = Json.format[PostInsertDto]
}

case class PostUpdateDto(id: Option[Long], title: String, content: String)

object PostUpdateDto {

  def tupled = (Post.apply _).tupled

  implicit val format: OFormat[PostUpdateDto] = Json.format[PostUpdateDto]
}