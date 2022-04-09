package services

import dao.BefriendsDao
import models.Befriends
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BefriendsService @Inject()(
  dao: BefriendsDao,
  userService: UserService)(implicit ex: ExecutionContext) {

  def exists(befriends: Befriends): Future[Boolean] = {
    dao.exists(befriends.userId0, befriends.userId1)
  }

  def all(): Future[Seq[Befriends]] = {
    dao.all()
  }

  def get(userId0: Long, userId1: Long): Future[Option[Befriends]] = {
    dao.get(userId0, userId1)
  }

  def getRequestsByUserId(userId: Long): Future[Seq[Befriends]] = {
    dao.getRequestsByUserId(userId)
  }

  def getFriendshipsByUserId(userId: Long): Future[Seq[Befriends]] = {
    dao.getFriendshipsByUserId(userId)
  }

  def request(befriends: Befriends): Future[EStatus] = {

    val res = for {
      b0 <- userService.existsId(befriends.userId0)
      b1 <- userService.existsId(befriends.userId1)
      b2 <- dao.exists(befriends.userId0, befriends.userId1)
    } yield b0 && b1 && !b2

    res.map {
      case true => {
        dao.insert(Befriends(befriends.userId0, befriends.userId1, 1))    // quick hack
        EStatus.Success
      }
      case false => {
        EStatus.Failure
      }
    }
  }

  def accept(befriends: Befriends): Future[EStatus] = {

    dao.exists(befriends.userId0, befriends.userId1).map {
      case true => {
        delete(befriends.userId0, befriends.userId1)
        dao.insert(Befriends(befriends.userId0, befriends.userId1, 2))    // quick hack
        EStatus.Success
      }
      case false => {
        EStatus.Failure
      }
    }
  }

  def delete(userId0: Long, userId1: Long): Future[Unit] = {
    dao.delete(userId0, userId1)
  }

  def deleteRequest(userId0: Long, userId1: Long): Future[Unit] = {
    dao.deleteRequest(userId0, userId1)
  }

  def deleteFriendship(userId0: Long, userId1: Long): Future[Unit] = {
    dao.deleteFriendship(userId0, userId1)
  }
}
