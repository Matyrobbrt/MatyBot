package io.github.matyrobbrt.matybot.util.console.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public final class LoggerNameFilter extends AbstractMatcherFilter<ILoggingEvent> {

	String loggerName;

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	@Override
	public FilterReply decide(ILoggingEvent event) {
		return event.getLoggerName().equals(loggerName) ? onMatch : onMismatch;
	}

}
