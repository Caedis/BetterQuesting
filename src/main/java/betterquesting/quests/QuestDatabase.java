package betterquesting.quests;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QuestDatabase
{
	/**
	 * This is used by UIs to query whether it needs refreshed</br>
	 * Set to false on UI init and when refreshed. It will be reset to true when required
	 */
	public static boolean updateUI = true;
	public static HashMap<Integer, QuestInstance> questDB = new HashMap<Integer, QuestInstance>();
	public static ArrayList<QuestLine> questLines = new ArrayList<QuestLine>();
	
	/**
	 * @return the next free ID within the quest database
	 */
	public static int getUniqueID()
	{
		int id = 0;
		
		while(questDB.containsKey(id))
		{
			id += 1;
		}
		
		return id;
	}
	
	public static void UpdateTasks(EntityPlayer player)
	{
		for(QuestInstance quest : questDB.values())
		{
			quest.Update(player);
		}
	}
	
	public static QuestInstance GetOrRegisterQuest(int id)
	{
		QuestInstance quest = getQuestByID(id);
		
		if(quest == null)
		{
			quest = new QuestInstance(id, true);
		}
		
		return quest;
	}
	
	public static QuestInstance getQuestByID(int id)
	{
		return questDB.get(id);
	}
	
	public static QuestInstance getQuestByOrder(int index)
	{
		if(index < 0 || index >= questDB.size())
		{
			return null;
		}
		
		return questDB.values().toArray(new QuestInstance[0])[index];
	}

	public static void DeleteQuest(int id)
	{
		QuestInstance quest = getQuestByID(id);
		questDB.remove(id);
		
		// Remove quest from quest lines and rebuild their trees
		for(QuestLine line : questLines)
		{
			line.questList.remove(quest);
			line.BuildTree();
		}
		
		for(QuestInstance qi : questDB.values())
		{
			qi.preRequisites.remove(quest);
		}
	}
	
	/**
	 * Updates all client's quest databases currently connected to the server.</br>
	 * <b>WARNING:</b> Network intensive! Avoid calling frequently (Use singular quest updates if possible)
	 */
	public static void UpdateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 0);
		JsonObject json = new JsonObject();
		QuestDatabase.writeToJson(json);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendToAll(new PacketQuesting(tags));
	}
	
	/**
	 * Updates specified client's quest databases currently connected to the server.</br>
	 * <b>WARNING:</b> Network intensive! Avoid calling frequently (Use singular quest updates if possible)
	 */
	public static void SendDatabase(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 0);
		JsonObject json = new JsonObject();
		QuestDatabase.writeToJson(json);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendTo(new PacketQuesting(tags), player);
	}
	
	public static void writeToJson(JsonObject json)
	{
		writeToJson_Quests(json);
		writeToJson_Lines(json);
	}
	
	public static void readFromJson(JsonObject json)
	{
		readFromJson_Quests(json);
		readFromJson_Lines(json);
	}
	
	public static void writeToJson_Lines(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(QuestLine line : questLines)
		{
			JsonObject tmp = new JsonObject();
			line.writeToJSON(tmp);
			jArray.add(tmp);
		}
		json.add("questLines", jArray);
	}
	
	public static void readFromJson_Lines(JsonObject json)
	{
		updateUI = true;
		questLines.clear();
		for(JsonElement entry : JsonHelper.GetArray(json, "questLines"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			QuestLine line = new QuestLine();
			line.readFromJSON(entry.getAsJsonObject());
			questLines.add(line);
		}
	}
	
	public static void writeToJson_Quests(JsonObject json)
	{
		JsonArray dbJson = new JsonArray();
		for(QuestInstance quest : questDB.values())
		{
			JsonObject questJson = new JsonObject();
			quest.writeToJSON(questJson);
			dbJson.add(questJson);
		}
		json.add("questDatabase", dbJson);
	}
	
	public static void readFromJson_Quests(JsonObject json)
	{
		if(json == null)
		{
			json = new JsonObject();
		}
		
		updateUI = true;
		questDB.clear();
		
		for(JsonElement entry : JsonHelper.GetArray(json, "questDatabase"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			int qID = JsonHelper.GetNumber(entry.getAsJsonObject(), "questID", -1).intValue();
			
			if(qID < 0)
			{
				continue;
			}
			
			QuestInstance quest = GetOrRegisterQuest(qID);
			quest.readFromJSON(entry.getAsJsonObject());
		}
	}
}
