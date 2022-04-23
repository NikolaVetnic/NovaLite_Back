package services

import dao.{CommentDao, CommentReactionDao}
import models.CommentReaction
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CommentReactionService @Inject()(
  dao: CommentReactionDao,
  userService: UserService,
  reactionService: ReactionService,
  commentDao: CommentDao) (implicit ex: ExecutionContext) {


  def getByCommentId(id: Long): Future[Seq[CommentReaction]] =
    dao.getByCommentId(id);


  def create(commentReaction: CommentReaction): Future[EStatus] = {

    val res = for {
      b0 <- commentDao.exists(commentReaction.commentId) // TODO: crashes when I use Service but Dao works, WTF?!
      b1 <- userService.existsId(commentReaction.userId)
      b2 <- reactionService.exists(commentReaction.reactionId)
    } yield b0 && b1 && b2

    res.map {
      case true => {
        dao.delete(commentReaction.userId, commentReaction.commentId)
        dao.insert(commentReaction)
        EStatus.Success
      }
      case false => {
        EStatus.Failure
      }
    }
  }


  def delete(userId: Long, commentId: Long): Future[EStatus] = {
    dao.exists(userId, commentId).map {
      case true =>
        dao.delete(userId, commentId)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def deleteIndiscriminately(commentId: Long): Future[EStatus] = {
    dao.deleteIndiscriminately(commentId)
    Future {EStatus.Success}
  }
}
