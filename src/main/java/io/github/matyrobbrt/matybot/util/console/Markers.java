package io.github.matyrobbrt.matybot.util.console;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Markers {

	public static final Marker EVENTS = MarkerFactory.getMarker("Events");

	public static final Marker DATABASE = MarkerFactory.getMarker("Database");

	public static final Marker MODULES = MarkerFactory.getMarker("Modules");

}