/*
 * MIT License
 *
 * Copyright (c) 2023. RECHERGG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package de.rechergg.command.core;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.rechergg.BluePearlMusic;
import de.rechergg.command.Command;
import de.rechergg.core.AudioInfo;
import de.rechergg.core.PlayerSendHandler;
import de.rechergg.core.TrackManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public class MusicCommand extends Command {

    private static final int PLAYER_LIST_LIMIT = 1000;
    private Guild guild;
    public static final AudioPlayerManager MANAGER = new DefaultAudioPlayerManager();
    private final Map<Guild, Map.Entry<AudioPlayer, TrackManager>> players = new HashMap<>();

    public MusicCommand(BluePearlMusic bluePearlMusic) {
        super(bluePearlMusic);
        AudioSourceManagers.registerRemoteSources(MANAGER);
        AudioSourceManagers.registerLocalSource(MANAGER);
    }

    @Override
    public CommandData getCommand() {
        return Commands.slash("music", "Allgemeiner Music Command")
                .addSubcommands(new SubcommandData("play", "Spielt einen gewünschten Song/PlayList ab")
                        .addOption(OptionType.STRING, "url", "Die URL oder der Name des Songs/PlayList", true))
                .addSubcommands(new SubcommandData("skip", "Überspringt einen Song")
                        .addOption(OptionType.INTEGER, "anzahl", "Die Anzahl an Songs die übersprungen werden sollen", false))
                .addSubcommands(new SubcommandData("stop", "Stop den aktuellen Song"))
                .addSubcommands(new SubcommandData("shuffel", "Shuffelt die aktuelle PlayList"))
                .addSubcommands(new SubcommandData("np", "Zeigt den Aktuellen Song an"))
                .addSubcommands(new SubcommandData("queue", "Zeigt die aktuelle Queue"))
                .addSubcommands(new SubcommandData("volume", "Setzt das Volume auf das angegeben Volume")
                        .addOption(OptionType.INTEGER, "volume", "Volume (ex: 80) in % aber ohne %", true));
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        guild = event.getGuild();

        if (event.getSubcommandName().equals("play")) {
            if (event.getOption("url") != null) {
                String url = event.getOption("url").getAsString();

                if (!(url.startsWith("http://") || url.startsWith("https://"))) {
                    url = "ytsearch: " + url;
                }

                loadTrack(url, event.getMember(), (TextChannel) event.getChannel());
                event.reply("✅").queue();

                return;
            }
            return;
        }

        if (event.getSubcommandName().equals("volume")) {
            if (event.getOption("volume") != null) {
                getManager(guild).volume(event.getOption("volume").getAsInt());
                event.replyEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.decode("#2f3136"))
                                .setDescription("Du hast erfolgreich das Volume auf " + event.getOption("volume").getAsInt() + "% gesetzt!")
                                .build()
                ).queue();
            }
        }

        if (event.getSubcommandName().equals("skip")) {
            if (isIdle(guild)) return;

            OptionMapping anzahl = event.getOption("anzahl");

            if (anzahl != null) {
                for (int i = anzahl.getAsInt(); i >= 1; i--) {
                    skip(guild);
                }
                event.reply("✅").queue();
                return;
            }

            skip(guild);
            event.reply("✅").queue();
            return;
        }

        if (event.getSubcommandName().equals("stop")) {
            if (isIdle(guild)) return;

            getManager(guild).purgeQueue();
            skip(guild);
            guild.getAudioManager().closeAudioConnection();
            event.reply("✅").queue();
            return;
        }

        if (event.getSubcommandName().equals("shuffel")) {
            if (isIdle(guild)) return;

            getManager(guild).shuffleQueue();
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.decode("#2f3136"))
                            .setDescription("Die Warteschlange wurde erfolgreich geshuffelt")
                            .setColor(Color.GREEN)
                            .build()
            ).queue();
            return;
        }

        if (event.getSubcommandName().equals("np")) {
            if (isIdle(guild)) return;

            AudioTrack track = getPlayer(guild).getPlayingTrack();
            AudioTrackInfo trackInfo = track.getInfo();
            String uri = trackInfo.uri;

            event.replyEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.decode("#2f3136"))
                            .setDescription("**Aktueller Song**")
                            .addField("Title", "**[" + trackInfo.title + "](" + trackInfo.uri + ")**", false)
                            .addField("Dauer", "`[ " + getTimestamp(track.getPosition()) + "/ " + getTimestamp(track.getDuration()) + " ]`", true)
                            .addField("Ersteller", trackInfo.author, true)
                            .setThumbnail("https://img.youtube.com/vi/" + uri.split("&list=")[0]
                                    .replace("https://youtube.com/watch?v=", "")
                                    .replace("https://www.youtube.com/watch?v=", "")+ "/0.jpg")
                            .setFooter("Hinzugefügt von " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                            .build()
            ).queue();

            return;
        }

        if (event.getSubcommandName().equals("queue")) {
            if (isIdle(guild)) return;

            List<String> tracks = new ArrayList<>();
            List<String> trackSublist;
            getManager(guild).getQueue().forEach(audioInfo -> tracks.add(buildQueueMessage(audioInfo)));

            if (tracks.size() > 20) {
                trackSublist = tracks.subList(0, 20);
            } else {
                trackSublist = tracks;
            }

            String out = trackSublist.stream().collect(Collectors.joining("\n"));

            event.replyEmbeds(
                    new EmbedBuilder()
                            .setColor(Color.decode("#2f3136"))
                            .setDescription("**Aktuelle Warteschlange**:\n" +
                                    "*[" + getManager(guild).getQueue().size() + " Songs]*\n\n" + out
                            )
                            .setFooter("Hinzugefügt von " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                            .build()
            ).queue();
        }
    }

    private void loadTrack(String identifier, Member author, TextChannel channel) {
        getPlayer(guild);

        MANAGER.setFrameBufferDuration(1000);
        MANAGER.loadItemOrdered(guild, identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                getManager(guild).queue(track, author);
                AudioTrackInfo trackInfo = track.getInfo();
                String uri = trackInfo.uri;

                channel.sendMessageEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.decode("#2f3136"))
                                .setDescription("**Song Hinzugefügt**")
                                .addField("Title", "**[" + trackInfo.title + "](" + trackInfo.uri + ")**", false)
                                .addField("Dauer", "`[ " + getTimestamp(track.getPosition()) + "/ " + getTimestamp(track.getDuration()) + " ]`", true)
                                .addField("Ersteller", trackInfo.author, true)
                                .setThumbnail("https://img.youtube.com/vi/" + uri.split("&list=")[0]
                                        .replace("https://youtube.com/watch?v=", "")
                                        .replace("https://www.youtube.com/watch?v=", "") + "/0.jpg")
                                .setFooter("Hinzugefügt von " + author.getUser().getAsTag(), author.getUser().getAvatarUrl())
                                .build()
                ).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (int i = 0; i < (Math.min(playlist.getTracks().size(), PLAYER_LIST_LIMIT)); i++) {
                    getManager(guild).queue(playlist.getTracks().get(i), author);
                }

                AudioTrack track = getManager(guild).getQueue().stream().findFirst().get().track();

                if (identifier.startsWith("ytsearch: ")) {
                    getManager(guild).getQueue().forEach(song -> {
                        if (!getManager(guild).getQueue().stream().findFirst().get().track().getInfo().uri.equals(song.track().getInfo().uri)) {
                            getManager(guild).remove(song);
                        }
                    });
                    channel.sendMessageEmbeds(
                            new EmbedBuilder()
                                    .setColor(Color.decode("#2f3136"))
                                    .setDescription("**Song Hinzugefügt**")
                                    .addField("Title", "**[" + track.getInfo().title + "]("
                                            + track.getInfo().uri + ")**", false)
                                    .addField("Dauer", "`[ "+ getTimestamp(track.getPosition())
                                            + " / " + getTimestamp(track.getDuration()) + " ]`", true)
                                    .addField("Ersteller", track.getInfo().author, true)
                                    .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().uri.split("&list=")[0]
                                            .replace("https://youtube.com/watch?v=", "")
                                            .replace("https://www.youtube.com/watch?v=", "") + "/0.jpg")
                                    .setFooter("Hinzugefügt von " + author.getUser().getAsTag(), author.getUser().getAvatarUrl())
                                    .build()
                    ).queue();
                    return;
                }

                channel.sendMessageEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.decode("#2f3136"))
                                .setDescription("**PlayList Hinzugefügt**")
                                .addField("Title", "**[" + track.getInfo().title + "](" + track.getInfo().uri + ")**", false)
                                .addField("Songs", "`[ "+ playlist.getTracks().size() + " ]`", true)
                                .addField("Ersteller", track.getInfo().author, true)
                                .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().uri.split("&list=")[0]
                                        .replace("https://youtube.com/watch?v=", "")
                                        .replace("https://www.youtube.com/watch?v=", "")  + "/0.jpg")
                                .setFooter("Hinzugefügt von " + author.getUser().getAsTag(), author.getUser().getAvatarUrl())
                                .build()
                ).queue();
            }

            @Override
            public void noMatches() {
                channel.sendMessageEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.decode("#2f3136"))
                                .setDescription("Es wurde kein Song gefunden")
                                .build()
                ).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessageEmbeds(
                        new EmbedBuilder()
                                .setColor(Color.RED)
                                .setDescription("Ein Fehler ist aufgetreten")
                                .build()
                ).queue();
            }
        });

    }

    private void skip(Guild guild) {
        getPlayer(guild).stopTrack();
    }
    private String buildQueueMessage(AudioInfo info) {
        AudioTrackInfo trackInfo = info.track().getInfo();
        String title = trackInfo.title;
        long length = trackInfo.length;
        return "`[ " + getTimestamp(length) + " ]` [" + title + "](" + trackInfo.uri + ")\n";
    }

    private String getTimestamp(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }

    private boolean isIdle(Guild guild) {
        return !hasPlayer(guild) || getPlayer(guild).getPlayingTrack() == null;
    }

    private TrackManager getManager(Guild guild) {
        return players.get(guild).getValue();
    }

    private AudioPlayer getPlayer(Guild guild) {
        if (hasPlayer(guild)) {
            return players.get(guild).getKey();
        }

        return createPlayer(guild);
    }

    private boolean hasPlayer(Guild guild) {
        return players.containsKey(guild);
    }

    private AudioPlayer createPlayer(Guild guild) {
        AudioPlayer player = MANAGER.createPlayer();
        TrackManager trackManager = new TrackManager(player);

        player.addListener(trackManager);
        guild.getAudioManager().setSendingHandler(new PlayerSendHandler(player));
        players.put(guild, new AbstractMap.SimpleEntry<>(player, trackManager));

        return player;
    }
}
