package forms

import models.{Post, PostInputDto, PostInsertDto, PostUpdateDto}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object PostForm {
  def create: Form[Post] = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "dateTime" -> sqlTimestamp,
      "ownerId" -> longNumber
    )(Post.apply)(Post.unapply)
  )
}

object PostInsertDtoForm {
  def create: Form[PostInsertDto] = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "ownerId" -> longNumber
    )(PostInsertDto.apply)(PostInsertDto.unapply)
  )
}

object PostInputDtoForm {
  def create: Form[PostInputDto] = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText
    )(PostInputDto.apply)(PostInputDto.unapply)
  )
}

object PostUpdateDtoForm {
  def create: Form[PostUpdateDto] = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "content" -> nonEmptyText
    )(PostUpdateDto.apply)(PostUpdateDto.unapply)
  )
}