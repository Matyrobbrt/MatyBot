package io.github.matyrobbrt.jdautils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.StoreChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.requests.restaction.GuildAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.internal.requests.restaction.MessageActionImpl;
import okhttp3.OkHttpClient;

public class JDABot implements JDA {

	private final JDA jda;

	protected JDABot(final JDA jda) {
		this.jda = jda;
	}

	public final JDA getJDA() {
		return jda;
	}

	/**
	 * Calls the given consumer only if the text channel with the given ID is known
	 * to the bot.
	 *
	 * @param channelID The channel ID
	 * @param consumer  The consumer of the text channel
	 * @see             net.dv8tion.jda.api.JDA#getTextChannelById(long)
	 */
	public void getChannelIfPresent(final long channelID, final Consumer<TextChannel> consumer) {
		final var channel = getTextChannelById(channelID);
		if (channel != null) {
			consumer.accept(channel);
		}
	}

	/**
	 * Creates a message that should be sent in the provided {@code channel}
	 * 
	 * @param  channel the channel in which the message will be sent when
	 *                 {@link RestAction#queue()} will be called
	 * 
	 * @return         the created message
	 */
	public MessageAction createMessage(final TextChannel channel) {
		return new MessageActionImpl(getJDA(), null, channel);
	}

	@Override
	public Status getStatus() {
		return jda.getStatus();
	}

	@Override
	public EnumSet<GatewayIntent> getGatewayIntents() {
		return jda.getGatewayIntents();
	}

	@Override
	public EnumSet<CacheFlag> getCacheFlags() {
		return jda.getCacheFlags();
	}

	@Override
	public boolean unloadUser(long userId) {
		return jda.unloadUser(userId);
	}

	@Override
	public long getGatewayPing() {
		return jda.getGatewayPing();
	}

	@Override
	public JDA awaitStatus(Status status, Status... failOn) throws InterruptedException {
		return jda.awaitStatus(status, failOn);
	}

	@Override
	public int cancelRequests() {
		return jda.cancelRequests();
	}

	@Override
	public ScheduledExecutorService getRateLimitPool() {
		return jda.getRateLimitPool();
	}

	@Override
	public ScheduledExecutorService getGatewayPool() {
		return jda.getGatewayPool();
	}

	@Override
	public ExecutorService getCallbackPool() {
		return jda.getCallbackPool();
	}

	@Override
	public OkHttpClient getHttpClient() {
		return jda.getHttpClient();
	}

	@Override
	public DirectAudioController getDirectAudioController() {
		return jda.getDirectAudioController();
	}

	@Override
	public void setEventManager(IEventManager manager) {
		jda.setEventManager(manager);
	}

	@Override
	public void addEventListener(Object... listeners) {
		jda.addEventListener(listeners);
	}

	@Override
	public void removeEventListener(Object... listeners) {
		jda.removeEventListener(listeners);
	}

	@Override
	public List<Object> getRegisteredListeners() {
		return jda.getRegisteredListeners();
	}

	@Override
	public RestAction<List<Command>> retrieveCommands() {
		return jda.retrieveCommands();
	}

	@Override
	public RestAction<Command> retrieveCommandById(String id) {
		return jda.retrieveCommandById(id);
	}

	@Override
	public RestAction<Command> upsertCommand(CommandData command) {
		return jda.upsertCommand(command);
	}

	@Override
	public CommandListUpdateAction updateCommands() {
		return jda.updateCommands();
	}

	@Override
	public CommandEditAction editCommandById(String id) {
		return jda.editCommandById(id);
	}

	@Override
	public RestAction<Void> deleteCommandById(String commandId) {
		return jda.deleteCommandById(commandId);
	}

	@Override
	public GuildAction createGuild(String name) {
		return jda.createGuild(name);
	}

	@Override
	public RestAction<Void> createGuildFromTemplate(String code, String name, Icon icon) {
		return jda.createGuildFromTemplate(code, name, icon);
	}

	@Override
	public CacheView<AudioManager> getAudioManagerCache() {
		return jda.getAudioManagerCache();
	}

	@Override
	public SnowflakeCacheView<User> getUserCache() {
		return jda.getUserCache();
	}

