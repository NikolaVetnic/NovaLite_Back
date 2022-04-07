package dao

import models.Post
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.sql.Date
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val posts = TableQuery[PostTable]

  def all(): Future[Seq[Post]] = db.run(posts.result)

  def get(id: Long): Future[Option[Post]] = {
    db.run(posts.filter(_.id === id).result.headOption)
  }

  def insert(post: Post): Future[String] = {
    dbConfig.db.run(posts += post).map(res => "Post successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def update(post: Post): Future[Post] = {
    db.run(posts.filter(_.id === post.id).update(post)).map(_ => post)
  }

  def delete(id: Long): Future[Unit] = {
    db.run(posts.filter(_.id === id).delete).map(_ => ())
  }

  private class PostTable(tag: Tag) extends Table[Post](tag, "post") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def content = column[String]("content")
    def dateTime = column[Date]("date_time")
    def ownerId = column[Long]("owner_id")

    def * = (id, title, content, dateTime, ownerId) <> (Post.tupled, Post.unapply)
  }
}