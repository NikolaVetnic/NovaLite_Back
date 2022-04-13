package dao

import models.{Post, PostInsertDto}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.sql.Date
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {


  import profile.api._


  private val posts = TableQuery[PostTable]


  def exists(id: Long) : Future[Boolean] =
    db.run(posts.filter(i => i.id === id).exists.result)


  def existsAndOwnedBy(id: Long, ownerId: Long) : Future[Boolean] =
    db.run(posts.filter(_.id === id).filter(_.ownerId === ownerId).exists.result)


  def all(): Future[Seq[Post]] =
    db.run(posts.result)


  def get(id: Long): Future[Option[Post]] =
    db.run(posts.filter(_.id === id).sortBy(_.id).result.headOption)


  def getByOwnerId(ownerId: Long): Future[Seq[Post]] =
    db.run(posts.filter(_.ownerId === ownerId).result)


  def getByIdAndOwnerId(id: Long, ownerId: Long): Future[Seq[Post]] =
    db.run(posts.filter(_.id === id).filter(_.ownerId === ownerId).result)


  def getByTitleContentAndOwnerId(title: String, content: String, ownerId: Long): Future[Seq[Post]] =
    db.run(posts.filter(_.title === title).filter(_.content === content).filter(_.ownerId === ownerId).sortBy(_.id).result)


  def insert(postInsertDto: PostInsertDto): Future[String] = dbConfig.db.run(
    posts += new Post(null, postInsertDto.title, postInsertDto.content, new Date(System.currentTimeMillis()), 1))
      .map(res => "Post successfully added").recover {
    case ex: Exception => ex.getCause.getMessage
  }


  def update(post: Post): Future[Post] = db.run(
    posts.filter(_.id === post.id)
      .update(post.copy(post.id, post.title, post.content, new Date(System.currentTimeMillis()), post.ownerId)))
      .map(_ => post)


  def delete(id: Long): Future[Unit] =
    db.run(posts.filter(_.id === id).delete).map(_ => ())


  def deleteByIdAndOwnerId(id: Long, ownerId: Long): Future[Unit] =
    db.run(posts.filter(_.id === id).filter(_.ownerId === ownerId).delete).map(_ => ())


  private class PostTable(tag: Tag) extends Table[Post](tag, "post") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def content = column[String]("content")
    def dateTime = column[Date]("date_time")
    def ownerId = column[Long]("owner_id")

    def * = (id, title, content, dateTime, ownerId) <> (Post.tupled, Post.unapply)
  }
}