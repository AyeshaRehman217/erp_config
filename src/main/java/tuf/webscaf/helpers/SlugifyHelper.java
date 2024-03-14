package tuf.webscaf.helpers;

import lombok.experimental.Helper;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Service
public class SlugifyHelper {

    public String slugify(String string){

        String result;
        // The normalize() method returns the Unicode Normalization Form of a given string.
        result = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        result = result
                .trim() // removing leading and trailing spaces
                .replaceAll(" ?- ?", "-") // remove spaces around hyphens
                .replaceAll("[ ']", "-") // turn spaces and quotes into hyphens
                .replaceAll("[^0-9a-zA-Z-]", "") // remove everything not in our allowed char set
                .replaceAll("-+", "-"); //Replace multiple Dashes with one dash

        // Removing dash from last
        if (result.endsWith("-")) {
            result = result.substring(0, result.length() - 1);
        }

        // Removing dash from start
        if (result.startsWith("-")) {
            result = result.substring(1);
        }

        result = result.toLowerCase();

        return result;
    }

    public Boolean validateSlug(String slug){
        String mSlug = slugify(slug);
        return mSlug.equals(slug);
//        return slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }
}
