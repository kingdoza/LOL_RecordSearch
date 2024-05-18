package study1;
import java.util.*;
import java.text.*;
import java.time.*;
import java.io.*;
import java.net.*;


public class Hello {
	public static void main(String[] args) {
		try {
            // API URL with your API key
			String summonerName = "누상촌돗자리장수";
            String apiKey = "RGAPI-45c320b5-0ce5-4423-b992-87078604e6ed";
            String apiUrl = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + summonerName
            		+ "?api_key=" + apiKey;

            // URL object
            URL url = new URL(apiUrl);

            // HTTP connection setup
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Response code check
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // API response reading
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuffer response = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Response data processing
                System.out.println(response.toString());
            } else {
                System.out.println("API 호출 실패: " + responseCode);
            }

            // Connection close
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