	@Override
	public List<Guild> getMutualGuilds(User... users) {
		return jda.getMutualGuilds(users);
	}

	@Override
	public List<Guild> getMutualGuilds(Collection<User> users) {
		return jda.getMutualGuilds(users);
	}

	@Override
	public RestAction<User> retrieveUserById(long id, boolean update) {
		return jda.retrieveUserById(id, update);
	}

	@Override
	public SnowflakeCacheView<Guild> getGuildCache() {
		return jda.getGuildCache();
	}

	@Override
	public Set<String> getUnavailableGuilds() {
		return jda.getUnavailableGuilds();
	}

	@Override
	public boolean isUnavailable(long guildId) {
		return jda.isUnavailable(guildId);
	}

	@Override
	public SnowflakeCacheView<Role> getRoleCache() {
		return jda.getRoleCache();
	}

	@Override
	public SnowflakeCacheView<StageChannel> getStageChannelCache() {
		return jda.getStageChannelCache();
	}

	@Override
	public SnowflakeCacheView<ThreadChannel> getThreadChannelCache() {
		return jda.getThreadChannelCache();
	}

	@Override
	public SnowflakeCacheView<Category> getCategoryCache() {
		return jda.getCategoryCache();
	}

	@Override
	public SnowflakeCacheView<StoreChannel> getStoreChannelCache() {
		return jda.getStoreChannelCache();
	}

	@Override
	public SnowflakeCacheView<TextChannel> getTextChannelCache() {
		return jda.getTextChannelCache();
	}

	@Override
	public SnowflakeCacheView<NewsChannel> getNewsChannelCache() {
		return jda.getNewsChannelCache();
	}

	@Override
	public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
		return jda.getVoiceChannelCache();
	}

	@Override
	public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
		return jda.getPrivateChannelCache();
	}

	@Override
	public RestAction<PrivateChannel> openPrivateChannelById(long userId) {
		return jda.openPrivateChannelById(userId);
	}

	@Override
	public SnowflakeCacheView<Emote> getEmoteCache() {
		return jda.getEmoteCache();
	}

	@Override
	public IEventManager getEventManager() {
		return jda.getEventManager();
	}

	@Override
	public SelfUser getSelfUser() {
		return jda.getSelfUser();
	}

	@Override
	public Presence getPresence() {
		return jda.getPresence();
	}

	@Override
	public ShardInfo getShardInfo() {
		return jda.getShardInfo();
	}

	@Override
	public String getToken() {
		return jda.getToken();
	}

	@Override
	public long getResponseTotal() {
		return jda.getResponseTotal();
	}

	@Override
	public int getMaxReconnectDelay() {
		return jda.getMaxReconnectDelay();
	}

	@Override
	public void setAutoReconnect(boolean reconnect) {
		jda.setAutoReconnect(reconnect);
	}

	@Override
	public void setRequestTimeoutRetry(boolean retryOnTimeout) {
		jda.setRequestTimeoutRetry(retryOnTimeout);
	}

	@Override
	public boolean isAutoReconnect() {
		return jda.isAutoReconnect();
	}

	@Override
	public boolean isBulkDeleteSplittingEnabled() {
		return jda.isBulkDeleteSplittingEnabled();
	}

	@Override
	public void shutdown() {
		jda.shutdown();
	}

	@Override
	public void shutdownNow() {
		jda.shutdownNow();
	}

	@Override
	public AccountType getAccountType() {
		return jda.getAccountType();
	}

	@Override
	public RestAction<ApplicationInfo> retrieveApplicationInfo() {
		return jda.retrieveApplicationInfo();
	}

	@Override
	public JDA setRequiredScopes(Collection<String> scopes) {
		return jda.setRequiredScopes(scopes);
	}

	@Override
	public String getInviteUrl(Permission... permissions) {
		return jda.getInviteUrl(permissions);
	}

	@Override
	public String getInviteUrl(Collection<Permission> permissions) {
		return jda.getInviteUrl(permissions);
	}

	@Override
	public ShardManager getShardManager() {
		return jda.getShardManager();
	}

	@Override
	public RestAction<Webhook> retrieveWebhookById(String webhookId) {
		return jda.retrieveWebhookById(webhookId);
	}

}
