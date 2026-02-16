package xyz.re_talk.domain.user.entity

enum class OAuthProvider(
    val description: String,
    val registrationId: String,
    val attributeKey: String
) {
    KAKAO("카카오", "kakao", "id");

    companion object {
        fun fromString(provider: String): OAuthProvider {
            return entries.find { it.registrationId.equals(provider, ignoreCase = true) }
                ?: throw IllegalArgumentException("지원하지 않는 로그인 제공자입니다: $provider")
        }
    }
}