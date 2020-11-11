package com.elytraforce.bungeesuite.util;

import net.md_5.bungee.api.ChatColor;


public class AuriBungeeUtil {
	
	private final static int CENTER_PX = 154;
	
	public static String centerMessage(String message) {
		if (message == null || message.equals("")) return "";
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
                if(c == '§'){
                        previousCode = true;
                        continue;
                }else if(previousCode == true){
                        previousCode = false;
                        if(c == 'l' || c == 'L'){
                                isBold = true;
                                continue;
                        }else isBold = false;
                }else{
                        DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                        messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                        messagePxSize++;
                }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
                sb.append(" ");
                compensated += spaceLength;
        }
        
        return sb.toString() + message;
	}
	
	public static String centerMOTD(String text, int lineLength) {
		String pex = ChatColor.translateAlternateColorCodes('&', text);
        char[] chars = pex.toCharArray(); // Get a list of all characters in text
        boolean isBold = false;
        double length = 0;
        ChatColor pholder = null;
        for (int i = 0; i < chars.length; i++) { // Loop through all characters
            // Check if the character is a ColorCode..
            if (chars[i] == '&' && chars.length != (i + 1) && (pholder = ChatColor.getByChar(chars[i + 1])) != null) {
                if (pholder != ChatColor.UNDERLINE && pholder != ChatColor.ITALIC
                        && pholder != ChatColor.STRIKETHROUGH && pholder != ChatColor.MAGIC) {
                    isBold = (chars[i + 1] == 'l'); // Setting bold  to true or false, depending on if the ChatColor is Bold.
                    i += isBold ? 1 : 0;
                }
            } else {
                // If the character is not a color code:
                length++; // Adding a space
                length += (isBold ? (chars[i] != ' ' ? 0.1555555555555556 : 0) : 0); // Adding 0.156 spaces if the character is bold.
            }
        }

        double spaces = (lineLength - length) / 2; // Getting the spaces to add by (max line length - length) / 2

        // Adding the spaces
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            builder.append(' ');
        }
        String copy = builder.toString();
        builder.append(pex).append(copy);

        return builder.toString();
    }

}
