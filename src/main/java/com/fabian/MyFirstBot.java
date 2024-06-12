package com.fabian;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.message.MessageCreateListener;

public class MyFirstBot {
    public static void main( String[] args ) {

        //init api
        DiscordApi api = new DiscordApiBuilder()
            .setToken(getEnvironmentVariable("DISCORD_API_TOKEN"))
            .setAllNonPrivilegedIntentsAnd(Intent.MESSAGE_CONTENT)
            .login()
            .join();

        System.out.println("Bot Invite Link: ");
        System.out.println("\t" + api.createBotInvite());
        checkAttachmentFilename(api);
    }

    private static void respondToPingWithPong(DiscordApi api) {
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
            if (slashCommandInteraction.getCommandName().equals("ping")) {
                slashCommandInteraction.createImmediateResponder()
                    .setContent("Pong!")
                    .respond();
            }
        });
    }

    private static void respondToFabianWithHi(DiscordApi api) {
        api.addMessageCreateListener((MessageCreateEvent event) -> {
            System.out.println(event.getMessageContent());
            if (event.getMessageContent().equalsIgnoreCase("!fabian")) {
                event
                .getChannel()
                .sendMessage("Hello there fabian")
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                })
                ;
            }
        });
    }

    private static void checkAttachmentFilename(DiscordApi api) {
        api.addMessageCreateListener(new MessageCreateListener() {
            @Override
            public void onMessageCreate(MessageCreateEvent event) {
                List<MessageAttachment> list = event.getMessageAttachments();
                
                if (!event.getMessageAuthor().isUser()) {
                    System.out.println("The message author is not a user");
                    return;
                }
                
                for(MessageAttachment attachment : list) {
                    try {
                        // get the extension of the file (the file type)
                        String fileName = attachment.getFileName();
                        String[] fileNameArr = fileName.split("\\.");
                        String fileExtension = fileNameArr[fileNameArr.length - 1];
                        
                        // we only care about files with the extension .json (ACC) and .sto (iRacing)
                        if ( !(fileExtension.equals("sto") || fileExtension.equals("json")) ) {
                            System.out.println("The file does not have 'sto' or 'json' as an extension");
                            continue;
                        }
                        
                        if (validateFilename(fileName, '_')) {
                            continue;
                        }
                        
                    } catch (Exception e) {
                        continue;
                    }
                    
                    // the allowedMentions object controls who we are allowed to mention
                    AllowedMentions allowedMentions = new AllowedMentionsBuilder()
                            .addUser(event.getMessageAuthor().getId())
                            .setMentionRoles(true)
                            .build();
                    
                    // cast MessageAuthor to User
                    // the 'Optional' objects basically avoids 'null' values
                    Optional<User> messageUserOptional = event.getMessageAuthor().asUser();
                    
                    new MessageBuilder()
                            .setAllowedMentions(allowedMentions)
                            .append(messageUserOptional.get().getMentionTag())
                            .append(" ")
                            .append(getRandomGuentherQuote())
                            .append("\n\n")
                            .append(" You better reupload the setup with an appropriate name conforming to the convention car_track_setupVersion or I get focking angry.")
                            .append(" ")
                            .send(event.getChannel());
                }
            }
        });
    }

    private static String getRandomGuentherQuote() {
        Random rand = new Random();
        String[] quotes = new String[]{
            "I need to phone Gene now.",
            "Last year, for a good looking setup name, I mean, I would have fucked the whole paddock.",
            "I'm done with ugly looking setup names until I go from this planet.",
            "Everybody on the team is highly motivated to follow the setup naming convention. Well, everybody but one.",
            "I learned English in pub. Not in school. And it looks like you learned how to name your setups there as well.",
            "We could have looked like Rock Stars. But now we look like a fucking bunch of wankers, A bunch of fucking clowns that can't follow simple standards.",
            "You're not focking doing that to me. You do not fock smash my setup conventions."
        };
        return quotes[rand.nextInt(quotes.length)];
    }

    private static boolean validateFilename(String fileName, char c) {
        // count the number of hyphens (_) and check the number
        int numberOfChar = 0;
        for(int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == c) {
                numberOfChar++;
            }
        }
        System.out.println("There are " + numberOfChar + " " + c);
        if (numberOfChar != 2) {
            System.out.println("The filename is not valid");
            return false;
        }
        System.out.println("The filename is valid");
        return true;
    }

    private static String getEnvironmentVariable(String variableName) {
        return System.getenv(variableName);
    }
}
