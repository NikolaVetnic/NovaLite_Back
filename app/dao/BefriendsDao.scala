package dao

import models.Befriends
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BefriendsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val befriendsObjects = TableQuery[BefriendsTable]

  def exists(userId0: Long, userId1: Long) : Future[Boolean] =
    db.run(befriendsObjects.filter(_.userId0 === userId0).filter(_.userId1 === userId1).exists.result)

  def all() : Future[Seq[Befriends]] =
    db.run(befriendsObjects.result)

  def get(userId0: Long, userId1: Long) : Future[Option[Befriends]] =
    db.run(befriendsObjects.filter(_.userId0 === userId0).filter(_.userId1 === userId1).result.headOption)

  def getRequestsByUserId(userId: Long) : Future[Seq[Befriends]] =
    getConnectionByUserId(userId, 1)

  def getFriendshipsByUserId(userId: Long) : Future[Seq[Befriends]] =
    getConnectionByUserId(userId, 2)

  def getConnectionByUserId(userId: Long, status: Int) : Future[Seq[Befriends]] =
    db.run(befriendsObjects
      .filter(b => b.userId0 === userId || b.userId1 === userId)
      .filter(_.status === status).result)

  def insert(befriends: Befriends): Future[String] =
    dbConfig.db.run(befriendsObjects += befriends).map(res => "Befriends object successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }

  def delete(userId0: Long, userId1: Long): Future[Unit] =
    db.run(befriendsObjects
      .filter(_.userId0 === userId0)
      .filter(_.userId1 === userId1).delete).map(_ => ())

  def deleteRequest(userId0: Long, userId1: Long): Future[Unit] =
    deleteConnection(userId0, userId1, 1)

  def deleteFriendship(userId0: Long, userId1: Long): Future[Unit] =
    deleteConnection(userId0, userId1, 2)

  def deleteConnection(userId0: Long, userId1: Long, status: Int): Future[Unit] =
    db.run(befriendsObjects
      .filter(_.userId0 === userId0)
      .filter(_.userId1 === userId1)
      .filter(_.status === status).delete).map(_ => ())

  private class BefriendsTable(tag: Tag) extends Table[Befriends](tag, "befriends") {

    def userId0 = column[Long]("user_id0")
    def userId1 = column[Long]("user_id1")
    def status = column[Int]("status")

    def id = primaryKey("befriends_pk", (userId0, userId1))

    def * = (userId0, userId1, status) <> (Befriends.tupled, Befriends.unapply)
  }
}