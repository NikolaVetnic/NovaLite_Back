package forms

import models.{Post, PostDto}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object PostForm {
  def create: Form[Post] = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "dateTime" -> sqlDate,
      "ownerId" -> longNumber
    )(Post.apply)(Post.unapply)
  )
}

object PostDtoForm {
  def create: Form[PostDto] = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "ownerId" -> longNumber
    )(PostDto.apply)(PostDto.unapply)
  )
}
