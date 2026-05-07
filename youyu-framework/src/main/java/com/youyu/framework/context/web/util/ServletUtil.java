package com.youyu.framework.context.web.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.function.Function;

import com.youyu.framework.context.Env;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Lombok;
import org.apache.catalina.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

/**
 * Servlet 相关处理工具类
 * <p>
 * 【重要】尽管 Servlet 处理 HTTP请求头 不区分大小写，但 HTTP/2、HTTP/3 已明确规定请求头必须是小写形式，因此我们也优先采用小写形式的请求头以提高性能
 *
 * @since 2015-07-28
 */
public abstract class ServletUtil {

	public static String USER_AGENT = "User-Agent";

	public static Function<Object, String> JSON_CONVERTER = com.alibaba.fastjson2.JSON::toJSONString;

	/**
	 * 获取站点的相对根路径<br>
	 * 如果当前项目名称为"test",则获取到的根路径为"/test"
	 */
	public static String getRoot(HttpServletRequest request) {
		return request.getContextPath();
	}

	/**
	 * 获取站点的绝对根路径<br>
	 * 如果当前站点域名为"domain.com"，项目名称为"test",则获取到的根路径为"{@code protocol}://domain.com/test"
	 *
	 * @param request 请求对象
	 */
	public static String getRootURL(final HttpServletRequest request) {
		String scheme = request.getScheme();
		if (!Env.inDev()) {
			scheme = "https";
		}
		StringBuilder url = new StringBuilder(24).append(scheme).append("://");
		url.append(request.getServerName());
		int port = request.getServerPort();
		if (port != 80) { // 非80端口，添加端口号
			if (port != 443 || !"https".equals(scheme)) {
				url.append(':').append(port);
			}
		}
		url.append(request.getContextPath());
		return url.toString();
	}

	/**
	 * 是否是内网环境访问
	 */
	public static boolean fromInnerEnv(HttpServletRequest request) {
		String host = request.getServerName();
		return inLAN(host) || "127.0.0.1".equals(host) || "localhost".equals(host);
	}

	/**
	 * 指示指定的IP是否是内网IP（不包括 127.0.0.1 等本地IP）
	 */
	public static boolean inLAN(String IPv4) {
		return IPv4.startsWith("172.16.") /* 172.16.0.0/12 云厂商多使用该IP段 */ || IPv4.startsWith("192.168") /* 192.168.0.0/16 本地多使用该IP段 */ || IPv4.startsWith("10.") /* 10.0.0.0/8 */;
	}

	/**
	 * 获取当前请求的来源路径，即获取 HTTP Referer 字段，如果没有则返回 null
	 */
	@Nullable
	public static String getReferer(HttpServletRequest request) {
		return request.getHeader("referer");
	}

	/**
	 * 获取当前请求的 Accept-Language 请求头信息，如果没有则返回 null
	 */
	@Nullable
	public static String getAcceptLanguage(HttpServletRequest request) {
		return request.getHeader("accept-language");
	}

	/**
	 * 获取当前请求的URI<br>
	 * 该方法在Controller forward到JSP后，仍能够正确获取用户请求的URI
	 */
	public static String getRequestURI(HttpServletRequest request) {
		String uri = (String) request.getAttribute(jakarta.servlet.RequestDispatcher.FORWARD_REQUEST_URI);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return uri;
	}

	/** 是否需要通过请求头来获取远程IP：> 0 表示需要；< -20 表示不需要；= [-20, 0]（默认为 0） 表示根据实际情况自动判断 */
	public static int getRemoteIpByHeader = 0;
	/** 当 IP获取策略尚未确定时，要连续多少次没有请求头才能确认最终策略 */
	private static final int NO_IP_HEADER_THRESHOLD = -20;
	/** 用于获取客户端真实IP的请求头候选数组 */
	private static String[] clientIpHeaderCandidates = { "x-real-ip", "x-forwarded-for" };

