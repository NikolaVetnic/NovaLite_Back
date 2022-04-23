package forms

import models.CommentReaction
import play.api.data.Form
import play.api.data.Forms.{mapping, _}

object CommentReactionForm {
  def create: Form[CommentReaction] = Form(
    mapping(
      "userId" -> longNumber,
      "commentId" -> longNumber,
      "reactionId" -> longNumber
    )(CommentReaction.apply)(CommentReaction.unapply)
  )
}
