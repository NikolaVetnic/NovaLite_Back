package services

import dao.BefriendsDao
import models.{Befriends, User}
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BefriendsService @Inject()(
  dao: BefriendsDao,
  userService: UserService)(implicit ex: ExecutionContext) {


  /**********
   * EXISTS *
   **********/
  def exists(befriends: Befriends): Future[Boolean] =
    dao.existsFriendship(befriends.userId0, befriends.userId1)


  def existsRequest(befriends: Befriends): Future[Boolean] =
    dao.existsRequest(befriends.userId0, befriends.userId1)


  def existsFriendship(befriends: Befriends): Future[Boolean] =
    dao.existsFriendship(befriends.userId0, befriends.userId1)


  def requestSent(befriends: Befriends): Future[Boolean] =
    dao.requestSent(befriends.userId0, befriends.userId1)


  def requestReceived(befriends: Befriends): Future[Boolean] =
    dao.requestReceived(befriends.userId0, befriends.userId1)


  /*******
   * GET *
   *******/
  def all(): Future[Seq[Befriends]] = dao.all()


  def getConnectionByUserIds(userId0: Long, userId1: Long): Future[Option[Befriends]] =
    dao.getConnectionByUserIds(userId0, userId1)


  def getRequestsByUserId(userId: Long): Future[Seq[Befriends]] =
    dao.getRequestsByUserId(userId)


  def getFriendshipsByUserId(userId: Long): Future[Seq[Befriends]] =
    dao.getFriendshipsByUserId(userId)


  def getApplicantsByUserId(id: Long): Future[Seq[User]] = {
    for {
      requests <- dao.getRequestsByUserId(id)
      reqstIds = requests.map { request =>
        if (request.userId0 != id)
          request.userId0
        else
          request.userId1
      }
      users <- userService.getAll()
    } yield {
      users.filter(user => reqstIds.contains(user.id.get))
    }
  }


  def getFriendsByUserId(id: Long): Future[Seq[User]] = {
    for {
      requests <- dao.getFriendshipsByUserId(id)
      reqstIds = requests.map { request =>
        if (request.userId0 != id)
          request.userId0
        else
          request.userId1
      }
      users <- userService.getAll()
    } yield {
      users.filter(user => reqstIds.contains(user.id.get))
    }
  }


  /********
   * POST *
   ********/
  def sendRequest(befriends: Befriends): Future[EStatus] = {

    val res = for {
      b0 <- Future { befriends.userId0 != befriends.userId1 }             // user0 and user1 are different users
      b1 <- userService.existsId(befriends.userId0)                       // user0 exists
      b2 <- userService.existsId(befriends.userId1)                       // user1 exists
      b3 <- dao.existsRequest(befriends.userId0, befriends.userId1)       // no requests between the two
      b4 <- dao.existsFriendship(befriends.userId1, befriends.userId0)    // no friendship between the two
    } yield b0 && b1 && b2 && !b3 && !b4

    res.map {
      case true => {
        dao.insert(befriends)
        EStatus.Success
      }
      case false => {
        EStatus.Failure
      }
    }
  }


  def acceptRequest(befriends: Befriends): Future[EStatus] = {

    val res = for {
      b0 <- Future { befriends.userId0 != befriends.userId1 }             // user0 and user1 are different users
      b1 <- userService.existsId(befriends.userId0)                       // user0 exists
      b2 <- userService.existsId(befriends.userId1)                       // user1 exists
      b3 <- dao.existsRequest(befriends.userId0, befriends.userId1)       // there is a pending request between the two
      b4 <- dao.existsFriendship(befriends.userId1, befriends.userId0)    // no friendship between the two
    } yield b0 && b1 && b2 && b3 && !b4

    res.map {
      case true =>

        for {
          _ <- delete(befriends.userId0, befriends.userId1)
          r <- dao.insert(befriends)
        } yield r

        EStatus.Success

      case false =>
        EStatus.Failure
    }
  }


  /**********
   * DELETE *
   **********/
  def deleteRequest(id0: Long, id1: Long): Future[EStatus] = {

    val res = for {
      b <- dao.existsRequest(id0, id1)
      _ <- dao.deleteRequest(id0, id1)
    } yield b

    res.map {
      case true => EStatus.Success
      case false => EStatus.Failure
    }
  }


  def deleteFriendship(id0: Long, id1: Long): Future[EStatus] = {

    val res = for {
      b <- dao.existsFriendship(id0, id1)
      _ <- dao.deleteFriendship(id0, id1)
    } yield b

    res.map {
      case true => EStatus.Success
      case false => EStatus.Failure
    }
  }


  def delete(id0: Long, id1: Long): Future[EStatus] = {

    val res = for {
      b <- dao.exists(id0, id1)
      _ <- dao.delete(id0, id1)
    } yield b

    res.map {
      case true => EStatus.Success
      case false => EStatus.Failure
    }
  }
}
