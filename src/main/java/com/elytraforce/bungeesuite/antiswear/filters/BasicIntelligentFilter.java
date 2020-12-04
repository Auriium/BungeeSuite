package com.elytraforce.bungeesuite.antiswear.filters;

import com.elytraforce.bungeesuite.antiswear.Filter;
import com.elytraforce.bungeesuite.config.PluginConfig;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class BasicIntelligentFilter implements Filter{

	private String swearID(final String s) {
        final List<String> stringList = PluginConfig.get().getSwearWords();
        final List<String> stringList2 = PluginConfig.get().getSafeWords();

        String compet = StringUtils.normalizeSpace(s);
        compet = compet
                .replaceAll("@","a")
                .replaceAll("4","a")
                .replaceAll("3","e")
                .replaceAll("1", "i")
                .replaceAll("0","o");
        compet = Normalizer
                        .normalize(compet, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "");

        for (final String str : stringList) {
            if (this.checkWord(str, compet)) {
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
		return 12;
	}

}
