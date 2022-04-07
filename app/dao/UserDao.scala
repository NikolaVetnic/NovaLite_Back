package dao

import javax.inject.Inject
import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val users = TableQuery[UserTable]

  def all(): Future[Seq[User]] = db.run(users.result)

  def get(id: Long): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  def insert(user: User): Future[String] = {
    dbConfig.db.run(users += user).map(res => "User successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def update(user: User): Future[User] = {
    db.run(users.filter(_.id === user.id).update(user)).map(_ => user)
  }

  def delete(id: Long): Future[Unit] = {
    db.run(users.filter(_.id === id).delete).map(_ => ())
  }

  private class UserTable(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def password = column[String]("password")
    def imgUrl = column[String]("img_url")
    def roleId = column[Long]("role_id")

    def * = (id, username, firstName, lastName, password, imgUrl, roleId) <> (User.tupled, User.unapply)
  }
}