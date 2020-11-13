package ruisun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class DictionaryModel {
    /*
     * Search Flickr.com for the searchTerm argument, and return a Bitmap that can be put in an ImageView
     */
    public String[] search(String searchTerm) throws IOException {
        String endpoint = "entries";
        String language_code = "en-us";
        // Create a neat value object to hold the URL
        URL url = new URL( "https://od-api.oxforddictionaries.com/api/v2/" + endpoint + "/" + language_code + "/" + searchTerm);

        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestMethod("GET");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("app_id", "ab20e008");
        connection.setRequestProperty("app_key", "deb2417a7c88c711830f0b0fb67c7c1f");

        // This line makes the request
        try {
            InputStream responseStream = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            String str, response = "";
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response += str;
            }
            in.close();
            return parse(response);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private String[] parse(String response) {
        // Finally we have the response
        JSONObject responseMap = JSON.parseObject(response, JSONObject.class);
        ArrayList<JSONObject> lexicalEntries = new ArrayList<>();
        for (Object json: (JSONArray)responseMap.get("results")) {
            if (!json.getClass().equals(JSONObject.class)) continue;
            for (Object lexicalEntry: (JSONArray) ((JSONObject) json).get("lexicalEntries")) {
                lexicalEntries.add((JSONObject) lexicalEntry);
            }
        }
        ArrayList<JSONObject> entries = new ArrayList<>();
        for (JSONObject lexicalEntry: lexicalEntries) {
            for (Object entry: (JSONArray) lexicalEntry.get("entries")) {
                entries.add((JSONObject) entry);
            }
        }
        ArrayList<JSONObject> senses = new ArrayList<>();
        for (JSONObject entry: entries) {
            for (Object sense: (JSONArray) entry.get("senses")) {
                senses.add((JSONObject) sense);
            }
        }
        ArrayList<String> definitions = new ArrayList<>();
        for (JSONObject sense: senses) {
            if (sense.get("definitions") == null) return null;
            for (Object definition: (JSONArray) sense.get("definitions")) {
                definitions.add((String) definition);
            }
        }
        String[] ans = new String[definitions.size()];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = (i + 1) + ". " + definitions.get(i);
        }
        return ans;
    }

}
