package forms

import models.Post
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object PostForm {
  def create: Form[Post] = Form(
    mapping(
      "id" -> longNumber,
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "dateTime" -> sqlDate,
      "ownerId" -> longNumber
    )(Post.apply)(Post.unapply)
  )
}
