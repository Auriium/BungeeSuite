package com.elytraforce.bungeesuite.antiswear.filters;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.antiswear.Filter;

public class FilterOne implements Filter{

	private String swearID(final String s) {
        final List<String> stringList = Main.get().getConfig().getSwearWords();
        final List<String> stringList2 = Main.get().getConfig().getSafeWords();
        for (final String str : stringList) {
            if (this.checkWord(str, s)) {
                final Iterator<String> iterator2 = stringList2.iterator();
                while (iterator2.hasNext()) {
                    if (s.contains(iterator2.next())) {
                        return "false";
                    }
                }
                return "true " + str;
            }
        }
        return "false";
    }
    
    private boolean checkWord(final String s, final String input) {
        final char[] charArray = s.toCharArray();
        String string = "";
        char[] array;
        for (int length = (array = charArray).length, i = 0; i < length; ++i) {
            string = String.valueOf(string) + "[" + array[i] + "].{0," + 1 + "}";
        }
        return Pattern.compile(string, 2).matcher(input).find();
    }

	@Override
	public Boolean filter(String string) {
		// TODO Auto-generated method stub
        return swearID(string).startsWith("true");
	}

	@Override
	public FilterPriority getPriority() {
		return FilterPriority.HIGHEST;
	}

	@Override
	public Integer getVls() {
		return 10;
	}
	
}
