package io.github.matyrobbrt.matybot.util.console.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public final class LoggerNameOrLevelFilter extends AbstractMatcherFilter<ILoggingEvent> {

	Level level;
	String loggerName;

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted()) { return FilterReply.NEUTRAL; }

		if (event.getLoggerName().equals(loggerName)) {
			return onMatch;
		} else {
			return event.getLevel().isGreaterOrEqual(level) ? onMatch : onMismatch;
		}
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	@Override
	public void start() {
		if (this.level != null && loggerName != null) {
			super.start();
		}
	}

}
