package forms

import models.{User, UserDto}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, _}

object UserForm {
  def create: Form[User] = Form(
    mapping(
      "id" -> optional(longNumber),
      "username" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "password" -> nonEmptyText,
      "imgUrl" -> nonEmptyText,
      "roleId" -> longNumber
    )(User.apply)(User.unapply)
  )
}

object UserDtoForm {
  def create: Form[UserDto] = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "imgUrl" -> nonEmptyText
    )(UserDto.apply)(UserDto.unapply)
  )
}