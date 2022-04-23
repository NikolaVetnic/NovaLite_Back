package dao

import models.Befriends
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BefriendsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {


  import profile.api._


  private val befriendsObjects = TableQuery[BefriendsTable]


  /**********
   * EXISTS *
   **********/
  def exists(id0: Long, id1: Long) : Future[Boolean] =
    db.run(befriendsObjects.filter(b => {
      (b.userId0 === id0 && b.userId1 === id1) || (b.userId0 === id1 && b.userId1 === id0)
    }).exists.result)


  def existsRequest(id0: Long, id1: Long): Future[Boolean] =
    db.run(befriendsObjects.filter(b => {
      (b.userId0 === id0 && b.userId1 === id1) || (b.userId0 === id1 && b.userId1 === id0)
    }).filter(_.status === 1).exists.result)


  def existsFriendship(id0: Long, id1: Long): Future[Boolean] =
    db.run(befriendsObjects.filter(b => {
      (b.userId0 === id0 && b.userId1 === id1) || (b.userId0 === id1 && b.userId1 === id0)
    }).filter(_.status === 2).exists.result)


  def requestSent(id0: Long, id1: Long) : Future[Boolean] =
    db.run(befriendsObjects.filter(b => {
      (b.userId0 === id0 && b.userId1 === id1 && b.status === 1)
    }).exists.result)


  def requestReceived(id0: Long, id1: Long) : Future[Boolean] =
    db.run(befriendsObjects.filter(b => {
      (b.userId0 === id1 && b.userId1 === id0 && b.status === 1)
    }).exists.result)


  /*******
   * GET *
   *******/
  def all() : Future[Seq[Befriends]] =
    db.run(befriendsObjects.result)


  def getConnectionByUserIds(id0: Long, id1: Long) : Future[Option[Befriends]] =
    db.run(befriendsObjects.filter(b => {
      (b.userId0 === id0 && b.userId1 === id1) || (b.userId0 === id1 && b.userId1 === id0)
    }).result.headOption)


  def getRequestsByUserId(id: Long) : Future[Seq[Befriends]] =
    db.run(befriendsObjects
      .filter(b => b.userId0 === id || b.userId1 === id)
      .filter(_.status === 1).result)


  def getFriendshipsByUserId(id: Long) : Future[Seq[Befriends]] =
    db.run(befriendsObjects
      .filter(b => b.userId0 === id || b.userId1 === id)
      .filter(_.status === 2).result)


  /********
   * POST *
   ********/
  def insert(befriends: Befriends): Future[String] =
    dbConfig.db.run(befriendsObjects += befriends).map(res => "Befriends object successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }


  /**********
   * DELETE *
   **********/
  def delete(id0: Long, id1: Long): Future[Unit] =
    db.run(befriendsObjects
      .filter(b => {
        (b.userId0 === id0 && b.userId1 === id1) || (b.userId0 === id1 && b.userId1 === id0)
      }).delete).map(_ => ())


  def deleteRequest(id0: Long, id1: Long): Future[Unit] =
    deleteConnection(id0, id1, 1)


  def deleteFriendship(id0: Long, id1: Long): Future[Unit] =
    deleteConnection(id0, id1, 2)


  def deleteConnection(id0: Long, id1: Long, status: Int): Future[Unit] =
    db.run(befriendsObjects
      .filter(b => {
        (b.userId0 === id0 && b.userId1 === id1) || (b.userId0 === id1 && b.userId1 === id0)
      }).filter(_.status === status).delete).map(_ => ())


  private class BefriendsTable(tag: Tag) extends Table[Befriends](tag, "befriends") {

    def userId0 = column[Long]("user_id0")
    def userId1 = column[Long]("user_id1")
    def status = column[Int]("status")

    def id = primaryKey("befriends_pk", (userId0, userId1))

    def * = (userId0, userId1, status) <> (Befriends.tupled, Befriends.unapply)
  }
}
