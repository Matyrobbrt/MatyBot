package io.github.matyrobbrt.matybot.util.console.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public final class LevelFilter extends AbstractMatcherFilter<ILoggingEvent> {

	Level level;

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted()) { return FilterReply.NEUTRAL; }

		if (event.getLevel().isGreaterOrEqual(level)) {
			return onMatch;
		} else {
			return onMismatch;
		}
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public void start() {
		if (this.level != null) {
			super.start();
		}
	}

}
