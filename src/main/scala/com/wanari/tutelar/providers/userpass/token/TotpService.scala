package com.wanari.tutelar.providers.userpass.token

import com.wanari.tutelar.Initable
import com.wanari.tutelar.core.AuthService.TokenData
import com.wanari.tutelar.providers.userpass.UserPassService
import com.wanari.tutelar.providers.userpass.token.TotpServiceImpl.QRData
import com.wanari.tutelar.util.LoggerUtil.LogContext
import spray.json.JsObject

trait TotpService[F[_]] extends UserPassService[F] with Initable[F] {
  def register(userName: String, registerToken: String, password: String, data: Option[JsObject])(
      implicit ctx: LogContext
  ): F[TokenData]
  def qrCodeData: F[QRData]
}
