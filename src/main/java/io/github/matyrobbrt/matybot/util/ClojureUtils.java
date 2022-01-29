package io.github.matyrobbrt.matybot.util;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Joiner;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import clojure.lang.Var;
import io.github.matyrobbrt.jdautils.utils.TypeBinding;
import io.github.matyrobbrt.matybot.MatyBot;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Slf4j
@SuppressWarnings("unchecked")
@UtilityClass
public class ClojureUtils {

	private static final Class<?>[] BLACKLIST_CLASSES = {
			Thread.class
	};

	// Blacklist accessing discord functions
	private static final String[] BLACKLIST_PACKAGES = {
			MatyBot.class.getPackage().getName(), JDA.class.getPackage().getName(),
	};

	private static final IFn SANDBOX;

	private static final Map<String, Function<CommandContext, Optional<?>>> CONTEXT_VARS = new LinkedHashMap<>();

	static {
		Clojure.var("clojure.core", "require").invoke(Clojure.read("[clojail core jvm testers]"));

		// Convenience declarations of used functions
		IFn readString = Clojure.var("clojure.core", "read-string");
		IFn sandboxfn = Clojure.var("clojail.core", "sandbox");
		clojure.lang.Var secure_tester = (Var) Clojure.var("clojail.testers", "secure-tester");

		// Load these to add new blacklisted resources
		IFn blacklist_objects = Clojure.var("clojail.testers", "blacklist-objects");
		IFn blacklist_packages = Clojure.var("clojail.testers", "blacklist-packages");

		// Create our tester with custom blacklist
		Object tester = Clojure.var("clojure.core/conj").invoke(secure_tester.getRawRoot(),
				blacklist_objects.invoke(PersistentVector.create((Object[]) BLACKLIST_CLASSES)),
				blacklist_packages.invoke(PersistentVector.create((Object[]) BLACKLIST_PACKAGES)));

		//@formatter:off
		final TypeBinding<Member> memberBinding = new TypeBinding<Member>("Member")
                .bind("id", Member::getIdLong)
                .bind("name", m -> m.getUser().getName())
                .bind("nick", Member::getEffectiveName)
                .bind("discriminator", m -> m.getUser().getDiscriminator())
                .bind("bot", m -> m.getUser().isBot())
                .bind("avatar", Member::getEffectiveAvatarUrl)
                .bind("joined", Member::getTimeJoined)
                // TODO all the presence shit
                .bind("roles", m -> m.getRoles().stream().sorted(Comparator.comparing(Role::getPositionRaw).reversed())
                		.mapToLong(ISnowflake::getIdLong).toArray());

		addContextVar("author", ctx -> Optional.ofNullable(TypeBindingPersistentMap.create(memberBinding, ctx.getMember())));
	
			SANDBOX = (IFn) sandboxfn.invoke(tester,
			            Clojure.read(":timeout"), 2000L,
			            Clojure.read(":namespace"), Clojure.read("matybot.sandbox"),
			            Clojure.read(":refer-clojure"), false,
			            Clojure.read(":init"), readString.invoke(Joiner.on('\n').join(
			                    readLines())));
	}
	
	@SuppressWarnings("rawtypes")
	private static List readLines() {
		try {
			return IOUtils.readLines(MatyBot.class.getResourceAsStream("/sandbox-init.clj"));
		} catch (IOException e) {
			log.error("Error while setting-up the sandbox clojure", e);
		}
		return null;
	}
	
	private static void addContextVar(String name, Function<CommandContext, Optional<?>> factory) {
        String var = "*" + name + "*";
        ((Var) Clojure.var("matybot.sandbox", var)).setDynamic().bindRoot(new PersistentArrayMap(new Object[0]));
        CONTEXT_VARS.put(var, factory);
    }
	
	public interface CommandContext {
		Member getMember();
	}
	
}
