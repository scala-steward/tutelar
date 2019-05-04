package com.wanari.tutelar.core
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractExecutionContext, onComplete, onSuccess, redirect}
import akka.http.scaladsl.server.Route
import com.wanari.tutelar.Api
import com.wanari.tutelar.core.AuthService.Token
import com.wanari.tutelar.core.ProviderApi._
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait ProviderApi extends Api {
  val callbackConfig: CallbackConfig

  def completeLoginFlowWithRedirect(token: Future[Token]): Route = {
    extractExecutionContext { implicit executor =>
      val callbackUrl = token
        .map(generateCallbackUrl)
        .recover { case _ => generateErrorCallbackUrl(Errors.AUTHENTICATION_FAILED) }

      onSuccess(callbackUrl) { url =>
        redirect(url, StatusCodes.Found)
      }
    }
  }

  def completeLoginFlowWithJson(token: Future[Token]): Route = {
    onComplete(token) {
      case Success(t) => complete(TokenData(t))
      case Failure(_) => complete((StatusCodes.Unauthorized, ErrorData(Errors.AUTHENTICATION_FAILED)))
    }
  }

  private def generateCallbackUrl(token: Token): String = {
    callbackConfig.success.replace("<<TOKEN>>", token)
  }

  private def generateErrorCallbackUrl(error: AuthError): String = {
    callbackConfig.failure.replace("<<ERROR>>", error)
  }
}

object ProviderApi {
  case class CallbackConfig(success: String, failure: String)

  case class LoginData(username: String, password: String, data: Option[JsObject])
  case class TokenData(token: Token)
  case class ErrorData(error: AuthError)

  import DefaultJsonProtocol._
  implicit val loginDataFormat: RootJsonFormat[LoginData] = jsonFormat3(LoginData)
  implicit val tokenDataFormat: RootJsonFormat[TokenData] = jsonFormat1(TokenData)
  implicit val errorDataFormat: RootJsonFormat[ErrorData] = jsonFormat1(ErrorData)

  type AuthError = String
  object Errors {
    val AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED"
  }
}
