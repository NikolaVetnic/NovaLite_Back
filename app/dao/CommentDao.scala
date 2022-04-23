package dao

import models.{Comment, CommentInsertDto}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                          (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {


  import profile.api._


  private val comments = TableQuery[CommentTable]


  /**********
   * EXISTS *
   **********/
  def exists(id: Long) : Future[Boolean] =
    db.run(comments.filter(_.id === id).exists.result)


  def existsAndOwnedBy(id: Long, ownerId: Long) : Future[Boolean] =
    db.run(comments.filter(_.id === id).filter(_.ownerId === ownerId).exists.result)


  /*******
   * GET *
   *******/
  def all(): Future[Seq[Comment]] =
    db.run(comments.result)


  def get(id: Long): Future[Option[Comment]] =
    db.run(comments.filter(_.id === id).sortBy(_.id).result.headOption)


  def getByPostId(id: Long): Future[Seq[Comment]] =
    db.run(comments.filter(_.postId === id).sortBy(_.dateTime).result)


  /********
   * POST *
   ********/
  def insert(commentInsertDto: CommentInsertDto): Future[String] = { dbConfig.db.run(
    comments += new Comment(null, commentInsertDto.content, new Timestamp(System.currentTimeMillis()), commentInsertDto.ownerId, commentInsertDto.postId))
      .map(res => "Comment successfully added").recover {
    case ex: Exception => ex.getCause.getMessage
  }}


  /*******
   * PUT *
   *******/
  def update(comment: Comment): Future[Comment] = db.run(
    comments.filter(_.id === comment.id)
      .update(comment.copy(comment.id, comment.content, new Timestamp(System.currentTimeMillis()), comment.ownerId)))
      .map(_ => comment)


  /**********
   * DELETE *
   **********/
  def delete(id: Long): Future[Unit] =
    db.run(comments.filter(_.id === id).delete).map(_ => ())


  def deleteByIdAndOwnerId(id: Long, ownerId: Long): Future[Unit] =
    db.run(comments.filter(_.id === id).filter(_.ownerId === ownerId).delete).map(_ => ())


  private class CommentTable(tag: Tag) extends Table[Comment](tag, "comment") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def dateTime = column[Timestamp]("date_time")
    def ownerId = column[Long]("owner_id")
    def postId = column[Long]("post_id")

    def * = (id, content, dateTime, ownerId, postId) <> (Comment.tupled, Comment.unapply)
  }
}