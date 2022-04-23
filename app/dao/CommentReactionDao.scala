package dao

import models.CommentReaction
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentReactionDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                  (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {


  import profile.api._


  private val commentReactions = TableQuery[CommentReactionTable]


  /**********
   * EXISTS *
   **********/
  def exists(userId: Long, commentId: Long) : Future[Boolean] =
    db.run(commentReactions.filter(_.userId === userId).filter(_.commentId === commentId).exists.result)


  /*******
   * GET *
   *******/
  def getByCommentId(id: Long): Future[Seq[CommentReaction]] =
    db.run(commentReactions.filter(_.commentId === id).filter(_.reactionId === 1.toLong).result)


  /********
   * POST *
   ********/
  def insert(commentReaction: CommentReaction): Future[String] =
    dbConfig.db.run(commentReactions += commentReaction).map(res => "Comment reaction successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }


  /**********
   * DELETE *
   **********/
  def delete(userId: Long, commentId: Long): Future[Unit] =
    db.run(commentReactions.filter(_.userId === userId).filter(_.commentId === commentId).delete).map(_ => ())


  def deleteIndiscriminately(commentId: Long): Future[Unit] =
    db.run(commentReactions.filter(_.commentId === commentId).delete).map(_ => ())


  private class CommentReactionTable(tag: Tag) extends Table[CommentReaction](tag, "comment_reaction") {

    def userId = column[Long]("user_id")
    def commentId = column[Long]("comment_id")
    def reactionId = column[Long]("reaction_id")

    def id = primaryKey("reaction_pk", (commentId, userId))

    def * = (userId, commentId, reactionId) <> (CommentReaction.tupled, CommentReaction.unapply)
  }
}
