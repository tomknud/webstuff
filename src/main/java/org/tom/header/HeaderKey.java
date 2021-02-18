package org.tom.header;

public enum HeaderKey {
	HOST			("Host"				),
	CONNECTION		("Connection"		),
	CACHE_CONTROL	("Cache-Control"	),
	PRAGMA			("Pragma"			),
	LOCATION		("Location"			),
	REFERER			("Referer"			),
	EXPIRES			("Expires"			),
	USER_AGENT		("User-Agent"		),
	ACCEPT			("Accept"			),
	CONTENT_LENGTH	("Content-Length"	),
	ORIGIN			("Origin"			),
	CONTENT_TYPE	("Accept"			),
	SEC_FETCH_SITE	("Sec-Fetch-Site"	),
	SEC_FETCH_MODE	("Sec-Fetch-Mode"	),
	SEC_FETCH_USER	("Sec-Fetch-User"	),
	SEC_FETCH_DEST	("Sec-Fetch-Dest"	),
	ACCEPT_ENCODING	("Accept-Encoding"	),
	ACCEPT_LANGUAGE	("Accept-Language"	),
	COOKIE			("Cookie"			),
	SET_COOKIE		("Set-Cookie"		),
	NULL			("null"				),
	UPGRADE_INSECURE_REQUESTS("Upgrade-Insecure-Requests");
	
	private String key;
	private String value;

	private HeaderKey(String key) {
		this.key = key;
	}

	public void setValue(String substring) {
		this.value = substring;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return key;
	}

	public String getKey() {
		return key;
	}

}