package saleson.common.enumeration;

public enum AuthAction {
	SUCCESS,				// 인증 성공
	FAILURE,				// 인증 실패
	ACCOUNT_EXPIRE,			// 1년간 로그인 X
	ACCOUNT_LOCK,			// 패스워드 3회
	ACCOUNT_CREDENTIAL_EXPIRE, // 패스워드 유효기간 만료
	ACCOUNT_ENABLE			// 계정활성화

}
