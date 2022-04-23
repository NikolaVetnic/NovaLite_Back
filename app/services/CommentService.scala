package services

import dao.{CommentDao, CommentReactionDao, PostDao, PostReactionDao}
import models.{Comment, CommentDisplayDto, CommentInsertDto, Post, PostDisplayDto, PostInsertDto}
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class CommentService @Inject()(dao: CommentDao, userService: UserService, commentReactionService: CommentReactionService)(implicit ex: ExecutionContext) {


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
    // TODO: write a concurrent version of this method
    dao.getByPostId(id).map { comments =>
      comments.map(comment => {
        val currUser = Await.result(userService.get(comment.ownerId), Duration.Inf).get
        val numLikes = Await.result(commentReactionService.getByCommentId(comment.id.get), Duration.Inf)

        CommentDisplayDto(comment.id.get, comment.content, comment.dateTime.toLocalDateTime,
          currUser, comment.postId, numLikes.size)
      })
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

    dao.exists(id).map {
      case true =>
        // TODO: write a concurrent version of this method
        val commentReactions = Await.result(commentReactionService.getByCommentId(id), Duration.Inf)

        commentReactions.map(p =>
          commentReactionService.delete(p.userId, p.commentId))
        dao.delete(id)

        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }
}
