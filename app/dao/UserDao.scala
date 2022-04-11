package dao

import javax.inject.Inject
import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val users = TableQuery[UserTable]

  def existsId(id: Long): Future[Boolean] =
    db.run(users.filter(user => user.id === id).exists.result)

  def existsUsername(username: String): Future[Boolean] =
    db.run(users.filter(_.username === username).exists.result)

  def isValidLogin(username: String, password: String): Future[Boolean] =
    db.run(users.filter(_.username === username).filter(_.password === password).exists.result)

  def all(): Future[Seq[User]] = db.run(users.result)

  def get(id: Long): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)

  def getByUsername(username: String): Future[Option[User]] =
    db.run(users.filter(_.username === username).result.headOption)

  def getByUsername2(username: String): Option[User] =
    Await.result(db.run(users.filter(_.username === username).result.headOption), Duration.Inf)

  def insert(user: User): Future[String] =
    dbConfig.db.run(users += user).map(res => "User successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }

  def update(user: User): Future[User] = {
      db.run(users
          .filter(_.id === user.id)
          .map(u => (u.firstName, u.lastName, u.password, u.imgUrl))
          .update(user.firstName, user.lastName, user.password, user.imgUrl))
        .map(_ => user)
  }

  def delete(id: Long): Future[Unit] =
    db.run(users.filter(_.id === id).delete).map(_ => ())

  private class UserTable(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def password = column[String]("password")
    def imgUrl = column[String]("img_url")
    def roleId = column[Long]("role_id")

    def * = (id, username, firstName, lastName, password, imgUrl, roleId) <> (User.tupled, User.unapply)
  }
}