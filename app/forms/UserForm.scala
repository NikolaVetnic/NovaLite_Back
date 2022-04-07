package forms

import models.User
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object UserForm {
  def create: Form[User] = Form(
    mapping(
      "id" -> longNumber,
      "username" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "password" -> nonEmptyText,
      "imgUrl" -> nonEmptyText,
      "roleId" -> longNumber
    )(User.apply)(User.unapply)
  )
}
