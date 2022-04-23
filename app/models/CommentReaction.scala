package models

import play.api.libs.json.{Json, OFormat}

case class CommentReaction(userId: Long, commentId: Long, reactionId: Long)

object CommentReaction {

  def tupled = (CommentReaction.apply _).tupled

  implicit val format: OFormat[CommentReaction] = Json.format[CommentReaction]
}