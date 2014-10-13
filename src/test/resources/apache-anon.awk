function octp(oct) {
	return (oct ~ /^[0-9]+$/) && ((oct+0) >= 0) && ((oct+0) <= 255)
}

function isValidIP(ip,	o) {
	return (split(ip,o,".")==4) && octp(o[1]) && octp(o[2]) && octp(o[3]) && octp(o[4])
}

function h2d(str, oct) {return hex2dec[substr(str, (oct*2)+1, 2)]}

function anonymize_ip(ip,	h) {
	if (ip in ANONIPS) {
		return ANONIPS[ip]
	} else {
		h = sprintf("%08x", ++ANONIPS["index"])
		return ANONIPS[ip] = sprintf(IPFORMAT, h2d(h,0), h2d(h,1), h2d(h,2), h2d(h,3))
	}
}

BEGIN {
	ANONIPS["index"] = 0
	for (i = 0; i < 256; i++) hex2dec[sprintf("%02x", i)] = i
	IPFORMAT = ((ZEROPAD) ? "%03d.%03d.%03d.%03d" : "%d.%d.%d.%d")
}

isValidIP($1) {
	$1 = anonymize_ip($1)
}

1
