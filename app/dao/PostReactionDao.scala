package dao

import models.PostReaction
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostReactionDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                               (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {


  import profile.api._


  private val postReactions = TableQuery[PostReactionTable]


  def exists(userId: Long, postId: Long) : Future[Boolean] =
    db.run(postReactions.filter(_.userId === userId).filter(_.postId === postId).exists.result)


  def all() : Future[Seq[PostReaction]] =
    db.run(postReactions.result)


  def getByPostId(id: Long) : Future[Seq[PostReaction]] =
    db.run(postReactions.filter(_.postId === id).result)


  def getByUserId(id: Long) : Future[Seq[PostReaction]] =
    db.run(postReactions.filter(_.userId === id).result)


  def getByUserIdAndPostId(userId: Long, postId: Long) : Future[Option[PostReaction]] =
    db.run(postReactions.filter(_.userId === userId).filter(_.postId === postId).result.headOption)


  def insert(postReaction: PostReaction): Future[String] =
    dbConfig.db.run(postReactions += postReaction).map(res => "Post reaction successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }


  def delete(userId: Long, postId: Long): Future[Unit] =
    db.run(postReactions.filter(_.userId === userId).filter(_.postId === postId).delete).map(_ => ())


  private class PostReactionTable(tag: Tag) extends Table[PostReaction](tag, "post_reaction") {

    def userId = column[Long]("user_id")
    def postId = column[Long]("post_id")
    def reactionId = column[Long]("reaction_id")

    def id = primaryKey("reaction_pk", (postId, userId))

    def * = (userId, postId, reactionId) <> (PostReaction.tupled, PostReaction.unapply)
  }
}
