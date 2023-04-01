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

package de.rechergg;

import de.rechergg.command.CommandHandler;
import de.rechergg.command.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluePearlMusic {

    private Logger logger;
    private JDA jda;
    private CommandManager commandManager;

    public BluePearlMusic() {
        this.logger = LoggerFactory.getLogger(BluePearlMusic.class);

        String token = System.getenv("BOT_TOKEN");

        if (token == null) {
            this.logger.error("Der Token vom Bot wurde nicht gefunden!");
            System.exit(-1);
        }

        this.commandManager = new CommandManager(this);
        login(token);
    }

    private void login(String token) {
        this.logger.info("Discord Bot logging in...");
        JDABuilder builder = JDABuilder.createDefault(token, GatewayIntent.GUILD_VOICE_STATES);
        builder.setAutoReconnect(true);
        builder.enableCache(CacheFlag.VOICE_STATE);
        builder.addEventListeners(new CommandHandler(this));
        builder.setBulkDeleteSplittingEnabled(false);
        try {
            jda = builder.build().awaitReady();
        } catch (InterruptedException e) {
            this.logger.info("Der Bot konnte sich nicht einloggen. Ist der Token korrekt?");
            this.logger.info("Exception is " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
        }

        commandManager.invoke();

        this.logger.info("Eingeloggt als " + jda.getSelfUser().getAsTag() + ". \n" +
                "$$$$$$$\\  $$\\                     $$$$$$$\\                                $$\\ $$\\      $$\\                     $$\\           \n" +
                "$$  __$$\\ $$ |                    $$  __$$\\                               $$ |$$$\\    $$$ |                    \\__|          \n" +
                "$$ |  $$ |$$ |$$\\   $$\\  $$$$$$\\  $$ |  $$ | $$$$$$\\   $$$$$$\\   $$$$$$\\  $$ |$$$$\\  $$$$ |$$\\   $$\\  $$$$$$$\\ $$\\  $$$$$$$\\ \n" +
                "$$$$$$$\\ |$$ |$$ |  $$ |$$  __$$\\ $$$$$$$  |$$  __$$\\  \\____$$\\ $$  __$$\\ $$ |$$\\$$\\$$ $$ |$$ |  $$ |$$  _____|$$ |$$  _____|\n" +
                "$$  __$$\\ $$ |$$ |  $$ |$$$$$$$$ |$$  ____/ $$$$$$$$ | $$$$$$$ |$$ |  \\__|$$ |$$ \\$$$  $$ |$$ |  $$ |\\$$$$$$\\  $$ |$$ /      \n" +
                "$$ |  $$ |$$ |$$ |  $$ |$$   ____|$$ |      $$   ____|$$  __$$ |$$ |      $$ |$$ |\\$  /$$ |$$ |  $$ | \\____$$\\ $$ |$$ |      \n" +
                "$$$$$$$  |$$ |\\$$$$$$  |\\$$$$$$$\\ $$ |      \\$$$$$$$\\ \\$$$$$$$ |$$ |      $$ |$$ | \\_/ $$ |\\$$$$$$  |$$$$$$$  |$$ |\\$$$$$$$\\ \n" +
                "\\_______/ \\__| \\______/  \\_______|\\__|       \\_______| \\_______|\\__|      \\__|\\__|     \\__| \\______/ \\_______/ \\__| \\_______|\n" +
                "                                                                                                                             \n" +
                "                                                                                                                             \n"
        );

        getJda().getPresence().setStatus(OnlineStatus.ONLINE);
        getJda().getGuilds().forEach(guild -> guild.getAudioManager().setSelfDeafened(true));
        getJda().getGuilds().forEach(guild -> guild.getAudioManager().setSelfMuted(false));
        getJda().getPresence().setActivity(Activity.playing(" Music"));
    }

    public JDA getJda() {
        return jda;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
