package com.shymain.discordRPG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.EventDispatcher;
import sx.blah.discord.handle.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.HTTP429Exception;

public class DiscordRPG {
	
	public static boolean ok = false;
	public static ConcurrentHashMap<Event, Integer> timedEvents = new ConcurrentHashMap<Event, Integer>();
	public static Timer timer = new Timer();
	public static Tick ticker = new Tick();
	public static HashMap<IUser, Event> eventStorage = new HashMap<IUser, Event>();

	
	public static IDiscordClient getClient(String email, String password, boolean login) throws DiscordException { //Returns an instance of the discord client
	    ClientBuilder clientBuilder = new ClientBuilder(); //Creates the ClientBuilder instance
	    clientBuilder.withLogin(email, password); //Adds the login info to the builder
	    if (login) {
	      return clientBuilder.login(); //Creates the client instance and logs the client in
	    } else {
	      return clientBuilder.build(); //Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
	    }
	  }
	
	public static void eventDistributer(String eventType, IUser user, IChannel channel) throws JSONException, IOException, MissingPermissionsException, HTTP429Exception, DiscordException
	{
		if(eventType.equalsIgnoreCase("defend"))
		{
			Monster.defend(user, channel);
		}else if(eventType.equalsIgnoreCase("RockReadyEvent"))
		{
			Floor.addRock(channel);
		}
	}
	
	@EventSubscriber
	public void firstMessage(ReadyEvent event) throws MissingPermissionsException, HTTP429Exception, DiscordException, IOException
	{
		if(ok==false){
			ok = true;
			String blank = "Testing";
		    Optional<String> game = Optional.<String>of(blank);
		    event.getClient().updatePresence(false, game);
		
		File f = new File(System.getProperty("user.home")+"/discordRPG");
		if(!f.exists())
		{
			f.mkdirs();
		}
		File g = new File(System.getProperty("user.home")+"/discordRPG/players.json");
		if(!g.exists())
		{
			g.createNewFile();
			FileWriter w = new FileWriter(System.getProperty("user.home")+"/discordRPG/players.json");
			w.write("{\"players\":{}}");
			w.flush();
			w.close();
		}
		File h = new File(System.getProperty("user.home")+"/discordRPG/floors.json");
		if(!h.exists())
		{
			h.createNewFile();
			FileWriter w = new FileWriter(System.getProperty("user.home")+"/discordRPG/floors.json");
			w.write("{\"floors\":{}}");
			w.flush();
			w.close();
			Floor.initialize();
		}
		File i = new File(System.getProperty("user.home")+"/discordRPG/monsters.json");
		if(!i.exists())
		{
			i.createNewFile();
			FileWriter w = new FileWriter(System.getProperty("user.home")+"/discordRPG/monsters.json");
			w.write("{\"monsters\":{}}");
			w.flush();
			w.close();
			Monster.initialize();
		}
		File j = new File(System.getProperty("user.home")+"/discordRPG/shops.json");
		if(!j.exists())
		{
			j.createNewFile();
			FileWriter w = new FileWriter(System.getProperty("user.home")+"/discordRPG/shops.json");
			w.write("{\"shops\":{}}");
			w.flush();
			w.close();
			Store.initialize();
		}
		}
	}
	
	@EventSubscriber
	public static void testMessages(MessageReceivedEvent event) throws JSONException, IOException, MissingPermissionsException, HTTP429Exception, DiscordException
	{
		String command;
		String allArguments = "";
		String[] arguments = null;
		
		String rawMessage = event.getMessage().getContent();
		
		String parts[] = rawMessage.split(" ", 2);
		command = parts[0];
		if(parts.length == 2)
		{
			allArguments = parts[1];
			arguments = allArguments.split(" ");
		}
		Input.commands(event);
		/*testMessages:
		if(command.equalsIgnoreCase("levelup"))
		{
			player.floorUp(event.getMessage().getAuthor());
		}else if(command.equalsIgnoreCase("statsup"))
		{
			player.statsUp(event.getMessage().getAuthor(), "mining");
		}else if(command.equalsIgnoreCase("inventoryremove"))
		{
			String result = player.inventoryRemove(event.getMessage().getAuthor(), "iron_ore", 1);
			if(result.equalsIgnoreCase("PlayerNotFoundError"))
			{
				event.getMessage().getChannel().sendMessage("Player could not be found.");
				break testMessages;
			}else if(result.equalsIgnoreCase("ItemNotFoundError"))
			{
				event.getMessage().getChannel().sendMessage("You do not have any of that item!");
				break testMessages;
			}else if(result.equalsIgnoreCase("ItemNumberError"))
			{
				event.getMessage().getChannel().sendMessage("You do not have enough of that item!");
				break testMessages;
			}else{
				event.getMessage().getChannel().sendMessage("One iron ore subtracted!");
			}
		}else if(command.equalsIgnoreCase("inventoryadd"))
		{
			String result = player.inventoryAdd(event.getMessage().getAuthor(), "iron_ore", 1);
			if(result.equalsIgnoreCase("PlayerNotFoundError"))
			{
				event.getMessage().getChannel().sendMessage("Player could not be found.");
				break testMessages;
			}else{
				event.getMessage().getChannel().sendMessage("One iron ore added.");
			}
		}
		*/
	}

	@EventSubscriber
	public static void userJoins(UserJoinEvent event) throws JSONException, IOException, MissingPermissionsException, HTTP429Exception, DiscordException
	{
		Player.create(event.getUser());
		event.getGuild().getChannels().get(0).sendMessage("A new traveler arrives, sword in hand.\n"
				+ event.getUser().mention() + ": Most functions take place via DM. Use .help anywhere in DM or this server to see commands available there.");
		
	}
	
	public static void main(String[] args) throws DiscordException, JSONException, IOException{
		IDiscordClient client = DiscordRPG.getClient("discordrpg@gmail.com", Password.getPass(), true);
		EventDispatcher dispatcher = client.getDispatcher();
	    dispatcher.registerListener(new DiscordRPG());
	    timer.schedule(ticker, 0, 1000);
	}

	public static String readFile( String file ) throws IOException {
	    BufferedReader reader = new BufferedReader( new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while( ( line = reader.readLine() ) != null ) {
	            stringBuilder.append( line );
	            stringBuilder.append( ls );
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
}

