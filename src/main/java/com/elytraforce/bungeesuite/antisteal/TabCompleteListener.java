package com.elytraforce.bungeesuite.antisteal;

import java.util.ArrayList;

import java.util.List;
import java.util.logging.Level;

import com.elytraforce.bungeesuite.Main;
import com.mojang.brigadier.tree.RootCommandNode;

import de.exceptionflug.protocolize.api.event.PacketSendEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import net.md_5.bungee.protocol.packet.Commands;

public class TabCompleteListener extends PacketAdapter<Commands> {
	
	private Main plugin;
	
	@SuppressWarnings("unchecked")
	public TabCompleteListener(Main main) {
        super(Stream.UPSTREAM, (Class)Commands.class);
        this.plugin = main;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void send(final PacketSendEvent<Commands> packetSendEvent) {
			Main.get().getLogger().log(Level.SEVERE, "calling event!");
		
            RootCommandNode root = ((Commands)packetSendEvent.getPacket()).getRoot();

            ArrayList list = new ArrayList();
            
            root.getChildren().removeAll(list);
            ((Commands)packetSendEvent.getPacket()).setRoot(root);
    }
	
	public boolean equalsIgnoreCase(final List<String> list, final String searchString) {
        if (list == null || searchString == null) {
            return false;
        }
        if (searchString.isEmpty()) {
            return true;
        }
        for (final String string : list) {
            if (string == null) {
                continue;
            }
            if (string.equalsIgnoreCase(searchString)) {
                return true;
            }
        }
        return false;
    }

}
