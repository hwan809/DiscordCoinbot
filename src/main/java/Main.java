import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
	public static JDA jda;
	public static final String DISCORD_TOKEN = "NzUxNjQ0NzU0NjAwNzg4MDY4.X1MFug.IlGb_bZ7PsEAWzD_KlnvDz0Ie_Q";
	public static final String API_KEY = "6d0e860dcea2999cc0d67cc8a5132a5502e55a5a8ed4bc09f4b5fb48129b04a7";
	public static final String IMAGE_BASELINK = "https://www.cryptocompare.com";

	public static final String UP = ":small_red_triangle:";
	public static final String DOWN = ":small_red_triangle_down:";

	InputStream fontStream;
	public static Font BLACK_HAN_SANS_REGULAR;
	public static Font CAFE24;

	static {
		try {
			BLACK_HAN_SANS_REGULAR = Font.createFont(Font.TRUETYPE_FONT, new File("C:\\Users\\KOREA\\intellij-workspace\\Coinbot\\src\\main\\resources\\BlackHanSans-Regular.ttf")).deriveFont(30f);
			CAFE24 = Font.createFont(Font.TRUETYPE_FONT, new File("C:\\Users\\KOREA\\intellij-workspace\\Coinbot\\src\\main\\resources\\Cafe24Dangdanghae.ttf")).deriveFont(30f);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	public static JSONObject array;
	public static ArrayList<String> NAMES = new ArrayList<>();
	public static Map<String, Coin> NAME_TO_COIN = new HashMap<String, Coin>();
	
	public static Map<String, Coiner> USERS = new HashMap<String, Coiner>();
	
	public static void main(String[] args) throws LoginException, IOException, FontFormatException {
		
		try {
            JDABuilder builder = JDABuilder.createDefault(DISCORD_TOKEN);
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
            builder.addEventListeners(new Commands());
            jda = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

		getPlayerData();
		
		try {
			URL url = new URL("https://min-api.cryptocompare.com/data/all/coinlist?summary=true");
			HttpURLConnection con = (HttpURLConnection) url.openConnection(); 
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.addRequestProperty("x-api-key", Main.API_KEY);
			con.setRequestMethod("GET");

            con.setDoOutput(false);

			StringBuilder sb = new StringBuilder();
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();
				
				String jsontext = sb.toString();
				JSONObject json = new JSONObject(jsontext);
				
				array = (JSONObject) json.get("Data");
				
				try {
                    FileWriter file = new FileWriter("data\\coin.json");
                    file.write(array.toString());
                    file.flush();
                    file.close();
				} catch (IOException ee) {
                    ee.printStackTrace();
				}

				for (String key : array.keySet()) {
					Object value = array.get(key);
					if (value instanceof JSONObject) {
						JSONObject coin = ((JSONObject) value);
						Coin c;

						if (!coin.isNull("ImageUrl")) {
							c = new Coin(coin.get("Symbol").toString(), coin.get("ImageUrl").toString(),
									coin.get("FullName").toString(), Integer.parseInt(coin.get("Id").toString()));
						} else {
							c = new Coin(coin.get("Symbol").toString(), "",
									coin.get("FullName").toString(), Integer.parseInt(coin.get("Id").toString()));
						}

						NAMES.add(key);
						NAME_TO_COIN.put(key, c);
					}
				}
			}

		} catch (Exception ee) {
			System.err.println(ee.toString());
		}
	}

	public static void savePlayerData() {
		try {
			//TODO - json으로 Coiner class 저장
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getPlayerData() {
		try {
			File f = new File("data/userdata.json");

			if (f.exists()) {

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
