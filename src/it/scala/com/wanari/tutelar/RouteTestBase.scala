package com.wanari.tutelar

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.wanari.tutelar.providers.oauth2.{FacebookService, GithubService, GoogleService, MicrosoftService}
import com.wanari.tutelar.providers.userpass.ldap.LdapService
import org.slf4j.LoggerFactory

import scala.concurrent.Future

trait RouteTestBase extends TestBase with ScalatestRouteTest {
  trait BaseTestScope {
    lazy val services = new ItTestServices {
      override implicit lazy val facebookService: FacebookService[Future]   = mock[FacebookService[Future]]
      override implicit lazy val githubService: GithubService[Future]       = mock[GithubService[Future]]
      override implicit lazy val googleService: GoogleService[Future]       = mock[GoogleService[Future]]
      override implicit lazy val microsoftService: MicrosoftService[Future] = mock[MicrosoftService[Future]]
      override implicit lazy val ldapService: LdapService[Future]           = mock[LdapService[Future]]

      when(facebookService.TYPE) thenReturn "facebook"
      when(githubService.TYPE) thenReturn "github"
      when(googleService.TYPE) thenReturn "google"
      when(microsoftService.TYPE) thenReturn "microsoft"
    }
    implicit val logger   = LoggerFactory.getLogger("TEST")
    lazy val route: Route = Api.createApi(services)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    await(system.terminate())
  }
}
