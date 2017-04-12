package com.gmail.br45entei.server;

/** @author Brian_Entei */
public enum HTTPStatusCodes {
	
	HTTP_100("Continue"),
	HTTP_101("Switching Protocols"),
	HTTP_102("Processing"),
	HTTP_200("OK"),
	HTTP_200_1("Connection Established"),
	HTTP_201("Created"),
	HTTP_202("Accepted"),
	HTTP_203("Non-Authoritative Information"),
	HTTP_204("No Content"),
	HTTP_205("Reset Content"),
	HTTP_206("Partial Content"),
	HTTP_207("Multi-Status"),
	HTTP_208("Already Reported"),
	HTTP_226("IM Used"),
	HTTP_300("Multiple Choices"),
	HTTP_301("Moved Permanently"),
	HTTP_302("Found"),
	HTTP_302_1("Moved Temporarily"),
	HTTP_303("See Other"),
	HTTP_304("Not Modified"),
	HTTP_305("Use Proxy"),
	HTTP_306("Switch Proxy"),
	HTTP_307("Temporary Redirect"),
	HTTP_308("Permanent Redirect"),
	HTTP_400("Bad Request"),
	HTTP_401("Authorization Required"),
	HTTP_401_1("Unauthorized"),
	HTTP_402("Payment Required"),
	HTTP_403("Forbidden"),
	HTTP_404("Not Found"),
	HTTP_405("Method Not Allowed"),
	HTTP_406("Not Acceptable"),
	HTTP_407("Proxy Authentication Required"),
	HTTP_408("Request Timeout"),
	HTTP_409("Conflict"),
	HTTP_410("Gone"),
	HTTP_411("Length Required"),
	HTTP_412("Precondition Failed"),
	HTTP_413("Request Entity Too Large"),
	HTTP_414("Request-URI Too Long"),
	HTTP_415("Unsupported Media Type"),
	HTTP_416("Requested Range Not Satisfiable"),
	HTTP_417("Expectation Failed"),
	HTTP_418("I'm a teapot")/*XD wut?*/,
	HTTP_419("Authentication Timeout"),
	HTTP_420("Method Failure"),
	HTTP_421("Misdirected Request")/* HTTP/2.0 */,
	HTTP_422("Unprocessable Entity"),
	HTTP_423("Locked"),
	HTTP_424("Failed Dependency"),
	HTTP_426("Upgrade Required"),
	HTTP_428("Precondition Required"),
	HTTP_429("Too Many Requests"),
	HTTP_431("Request Header Fields Too Large"),
	HTTP_440("Login Timeout"),
	HTTP_444("No Response"),
	HTTP_449("Retry With"),
	HTTP_450("Blocked by Windows Parental Controls"),
	HTTP_451("Unavailable For Legal Reasons"),
	HTTP_473("Not A Proxy"),
	HTTP_494("Request Header Too Large"),
	HTTP_495("Cert Error"),
	HTTP_496("No Cert"),
	HTTP_497("HTTP to HTTPS"),
	HTTP_498("Token expired/invalid"),
	HTTP_499("Client Closed Request"),
	HTTP_499_1("Token required"),
	HTTP_500("Internal Server Error"),
	HTTP_501("Not Implemented"),
	HTTP_502("Bad Gateway"),
	HTTP_503("Service Unavailable"),
	HTTP_504("Gateway Timeout"),
	HTTP_505("HTTP Version Not Supported"),
	HTTP_506("Variant Also Negotiates"),
	HTTP_507("Insufficient Storage"),
	HTTP_508("Loop Detected"),
	HTTP_509("Bandwidth Limit Exceeded"),
	HTTP_510("Not Extended"),
	HTTP_511("Network Authentication Required"),
	HTTP_598("Network read timeout error"),
	HTTP_599("Network connect timeout error"),
	//Goofy status codes follow:
	HTTP_23("Display detected, turn it off please"),
	HTTP_42("42"),
	HTTP_88("Site contains NML (Nazi Markup Language), refused!"),
	HTTP_420_1("Enhance Your Calm"),
	HTTP_666("Site is evil!"),
	HTTP_1337("700 1337 f0R j00, K1dD0"),
	HTTP_TYPO3("Site uses TYPO3, switch to WordPress to continue"),
	
	HTTP_NOT_SET("-1 Not a status code");
	
	private final String value;
	
	private HTTPStatusCodes(String value) {
		this.value = value;
	}
	
	public final String getName() {
		return this.name().replace("HTTP_", "").replaceAll("_[0-9]*", "");
	}
	
	public final String getValue() {
		return this.value;
	}
	
	@Override
	public final String toString() {
		return this.getName() + " " + this.value;
	}
	
	/** @param httpStatusCode The HTTPStatusCodes code, represented as a string
	 * @return The resulting HTTPStatusCodes code, if found */
	public static HTTPStatusCodes fromString(String httpStatusCode) {
		if(httpStatusCode != null) {
			for(HTTPStatusCodes code : HTTPStatusCodes.values()) {
				if(code.toString().equalsIgnoreCase(httpStatusCode) || code.toString().equalsIgnoreCase(httpStatusCode.trim())) {
					return code;
				}
			}
		}
		return null;
	}
	
	/*static {
		for(HTTPStatusCodes code : values()) {
			LogUtils.println(code.toString());
		}
	}*/
	
}
