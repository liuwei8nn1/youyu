package com.youyu.framework.warn.core;


import com.youyu.common.model.Result;
import org.jspecify.annotations.Nullable;

/**
 * bug 告警服务
 *
 */
public interface MsgWarnChannel {

	/**
	 * 程序异常发送 bug 信息【短时间内自动去除重复异常】
	 */
	Result<String> sendBugMsg(@Nullable String uri, Throwable e, boolean atAll);

	/**
	 * 程序异常发送 bug 信息【短时间内自动去除重复异常】
	 * 默认不会 @ 所有人
	 */
	default Result<String> sendBugMsg(@Nullable String uri, Throwable e) {
		return sendBugMsg(uri, e, false);
	}

	/**
	 * 程序异常发送 bug 信息【短时间内自动去除重复异常】
	 * 默认不会 @ 所有人
	 */
	default Result<String> sendBugMsg(Exception e) {
		return sendBugMsg(null, e);
	}

	/**
	 * 业务异常发送 bug 信息【短时间内自动去除重复异常】
	 */
	Result<String> sendBugMsg(String bugMsg, boolean atAll);

	/**
	 * 业务异常发送 bug 信息【短时间内自动去除重复异常】
	 * 默认会 @ 所有人
	 */
	default Result<String> sendBugMsg(String bugMsg) {
		return sendBugMsg(bugMsg, true);
	}

	/**
	 * 发送信息（没有阻塞条件）
	 */
	Result<String> sendMsg(String msg, boolean atAll);

	/**
	 * 发送信息（支持异步）
	 *
	 * @param msg   消息内容
	 * @param atAll 是否@所有人
	 * @param async 是否异步发送
	 */
	default Result<String> sendMsg(String msg, boolean atAll, boolean async) {
		if (async) {
			Thread.startVirtualThread(() -> sendMsg(msg, atAll));
			return Result.success();
		}
		return sendMsg(msg, atAll);
	}

	/**
	 * 发送信息（没有阻塞条件）
	 * 默认会 @ 所有人
	 */
	default Result<String> sendMsg(String msg) {
		return sendMsg(msg, true);
	}

	/**
	 * 发送 Markdown 信息（没有阻塞条件）
	 */
	Result<String> sendMarkdownMsg(String title, String markdown, boolean atAll);

	/**
	 * 发送 Markdown 信息（没有阻塞条件）
	 * 默认会 @ 所有人
	 */
	default Result<String> sendMarkdownMsg(String title, String markdown) {
		return sendMarkdownMsg(title, markdown, true);
	}

	// ==================== 异步方法（使用虚拟线程）====================

	/**
	 * 异步发送程序异常告警【不阻塞当前线程】
	 * <p>
	 * 注意：具体实现在AbstractWarnChannel中，采用"先判断再开线程"策略
	 */
	void sendBugMsgAsync(@Nullable String uri, Throwable e, boolean atAll);

	/**
	 * 异步发送程序异常告警【不阻塞当前线程】
	 * 默认不会 @ 所有人
	 */
	default void sendBugMsgAsync(@Nullable String uri, Throwable e) {
		sendBugMsgAsync(uri, e, false);
	}

	/**
	 * 异步发送程序异常告警【不阻塞当前线程】
	 */
	default void sendBugMsgAsync(Exception e) {
		sendBugMsgAsync(null, e);
	}

	/**
	 * 异步发送业务异常告警【不阻塞当前线程】
	 * <p>
	 * 注意：具体实现在AbstractWarnChannel中，采用"先判断再开线程"策略
	 */
	void sendBugMsgAsync(String bugMsg, boolean atAll);

	/**
	 * 异步发送业务异常告警【不阻塞当前线程】
	 * 默认会 @ 所有人
	 */
	default void sendBugMsgAsync(String bugMsg) {
		sendBugMsgAsync(bugMsg, true);
	}

	/**
	 * 异步发送普通消息【不阻塞当前线程】
	 * <p>
	 * 注意：具体实现在AbstractWarnChannel中，采用"先判断再开线程"策略
	 */
	void sendMsgAsync(String msg, boolean atAll);

	/**
	 * 异步发送普通消息【不阻塞当前线程】
	 * 默认会 @ 所有人
	 */
	default void sendMsgAsync(String msg) {
		sendMsgAsync(msg, true);
	}

	/**
	 * 异步发送 Markdown 消息【不阻塞当前线程】
	 * <p>
	 * 注意：具体实现在AbstractWarnChannel中，采用"先判断再开线程"策略
	 */
	void sendMarkdownMsgAsync(String title, String markdown, boolean atAll);

	/**
	 * 异步发送 Markdown 消息【不阻塞当前线程】
	 * 默认会 @ 所有人
	 */
	default void sendMarkdownMsgAsync(String title, String markdown) {
		sendMarkdownMsgAsync(title, markdown, true);
	}

}