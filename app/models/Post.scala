package models

import play.api.libs.json._

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime

// Post entity
case class Post(id: Option[Long], title: String, content: String, dateTime: Timestamp, ownerId: Long)


object Post {

  def tupled = (Post.apply _).tupled

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

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


case class PostDisplayDto(id: Option[Long], title: String, content: String, dateTime: LocalDateTime, owner: User, numLikes: Long)

object PostDisplayDto {

  def tupled = (Post.apply _).tupled

  implicit val format: OFormat[PostDisplayDto] = Json.format[PostDisplayDto]
}