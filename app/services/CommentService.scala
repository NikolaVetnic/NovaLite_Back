package services

import dao.CommentDao
import models.{Comment, CommentDisplayDto, CommentInsertDto}
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommentService @Inject()(
  dao: CommentDao,
  userService: UserService,
  commentReactionService: CommentReactionService)(implicit ex: ExecutionContext) {


  /**********
   * EXISTS *
   **********/
  def exists(id: Long): Future[Boolean] =
    dao.exists(id)


  /*******
   * GET *
   *******/
  def getAll(): Future[Seq[Comment]] = {
    dao.all()
  }


  def get(id: Long): Future[Option[Comment]] = {
    dao.get(id)
  }


  def getByPostId(id: Long): Future[Seq[CommentDisplayDto]] = {
    for {
      comments <- dao.getByPostId(id)
      users <- userService.getAll()
      likes <- commentReactionService.getAllLikes()
    } yield {
      comments.map { comment =>
        CommentDisplayDto(
          comment.id.get,
          comment.content,
          comment.dateTime.toLocalDateTime,
          users.filter(user => user.id.get == comment.ownerId).head,
          comment.postId,
          likes.filter(_.commentId == comment.id.get).size
        )
      }
    }
  }


  /********
   * POST *
   ********/
  def create(commentDto: CommentInsertDto): Future[EStatus] = {
    userService.existsId(commentDto.ownerId).map {
      case true =>
        dao.insert(commentDto)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  /*******
   * PUT *
   *******/
  def update(comment: Comment): Future[EStatus] = {
    dao.existsAndOwnedBy(comment.id.get, comment.ownerId).map {
      case true =>
        dao.update(comment)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }


  def delete(id: Long): Future[EStatus] = {
    for {
      b <- dao.exists(id)
      _ <- commentReactionService.deleteByCommentId(id)
    } yield {
      if (b) {
        dao.delete(id)
        EStatus.Success
      } else {
        EStatus.Failure
      }
    }
  }
}
