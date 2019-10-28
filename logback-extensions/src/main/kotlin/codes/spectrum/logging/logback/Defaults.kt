package codes.spectrum.logging.logback

const val DEFAULT_STD_OUT_PATTERN = "%-16(%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} %r %-5level %property{host} [%thread] %property{service} %logger{36}) - %replace(%msg){'\\s+',' '}%n%ex{10}"

val DEFAULT_STD_OUT_PATTERN_REGEX = """(\S+)\s*(\S+)\s*(\S+)\s*(\S+)\s*\[([^\]]+)\]\s*(\S+)\s*(\S+)\s*-\s*([^\r\n]+)(\s*(\S+):\s*(\S+)([^\r\n]*[\r\n]+((\s*at\s*[^\r\n]+[\r\n]*))*)?)?""".toRegex()

val NO_MDC_RABBIT_ROUTING_PATTERN = """log.%level.%property{service}.%replace(%replace(%msg){'(?m)^([\s\S]*?("operation"[^:]*:[^"]*"([^"]+)"[\s\S]*))|([\s\S]*)','${'$'}3'}){'/','--'}.%replace(%msg){'(?m)^([\s\S]*?("phase"[^:]*:[^"]*"([^"]+)"[\s\S]*))|([\s\S]*)','${'$'}3'}.%replace(%msg){'(?m)^([\s\S]*?("state"[^:]*:[^"]*"([^"]+)"[\s\S]*))|([\s\S]*)','${'$'}3'}.%replace(%msg){'(?m)^([\s\S]*?("userId"[^:]*:[^"]*"([^"]+)"[\s\S]*))|([\s\S]*)','${'$'}3'}.%replace(%msg){'(?m)^([\s\S]*?("sessionId"[^:]*:[^"]*"([^"]+)"[\s\S]*))|([\s\S]*)','${'$'}3'}.%replace(%logger){'[\s\S]*\.([^\.]+)','${'$'}1'}.%property{host}"""
val MDC_RABBIT_ROUTING_PATTERN = """log.%level.%property{service}.%X{operation:-none}.%X{phase:-none}.%X{state:-none}.%X{user:-none}.%X{session:-none}.%X{logger-tail:-none}.%property{host}"""