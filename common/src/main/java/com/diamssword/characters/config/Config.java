package com.diamssword.characters.config;

import com.diamssword.characters.Characters;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Config {

	public Server serverOptions = new Server();

	public static class Server {
		public String SkinServerURL = "https://resurgence.info";
		public String ServerSideApiKey = "";
		public String defaultModIdentifierForTextures= Characters.MOD_ID;
		public GuiConfig[] customBodyPartGuiEditor=new GuiConfig[]{new GuiConfig("makeup",new String[]{"cosmetic","brows"},true)};
		public static record GuiConfig(String id,String[] allowedLayers,boolean startCloseUp) {
		}
		public List<String> getGuiNames()
		{
			var ls=new ArrayList<String>();
			for(GuiConfig guiConfig : customBodyPartGuiEditor) {
				ls.add(guiConfig.id);
			}
			return ls;
		}
		public GuiConfig findGuiConfig(String id)
		{
			for(GuiConfig guiConfig : customBodyPartGuiEditor) {
				if(guiConfig.id.equals(id))
					return guiConfig;
			}
			return null;
		}
	}


}
