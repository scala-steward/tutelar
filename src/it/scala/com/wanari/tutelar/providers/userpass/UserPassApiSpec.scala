package com.wanari.tutelar.providers.userpass

import akka.http.scaladsl.model._
import com.wanari.tutelar.RouteTestBase
import com.wanari.tutelar.core.ProviderApi._
import org.mockito.ArgumentMatchersSugar._
import org.mockito.Mockito._
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.wanari.tutelar.util.LoggerUtil.LogContext

import scala.concurrent.Future

class UserPassApiSpec extends RouteTestBase {

  trait TestScope extends BaseTestScope {
    lazy val serviceMock = mock[UserPassService[Future]]
    override lazy val route = new UserPassApi {
      override val service        = serviceMock
      override val servicePath    = "testPath"
      override val callbackConfig = services.configService.getCallbackConfig
    }.route()
  }

  val jsonRequest = LoginData("user", "pw", Some(JsObject("hello" -> JsTrue))).toJson.compactPrint
  val entity      = HttpEntity(MediaTypes.`application/json`, jsonRequest)

  "POST /testPath/login" should {
    val postLoginRequest = {
      Post("/testPath/login").withEntity(entity)
    }

    "forward the username, password and extra data to service" in new TestScope {
      when(serviceMock.login(any[String], any[String], any[Option[JsObject]])(any[LogContext])) thenReturn Future
        .successful("TOKEN")
      postLoginRequest ~> route ~> check {
        verify(serviceMock).login(eqTo("user"), eqTo("pw"), eqTo(Some(JsObject("hello" -> JsTrue))))(any[LogContext])
      }
    }
    "return redirect with callback" in new TestScope {
      when(serviceMock.login(any[String], any[String], any[Option[JsObject]])(any[LogContext])) thenReturn Future
        .successful("TOKEN")
      postLoginRequest ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[TokenData] shouldEqual TokenData("TOKEN")
      }
    }
    "return redirect with error" in new TestScope {
      when(serviceMock.login(any[String], any[String], any[Option[JsObject]])(any[LogContext])) thenReturn Future
        .failed(new Exception())
      postLoginRequest ~> route ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[ErrorData] shouldEqual ErrorData("AUTHENTICATION_FAILED")
      }
    }
  }

}
