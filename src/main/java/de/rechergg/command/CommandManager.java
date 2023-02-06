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

package de.rechergg.command;

import de.rechergg.BluePearlMusic;
import de.rechergg.command.core.MusicCommand;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final BluePearlMusic bluePearlMusic;

    private final List<Command> commands;

    public CommandManager(BluePearlMusic bluePearlMusic) {
        this.bluePearlMusic = bluePearlMusic;
        this.commands = new ArrayList<>();
    }

    private void registerCommand(@NotNull Command command) {
        this.commands.add(command);
    }

    public void invoke() {
        registerCommand(new MusicCommand(bluePearlMusic));

        CommandListUpdateAction update = bluePearlMusic.getJda().updateCommands();

        for (Command command : commands) {
            update = update.addCommands(command.getCommand());
        }

        update.queue();
    }

    @NotNull
    public List<Command> getCommands() {
        return commands;
    }
}
