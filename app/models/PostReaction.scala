package models

import play.api.libs.json.{Json, OFormat}

case class PostReaction(userId: Long, postId: Long, reactionId: Long)

object PostReaction {

  def tupled = (PostReaction.apply _).tupled

  implicit val format: OFormat[PostReaction] = Json.format[PostReaction]
}