package eu.clarin.cmdi.virtualcollectionregistry;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.LinkedList;
import java.util.List;

public class UserInputSanitizer {

    public static String sanitize(String input) {
        String cleaned = "";
        //Remove html tags
        Safelist whitelist = Safelist.none();
        cleaned = Jsoup.clean(input, whitelist);
        //TODO: Protect against SQL injection
        return cleaned;
    }

    public static List<String> sanitizeList(List<String> input) {
        List<String> cleaned = new LinkedList<>();
        for(String in : input) {
            cleaned.add(sanitize(in));
        }
        return cleaned;
    }
}
