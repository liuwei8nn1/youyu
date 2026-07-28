import java.time.*;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class JavaTest {

	@Test
	public void test() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		// 1. LocalDateTime → 赋予时区 → ZonedDateTime
		LocalDateTime ldt = LocalDateTime.parse("2026-07-28T10:00:00");
		ZonedDateTime beijingTime = ldt.atZone(ZoneId.of("Asia/Shanghai"));  // 认为这是北京时间

		// 2. 转换到另一个时区
		ZonedDateTime tokyoTime = beijingTime.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
		System.out.println(tokyoTime.toLocalDateTime());
		// 结果: 2026-07-28T11:00:00+09:00[Asia/Tokyo]  (东京比北京快1小时)

		ZonedDateTime utcTime = beijingTime.withZoneSameInstant(ZoneOffset.UTC);
		System.out.println(utcTime.toLocalDateTime());
		// 结果: 2026-07-28T02:00:00Z  (北京 UTC+8, 所以 UTC 是 2 点)
	}

}