	/**
	 * 设置获取客户端真实IP的请求头数组，默认为：["x-real-ip", "x-forwarded-for"]
	 * <p>
	 * 【注意】如果设置了 IP header 数组，则无需动态判断，内部直接从指定的请求头中获取真实IP
	 */
	public static void setClientIpHeaderCandidates(String... headers) {
		Assert.isTrue(headers != null && headers.length > 0, "headers");
		getRemoteIpByHeader = 1;
		clientIpHeaderCandidates = headers;
	}

	/**
	 * 获取当前请求的用户客户端的真实IP地址（智能防止IP伪造）
	 * <p>
	 * 获取客户端真实IP，一般有3种情况：
	 * <ol>
	 * <li><b>客户端->反向代理（HTTP协议）</b>->应用：需要通过 <b>请求头</b> 进行获取</li>
	 * <li><b>客户端->反向代理（AJP协议）->应用</b> 或 <b>客户端 -> 应用</b>：直接通过 <code>request.getRemoteAddr()</code> 即可获取</li>
	 * <li><b>内网监测程序探针</b> 请求：此时不存在外网IP，只有内网IP </li>
	 * </ol>
	 * <p>
	 * 此外，内网探针请求可能会和前面两种情况混合存在
	 */
	public static String getClientIP(final HttpServletRequest request) {
		String headerIP = null;
		if (getRemoteIpByHeader >= NO_IP_HEADER_THRESHOLD) {
			final String[] headerNames = clientIpHeaderCandidates;
			for (int i = 0; i < headerNames.length; i++) {
				String name = headerNames[i];
				String value = request.getHeader(name);
				if (value != null && !"unknown".equalsIgnoreCase(value)) {
					headerIP = value;
					if (i > 0 && !value.isEmpty()) { // 如果当前匹配的第一个请求头，则与第一次请求头进行交换，以减少下次匹配的消耗，及避免恶意注入
						final String first = headerNames[0];
						headerNames[0] = name;
						headerNames[i] = first;
						// todo
						// Context.LOGGER.warn("IP headers trigger position change: {}[{}] -> {}[0]", name, i, first);
					}
					break;
				}
			}
			if (headerIP != null && headerIP.length() > 15) { // 如果ip地址长度大于15，即"1.1.1.1,1.1.1.1"的长度，则可能存在多个ip地址
				int pos = headerIP.indexOf(',', 7 /* "1.1.1.1".length() */);
				if (pos > 0) {
					headerIP = headerIP.substring(0, pos);
				}
			}
			if (headerIP != null && getRemoteIpByHeader > 0) {
				return headerIP;
			}
		}
		String remoteIP = request.getRemoteAddr();
		String ip = tryTranslateLocalIP(remoteIP);
		// 进行最终的IP获取策略判断。这里之所以要用多次偏移才能确定，是担心启动后的首次请求可能来自于内网（例如：健康检查程序），才导致检测不到IP请求头
		if (getRemoteIpByHeader >= NO_IP_HEADER_THRESHOLD && getRemoteIpByHeader <= 0) {
			if (headerIP == null) {
				getRemoteIpByHeader--;
			} else if ("127.0.0.1".equals(remoteIP) || inLAN(ip)) {
				getRemoteIpByHeader = 1000; // > 0 即可，但不宜太小，避免并发请求导致前一个分支的自减也被触发
				return headerIP;
			}
		}
		return ip;
	}

	static String tryTranslateLocalIP(String ip) {
		if (ip == null) { // 在某些场景下，request.getRemoteAddr() 会返回 null
			return null;
		}
		switch (ip) {
			case "127.0.0.1":
			case "0:0:0:0:0:0:0:1":
				try {
					return getLocalIP();
				} catch (SocketException e) {
					throw new IllegalStateException(e);
				}
			default:
				return ip;
		}
	}

