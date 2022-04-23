package forms

import models.{Comment, CommentInputDto, CommentInsertDto, CommentUpdateDto}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object CommentForm {
  def create: Form[Comment] = Form(
    mapping(
      "id" -> optional(longNumber),
      "content" -> nonEmptyText,
      "dateTime" -> sqlTimestamp,
      "ownerId" -> longNumber,
      "postId" -> longNumber
    )(Comment.apply)(Comment.unapply)
  )
}

object CommentInsertDtoForm {
  def create: Form[CommentInsertDto] = Form(
    mapping(
      "content" -> nonEmptyText,
      "ownerId" -> longNumber,
      "postId" -> longNumber
    )(CommentInsertDto.apply)(CommentInsertDto.unapply)
  )
}

object CommentInputDtoForm {
  def create: Form[CommentInputDto] = Form(
    mapping(
      "content" -> nonEmptyText,
      "postId" -> longNumber
    )(CommentInputDto.apply)(CommentInputDto.unapply)
  )
}

object CommentUpdateDtoForm {
  def create: Form[CommentUpdateDto] = Form(
    mapping(
      "id" -> optional(longNumber),
      "content" -> nonEmptyText,
      "ownerId" -> longNumber,
      "postId" -> longNumber
    )(CommentUpdateDto.apply)(CommentUpdateDto.unapply)
  )
}