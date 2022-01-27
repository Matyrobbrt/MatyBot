## How to run the bot
Before you can do anything, make sure that you have a Java 16 JDK installed on your machine, and that it is the JAVA_HOME environment variable.
1. Download the bot. This can be done through either:
    - Click on the latest commit, go to the actions tab, and a built JAR should be there if there were not any compiling errors; or
    - Download the repository contents, and put them in an empty directory. CD into that directory, and run `gradlew shadowJar`. When the building process is complete, the jar should be in `build/libs/`

2. Create a [Discord App](https://discordapp.com/developers/applications/me). Give it an appropriate name, and make sure to click the "Create a Bot User" button. After that, make sure to copy the bot token, you'll need it for the next step!
3. Put the jar that you got at step 1 in an *empty* directory. It is needed that the directory is empty as the bot does need to store quite a bit of stuff. 
4. Create an `.env` file at the root of the directory, and add the following line in it: `BOT_TOKEN=yourBotToken` (`yourBotToken` being the bot token)
5. The bot can be run using the command: `java -jar [jar name]`. The first run will create some directories, and the `general.toml` config file inside the `configs` directory. 
6. Open the general config and set the `general.botOwner` property to your discord ID.