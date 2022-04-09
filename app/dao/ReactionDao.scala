package dao

import models.Reaction
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReactionDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val reactions = TableQuery[ReactionTable]

  def exists(id: Long) : Future[Boolean] =
    db.run(reactions.filter(reaction => reaction.id === id).exists.result)

  def all(): Future[Seq[Reaction]] = db.run(reactions.result)

  def get(id: Long): Future[Option[Reaction]] =
    db.run(reactions.filter(_.id === id).result.headOption)

  private class ReactionTable(tag: Tag) extends Table[Reaction](tag, "reaction") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def reaction = column[String]("reaction")

    def * = (id, reaction) <> (Reaction.tupled, Reaction.unapply)
  }
}
