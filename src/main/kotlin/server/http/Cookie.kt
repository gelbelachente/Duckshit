package server.http


data class Cookie(val name : String, val value : String, val expires : String?, val maxAge : String?,
                  val domain : String?, val path : String?, val sameSite : SameSite = SameSite.STRICT){


    override fun toString(): String {
        return "$name=$value" +
                if(!expires.isNullOrBlank()) "; expires=$expires" else "" +
                        if(!maxAge.isNullOrBlank()) "; max-age=$maxAge" else "" +
                                if(!domain.isNullOrBlank()) "; domain=$domain" else "" +
                                        (if(!path.isNullOrBlank()) "; path=$path" else "" +
                                                 "; SameSite=${sameSite.name}") + "; secure"

    }


}

/*
Set-Cookie: <cookie-name>=<cookie-value> | Expires=<date>
               | Max-Age=<non-zero-digit> | Domain=<domain-value>
               | Path=<path-value> | SameSite=Strict|Lax|none
               https://www.geeksforgeeks.org/http-headers-set-cookie/
 */