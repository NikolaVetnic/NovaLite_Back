package forms

import models.{Befriends, PostReaction}
import play.api.data.Form
import play.api.data.Forms.{mapping, _}

object BefriendsForm {
  def create: Form[Befriends] = Form(
    mapping(
      "userId0" -> longNumber,
      "userId1" -> longNumber,
      "status" -> number
    )(Befriends.apply)(Befriends.unapply)
  )
}
