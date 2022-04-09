package dao

import models.{Reaction, Role}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RoleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val roles = TableQuery[RoleTable]

  def exists(id: Long) : Future[Boolean] =
    db.run(roles.filter(role => role.id === id).exists.result)

  def all(): Future[Seq[Role]] = db.run(roles.result)

  def get(id: Long): Future[Option[Role]] =
    db.run(roles.filter(_.id === id).result.headOption)

  private class RoleTable(tag: Tag) extends Table[Role](tag, "reaction") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def role = column[String]("reaction")

    def * = (id, role) <> (Role.tupled, Role.unapply)
  }
}
