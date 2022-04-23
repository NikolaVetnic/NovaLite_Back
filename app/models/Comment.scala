package models

import play.api.libs.json._

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime

// Comment entity
case class Comment(id: Option[Long], content: String, dateTime: Timestamp, ownerId: Long, postId: Long)


object Comment {

  def tupled = (Comment.apply _).tupled

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val format: OFormat[Comment] = Json.format[Comment]
}


// DTO used for persisting Comments
case class CommentInsertDto(content: String, ownerId: Long, postId: Long)

object CommentInsertDto {

  def tupled= (CommentInsertDto.apply _).tupled

  implicit val format: OFormat[CommentInsertDto] = Json.format[CommentInsertDto]
}


// DTO used for inputting Comments (CommentInputDto -> CommentInsertDto), ownerId taken from current user
case class CommentInputDto(content: String, postId: Long)

object CommentInputDto {

  def tupled= (Post.apply _).tupled

  implicit val format: OFormat[CommentInputDto] = Json.format[CommentInputDto]
}


// DTO used for updating Comments
case class CommentUpdateDto(id: Option[Long], content: String, ownerId: Long, postId: Long)

object CommentUpdateDto {

  def tupled= (CommentUpdateDto.apply _).tupled

  implicit val format: OFormat[CommentUpdateDto] = Json.format[CommentUpdateDto]
}


// DTO used for displaying Comments on the frontend
case class CommentDisplayDto(id: Long, content: String, dateTime: LocalDateTime, owner: User, postId: Long, numLikes: Long)

object CommentDisplayDto {

  def tupled= (CommentDisplayDto.apply _).tupled

  implicit val format: OFormat[CommentDisplayDto] = Json.format[CommentDisplayDto]
}