package services

import dao.ReactionDao
import models.Reaction

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReactionService @Inject()(dao: ReactionDao)(implicit ex: ExecutionContext) {


  def exists(id: Long): Future[Boolean] = {
    dao.exists(id)
  }


  def getAll(): Future[Seq[Reaction]] = {
    dao.all()
  }


  def get(id: Long): Future[Option[Reaction]] = {
    dao.get(id)
  }
}
