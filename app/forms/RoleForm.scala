package forms

import models.Role
import play.api.data.Form
import play.api.data.Forms.{mapping, _}

object RoleForm {
  def create: Form[Role] = Form(
    mapping(
      "id" -> optional(longNumber),
      "role" -> nonEmptyText
    )(Role.apply)(Role.unapply)
  )
}
