package forms

import models.Reaction
import play.api.data.Form
import play.api.data.Forms.{mapping, _}

object ReactionForm {
  def create: Form[Reaction] = Form(
    mapping(
      "id" -> optional(longNumber),
      "reaction" -> nonEmptyText
    )(Reaction.apply)(Reaction.unapply)
  )
}
