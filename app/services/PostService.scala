package services

import dao.{PostDao, PostReactionDao}
import models.{Post, PostDisplayDto, PostInsertDto}
import utils.EStatus
import utils.EStatus.EStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostService @Inject()(
  dao: PostDao,
  userService: UserService,
  befriendsService: BefriendsService,
  postReactionDao: PostReactionDao,
  commentService: CommentService)(implicit ex: ExecutionContext) {


  /**********
   * EXISTS *
   **********/
  def exists(id: Long): Future[Boolean] = {
    dao.exists(id)
  }


  /*******
   * GET *
   *******/
  def getAll(): Future[Seq[Post]] = {
    dao.all()
  }


  def getByFriendIds(id: Long): Future[Seq[PostDisplayDto]] = {
    for {
      friends <- befriendsService.getFriendsByUserId(id)
      posts <- Future.sequence(friends.map(_.id).map(id => dao.getByOwnerId(id.get))).map(_.flatten)
      likes <- postReactionDao.allLikes()
    } yield {
      posts.map { post =>
        {
          PostDisplayDto(
            post.id,
            post.title,
            post.content,
            post.dateTime.toLocalDateTime,
            friends.find(_.id.get == post.ownerId).get,
            likes.filter(_.postId == post.id.get).size
          )
        }
      }
    }
  }


  def get(id: Long): Future[Option[Post]] = {
    dao.get(id)
  }


  def getByOwnerId(id: Long): Future[Seq[PostDisplayDto]] = {
    for {
      owner <- userService.get(id)
      posts <- dao.getByOwnerId(id)
      likes <- postReactionDao.allLikes()
    } yield {
      posts.map { post =>
        {
          PostDisplayDto(
            post.id,
            post.title,
            post.content,
            post.dateTime.toLocalDateTime,
            owner.get,
            likes.filter(_.postId == post.id.get).size
          )
        }
      }
    }
  }


  def getByIdAndOwnerId(id: Long, ownerId: Long): Future[Seq[Post]] = {
    dao.getByOwnerId(ownerId)
  }


  def getByTitleContentAndOwnerId(title: String, content: String, ownerId: Long): Future[Seq[Post]] = {
    dao.getByTitleContentAndOwnerId(title, content, ownerId)
  }


  /********
   * POST *
   ********/
  def create(postInsertDto: PostInsertDto): Future[Option[Post]] = {
    for {
      _ <- dao.insert(postInsertDto)
      s <- dao.num()
      posts <- getAll()
    } yield posts.sortBy(_.id).drop(s - 1).find(p => true)
  }


  def update(post: Post): Future[Option[Post]] = {
    for {
      _ <- dao.update(post)
      post <- get(post.id.get)
    } yield post
  }


  def delete(id: Long): Future[EStatus] = {
    for {
      b <- dao.exists(id)
      _ <- commentService.deleteByPostId(id)
      _ <- postReactionDao.deleteByPostId(id)
    } yield {
      if (b) {
        dao.delete(id)
        EStatus.Success
      } else {
        EStatus.Failure
      }
    }
  }


  def deleteByIdAndOwnerId(id: Long, ownerId: Long): Future[EStatus] = {
    dao.existsAndOwnedBy(id, ownerId).map {
      case true =>
        dao.deleteByIdAndOwnerId(id, ownerId)
        EStatus.Success
      case false =>
        EStatus.Failure
    }
  }
}
