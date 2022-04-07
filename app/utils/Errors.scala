package utils

import play.api.libs.json.Json

object Errors {

  val ID_NOT_FOUND_ERROR =
    Json.obj("idNotFoundError" -> "Object with input ID not found.")
  val USERNAME_FOUND_ERROR =
    Json.obj("usernameFoundError" -> "User with input username already exists.")
  val ID_NOT_FOUND_OR_USERNAME_FOUND_ERROR =
    Json.obj("idNotFoundOrUsernameFoundError" -> "User with input ID not found or input username already exists.")
  val UPDATED_OBJECT_NOT_FOUND_ERROR =
    Json.obj("updatedObjectNotFoundError" -> "Updated object not found.")
}
