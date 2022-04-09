package forms

import models.PostReaction
import play.api.data.Form
import play.api.data.Forms.{mapping, _}

object PostReactionForm {
  def create: Form[PostReaction] = Form(
    mapping(
      "userId" -> longNumber,
      "postId" -> longNumber,
      "reactionId" -> longNumber
    )(PostReaction.apply)(PostReaction.unapply)
  )
}
