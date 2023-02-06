# BluePearlMusic Discord Bot (JDA) ![](https://cdn.discordapp.com/attachments/1007204774053150762/1072168325435625602/bluepearl_better.png)

A simple music bot easy to set up.

The bot is written in Java with [JDA](https://github.com/DV8FromTheWorld/JDA) and [LavaPlayer](https://github.com/sedmelluq/lavaplayer).

# Commands
-   /music play <URL/Name¹> 
    -   Plays a song / playlist that given.
-   /music skip [amount]
    -   Skip a songs / skips a given amount on songs.
-   /music stop
    -   Stops the current music and clean the queue.
-   /music shuffel
    -   Randomize the current queue.
-   /music np
    -   Show the current playing song.
-   /music queue
    -   Show the next 20 songs.
-   /music volume <volume²>
    -   Set the volume to the given volume.

¹ = The name of a song, ² = ex: 80 (without %)

<> = required
[] = optional

# Install
- Required:
  - `Java 17`
  - `Docker`
  - `Docker Compose`
  - `Linux / Hosting System`
  - `SFTP` like `Termius`
- [Download the jar File](https://github.com/RECHERGG/BluePearlMusic/releases)
- Go into the `usr` Folder and create a new Folder with your bot`s name example(`BluePearlMusic`).
- [Put the 3 Files in there](https://github.com/RECHERGG/BluePearlMusic/tree/master/example)
- edit the `docker-compose.yml` like that:
```yml
version: '3.8'

services:
  yourbotname:
    image: yourbotname
    env_file:
      - token.env
    networks:
      - default
    restart: unless-stopped
networks:
  default:
```
- the `Dockerfile` like thath:
```Dockerfile
FROM ibm-semeru-runtimes:open-17-jre-focal

WORKDIR /usr/app
COPY ./YourBotName/ /usr/app/

ENTRYPOINT ["java", "-jar", "yourJarFileName.jar"]
```
- And the `token.env` ([Create a Bot](https://discord.com/developers/applications)):
```env
BOT_TOKEN=yourBotToken
```

# Edit
You can fork this repository to edit the messages of the Bot.
When you don't have a good hosting System, then edit the Buffer in the Constructor form the `PlayerSendHandler`.

# License
This bot (include the images and name) is licensed under the MIT License.
See [**LICENSE**](https://github.com/Rysefoxx/RyseInventory/blob/master/LICENSE)

Copyright (c) 2023 RECHERGG

    
