package betterquesting.quests.tasks;

import java.awt.Color;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TaskLocation extends TaskBase
{
	public String name = "New Location";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int dim = 0;
	public int range = -1;
	public boolean visible = false;
	public boolean hideInfo = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.location";
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(player.ticksExisted%20 != 0)
		{
			return; // Keeps ray casting calls to a minimum
		}
		
		Detect(player);
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
		if(isComplete(player))
		{
			return; // Keeps ray casting calls to a minimum
		}
		
		if(player.dimension == dim && (range <= 0 || player.getDistance(x, y, z) <= range))
		{
			if(visible && range > 0) // Do not do ray casting with infinite range!
			{
				Vec3 pPos = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3 tPos = Vec3.createVectorHelper(x, y, z);
				boolean liquids = false;
				MovingObjectPosition mop = player.worldObj.func_147447_a(pPos, tPos, liquids, !liquids, false);
				
				if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK)
				{
					this.completeUsers.add(player.getUniqueID());
				} else
				{
					return;
				}
			} else
			{
				this.completeUsers.add(player.getUniqueID());
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void drawQuestInfo(GuiQuesting screen, int mouseX, int mouseY, int posX, int posY, int sizeX, int sizeY)
	{
		int i = 0;

		screen.mc.fontRenderer.drawString(name, posX, posY + i, Color.BLACK.getRGB(), false);
		i += 12;
		
		if(!hideInfo)
		{
			if(range >= 0)
			{
				screen.mc.fontRenderer.drawString("Location: " + x + "," + y + "," + z, posX, posY + i, Color.BLACK.getRGB(), false);
				i += 12;
				screen.mc.fontRenderer.drawString("Range: " + range, posX, posY + i, Color.BLACK.getRGB(), false);
				i += 12;
			}
			
			screen.mc.fontRenderer.drawString("Dimension: " + dim, posX, posY + i, Color.BLACK.getRGB(), false);
			i += 12;
		}
		
		if(this.isComplete(screen.mc.thePlayer))
		{
			screen.mc.fontRenderer.drawString("Found!", posX, posY + i, Color.GREEN.getRGB(), false);
		} else
		{
			screen.mc.fontRenderer.drawString("Undiscovered", posX, posY + i, Color.RED.getRGB(), false);
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("name", name);
		json.addProperty("posX", x);
		json.addProperty("posY", y);
		json.addProperty("posZ", z);
		json.addProperty("dimension", dim);
		json.addProperty("range", range);
		json.addProperty("visible", visible);
		json.addProperty("hideInfo", hideInfo);
		
		super.writeToJson(json);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		name = JsonHelper.GetString(json, "name", "New Location");
		x = JsonHelper.GetNumber(json, "posX", 0).intValue();
		y = JsonHelper.GetNumber(json, "posY", 0).intValue();
		z = JsonHelper.GetNumber(json, "posZ", 0).intValue();
		dim = JsonHelper.GetNumber(json, "dimension", 0).intValue();
		range = JsonHelper.GetNumber(json, "range", -1).intValue();
		visible = JsonHelper.GetBoolean(json, "visible", false);
		hideInfo = JsonHelper.GetBoolean(json, "hideInfo", false);
	}
}