	/**
	 * 获取本地的IP地址。如果能够获取到外网IP，则返回外网IP；否则返回内网IP<br>
	 * <b>注意</b>：暂不支持IPv6地址
	 */
	public static String getLocalIP() throws SocketException {
		String ip = null;// 外网IP(或内网IP)
		Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ia;
		boolean notFound = true;
		while (notFound && netInterfaces.hasMoreElements()) {
			NetworkInterface ni = netInterfaces.nextElement();
			if (!ni.isUp() || ni.isLoopback()) {
				// 如果该网络接口 未启用 或 是本地回环接口，则忽略掉
				continue;
			}
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ia = address.nextElement();
				if ((ia instanceof Inet4Address) && !ia.isLoopbackAddress()) { // 仅处理IPv4地址，并且是非回环地址
					if (!ia.isSiteLocalAddress()) { // 内网IP
						ip = ia.getHostAddress(); // 外网IP
						notFound = false;
						break;
					}
				}
			}
		}
		if (StringUtils.isEmpty(ip)) {
			try {
				ia = Inet4Address.getLocalHost();
			} catch (UnknownHostException e) {
				throw new IllegalStateException(e);
			}
			ip = ia.getHostAddress();
		}
		return ip;
	}

	/**
	 * 获取指定网络地址所对应的网络接口硬件地址(一般是Mac地址)
	 */
	public static String getMacAddress(InetAddress inetAddress) throws SocketException {
		byte[] macArray = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
		if (macArray == null) {
			return null;
		}
		StringBuilder mac = new StringBuilder(17);
		final char[] buf = "0123456789ABCDEF".toCharArray();
		for (int i = 0; i < macArray.length; i++) {
			if (i > 0) {
				mac.append('-');
			}
			mac.append(buf[macArray[i] >> 4 & 0xF]);
			mac.append(buf[macArray[i] & 0xF]);
		}
		return mac.toString();
	}

	/**
	 * 获取本地网络地址所对应的网络接口硬件地址(一般是 MAC 地址)
	 */
	public static String getLocalMacAddress() throws SocketException, UnknownHostException {
		InetAddress ip = InetAddress.getLocalHost();
		if (ip.isLoopbackAddress()) {
			boolean notFound = true;
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (notFound && nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				if (ni.isUp() && !ni.isLoopback()) {
					Enumeration<InetAddress> ips = ni.getInetAddresses();
					while (ips.hasMoreElements()) {
						ip = ips.nextElement();
						if (ip instanceof Inet4Address) {
							notFound = false;
							break;
						}
					}
				}
			}
		}
		return getMacAddress(ip);
	}

	/**
	 * 判断是否是Ajax提交过来的请求
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}

	/**
	 * 检查指定请求的来源 referer 和 当前请求主机名 是否不一致
	 *
	 * @return 返回 true 表示检测到 CSRF 攻击
	 */
	public static boolean refererIsSameOrigin(HttpServletRequest request, boolean allowEmptyReferer) {
		final String referer = getReferer(request); // https://domain.com/h5/page
		final int refSize = StringUtils.length(referer);
		if (refSize > 0) {
			String host = request.getServerName(); // domain.com
			final int from = referer.indexOf("//", 4) + 2, length = host.length();
			return referer.regionMatches(from, host, 0, length) && (refSize == from + length || referer.charAt(from + length) == '/');
		}
		return allowEmptyReferer;
	}

	/**
	 * 判断是否是期望接收JSON数据的请求
	 */
	public static boolean isAcceptJSON(HttpServletRequest request) {
		String accept = request.getHeader("accept");
		return accept != null && accept.startsWith("application/json");
	}

	/**
	 * 将指定的对象转为JSON字符串并输出到HTTP响应流中
	 *
	 * @param response HttpServletResponse对象
	 * @param object 需要转为JSON进行输出的对象
	 */
	public static void writeJSON(HttpServletResponse response, Object object) throws UncheckedIOException {
		writeJSON(response, JSON_CONVERTER.apply(object));
	}

	/**
	 * 将指定的对象转为JSON字符串并输出到HTTP响应流中
	 *
	 * @param response HttpServletResponse对象
	 * @param jsonStr JSON字符串
	 */
	public static void writeJSON(HttpServletResponse response, String jsonStr) throws UncheckedIOException {
		response.setContentType("application/json");
		try {
			response.getWriter().write(jsonStr);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * 将指定的URL进行编码
	 */
	public static String encodeURL(String str, String encoding) {
		try {
			return URLEncoder.encode(str, encoding);
		} catch (UnsupportedEncodingException e) {
			throw Lombok.sneakyThrow(e);
		}
	}

	/**
	 * 将指定的URL基于"UTF-8"进行编码
	 */
	public static String encodeURL(String str) {
		return URLEncoder.encode(str, StandardCharsets.UTF_8);
	}

	/**
	 * 将指定的文本进行URL解码
	 */
	public static String decodeURL(String str, String encoding) {
		try {
			return URLDecoder.decode(str, encoding);
		} catch (UnsupportedEncodingException e) {
			throw Lombok.sneakyThrow(e);
		}
	}

	/**
	 * 将指定的文本进行基于"UTF-8"的URL解码
	 */
	public static String decodeURL(String str) {
		return URLDecoder.decode(str, StandardCharsets.UTF_8);
	}

	/**
	 * 设置用于下载文件的响应头信息（兼容所有浏览器）
	 */
	public static void responseHeaderForDownload(HttpServletRequest request, HttpServletResponse response, String fileName) {
		// see https://tools.ietf.org/html/rfc5987
		// see http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
		String userAgent = request.getHeader("user-agent");
		response.setContentType("application/octet-stream; charset=UTF-8");
		String contentDisposition = null;
		if (!StringUtils.isEmpty(userAgent)) {
			int pos;
			if ((pos = userAgent.indexOf("MSIE")) != -1) { // 如果是 IE
				char version = userAgent.charAt(pos + 5); // "MSIE x.0" 如果是 IE 7.0 或 8.0
				if (version == '7' || version == '8') {
					contentDisposition = "attachment; filename=" + encodeURL(fileName);
				}
			}
			/* 由于新版 Tomcat 限制响应头只能输出 ASCII，输出中文也会报错，所以先屏蔽这段代码
			else if (StringUtils.containsIgnoreCase(userAgent, "android")) { // 如果是Android浏览器
				contentDisposition = "attachment; filename=\"" + makeAndroidSafeFileName(fileName) + "\"";
			}
			*/
		}
		if (contentDisposition == null) { // default
			final String encodedFileName = encodeURL(fileName);
			contentDisposition = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
		}
		response.setHeader("Content-Disposition", contentDisposition);
		response.setHeader("Access-Control-Expose-Headers", "Content-Disposition"); // 允许前端JS获取下载的文件名称信息
	}

	private static String makeAndroidSafeFileName(String fileName) {
		// Android 浏览器仅支持 "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ._-+,@£$€!½§~'=()[]{}0123456789";
		final char[] newFileName = fileName.toCharArray();
		boolean changed = false;
		for (int i = 0; i < newFileName.length; i++) {
			final char c = newFileName[i];
			if (!((c >= 'A' && c <= 'Z') // 65 - 90
					|| (c >= 'a' && c <= 'z') // 97 - 122
					|| (c >= '0' && c <= '9') // 48 - 57
					|| "._-+,@£$€!½§~'=()[]{}".indexOf(c) != -1)) { // other chars
				newFileName[i] = '_';
				changed = true;
			}
		}
		return changed ? new String(newFileName) : fileName;
	}

	public static String getDeviceUniqueId(@NonNull HttpServletRequest request){
		// 根据请求信息生成设备ID：使用浏览器特征的组进行哈希
		String userAgent = request.getHeader("User-Agent");
		String secChUa = request.getHeader("Sec-Ch-Ua");
		String secChUaPlatform = request.getHeader("Sec-Ch-Ua-Platform");

		// 构建用于生成设备ID的字符串（按重要性排序）
		StringBuilder sb = new StringBuilder();
		if (userAgent != null) {
			sb.append(userAgent);
		}
		if (secChUa != null) {
			sb.append("|").append(secChUa);
		}
		if (secChUaPlatform != null) {
			sb.append("|").append(secChUaPlatform);
		}

		// 使用哈希算法生成设备ID
		String combinedString = sb.toString();
		if (!combinedString.isEmpty()) {
			return Integer.toHexString(combinedString.hashCode());
		} else {
			// 如果没有任何信息可用，生成一个随机ID
			return java.util.UUID.randomUUID().toString().replace("-", "");
		}
	}

}