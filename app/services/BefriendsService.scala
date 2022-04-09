package services

import dao.BefriendsDao
import models.{Befriends, PostReaction}
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class BefriendsService @Inject()(
  dao: BefriendsDao,
  userService: UserService)(implicit ex: ExecutionContext) {

  def exists(befriends: Befriends): Future[Boolean] = {
    dao.exists(befriends.userId0, befriends.userId1)
  }

  def getAll(): Future[Seq[Befriends]] = {
    dao.all()
  }

  def get(userId0: Long, userId1: Long): Future[Option[Befriends]] = {
    dao.get(userId0, userId1)
  }

  def request(befriends: Befriends): Future[EStatus] = {

    val checkUser0 = Await.result(userService.existsId(befriends.userId0), Duration.Inf)
    val checkUser1 = Await.result(userService.existsId(befriends.userId1), Duration.Inf)
    val checkBefriends = Await.result(dao.exists(befriends.userId0, befriends.userId1), Duration.Inf)

    if (checkUser0 && checkUser1 && !checkBefriends)
      Future {
        dao.insert(befriends)
        EStatus.Success
      }
    else
      Future { EStatus.Failure }
  }

  def accept(befriends: Befriends): Future[EStatus] = {

    val fetchedBefriends = Await.result(dao.get(befriends.userId0, befriends.userId1), Duration.Inf)

    if (!fetchedBefriends.isEmpty && fetchedBefriends.get.status == 0)
      Future {
        delete(fetchedBefriends.get.userId0, fetchedBefriends.get.userId1)
        val newBefriends = new Befriends(fetchedBefriends.get.userId0, fetchedBefriends.get.userId1, 1)
        dao.insert(newBefriends)
        EStatus.Success
      }
    else
      Future { EStatus.Failure }
  }

  def delete(userId0: Long, userId1: Long): Future[Unit] = {
    dao.delete(userId0, userId1)
  }
}
