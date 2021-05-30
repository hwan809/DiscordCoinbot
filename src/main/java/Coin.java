import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.OHLCChart;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.style.OHLCStyler;
import org.knowm.xchart.style.Styler;

public class Coin implements Serializable {
	private String symbol;
	private String imageurl;
	private String fullname;

	private float usdprice;
	
	private int ID;
	
	public Coin(String symbol, String imageurl, String fullname, int ID) {
		this.symbol = symbol;
		this.imageurl = imageurl;
		this.fullname = fullname;
		this.ID = ID;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public String getImageUrl() {
		return imageurl;
	}
	public String getFullName() {
		return fullname;
	}
	public int getId() {
		return ID;
	}
	public float getUSD() {
		return usdprice;
	}
	
	public EmbedBuilder getInfo() {
		refreshPrice();
		
		String usd = "";
		usd = new BigDecimal(Float.toString(this.usdprice)).toPlainString();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
		
		EmbedBuilder embed = new EmbedBuilder(); //Creates the embed.
        //Sets the contents of the embed
		embed.setAuthor("COIN - " + this.fullname, "https://www.cryptocompare.com/coins/" + symbol + "/overview/USD");
        embed.setColor(Color.YELLOW);
        embed.setThumbnail(Main.IMAGE_BASELINK + this.imageurl);
        
        embed.addField("[코인 단위]", symbol.toUpperCase(), true);
        embed.addField("[ID]", Integer.toString(ID), true);  
        embed.addField("[현재 거래가]", usd + "$", true);

        float percentage = get24Percent();
        String arrow = percentage >= 0 ? Main.UP : Main.DOWN;
        embed.addField("[24시간 수익률]", arrow + " " + percentage + "%", true);

        embed.setImage("attachment://" + this.symbol.toUpperCase() + ".png");
        embed.setFooter("Request made @ " + formatter.format(date));
        
        return embed;
	}
	
	public void refreshPrice() {
		try {
			URL url = new URL("https://min-api.cryptocompare.com/data/pricemulti?fsyms=" + this.symbol.toUpperCase() + "&tsyms=USD");
			HttpURLConnection con = (HttpURLConnection) url.openConnection(); 
			con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
			con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
			con.addRequestProperty("x-api-key", Main.API_KEY); //key값 설정
			con.setRequestMethod("GET");

			//URLConnection에 대한 doOutput 필드값을 지정된 값으로 설정한다. URL 연결은 입출력에 사용될 수 있다. URL 연결을 출력용으로 사용하려는 경우 DoOutput 플래그를 true로 설정하고, 그렇지 않은 경우는 false로 설정해야 한다. 기본값은 false이다.
            con.setDoOutput(false); 

			StringBuilder sb = new StringBuilder();
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				//Stream을 처리해줘야 하는 귀찮음이 있음. 
				BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();
				
				String jsontext = sb.toString();
				JSONObject json = new JSONObject(jsontext);
				
				JSONObject usd = (JSONObject) json.get(this.symbol.toUpperCase());
				this.usdprice = Float.parseFloat(usd.get("USD").toString());
			}
		} catch (Exception ee) {
			System.err.println(ee.toString());
		}
	}

	public float get24Percent() {
		try {
			//URL url = new URL("https://min-api.cryptocompare.com/data/generateAvg?fsym=" + this.symbol + "&tsym=USD&e=e=Kraken");
			URL url = new URL("https://min-api.cryptocompare.com/data/generateAvg?fsym=" + this.symbol.toUpperCase() + "&tsym=USD&e=Kraken");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
			con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
			con.addRequestProperty("x-api-key", Main.API_KEY); //key값 설정
			con.setRequestMethod("GET");

			//URLConnection에 대한 doOutput 필드값을 지정된 값으로 설정한다. URL 연결은 입출력에 사용될 수 있다. URL 연결을 출력용으로 사용하려는 경우 DoOutput 플래그를 true로 설정하고, 그렇지 않은 경우는 false로 설정해야 한다. 기본값은 false이다.
			con.setDoOutput(false);

			StringBuilder sb = new StringBuilder();
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				//Stream을 처리해줘야 하는 귀찮음이 있음.
				BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();

				String jsontext = sb.toString();
				JSONObject json = new JSONObject(jsontext);
				JSONObject raw = (JSONObject) json.get("DISPLAY");

				return Float.parseFloat(raw.get("CHANGEPCT24HOUR").toString());
			} else {
				JSONObject json = new JSONObject(con.getResponseMessage());
				JSONObject raw = (JSONObject) json.get("DISPLAY");

				return Float.parseFloat(raw.get("CHANGEPCT24HOUR").toString());
			}
		} catch (Exception ee) {
			ee.printStackTrace();
			System.out.println("HOW");
		}

		return 0f;
	}

	public File getGraph() {
		try {
			URL url = new URL("https://min-api.cryptocompare.com/data/v2/histominute?fsym=" + this.symbol.toUpperCase() + "&tsym=USD&limit=120&aggregate=1");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
			con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
			con.addRequestProperty("x-api-key", Main.API_KEY); //key값 설정
			con.setRequestMethod("GET");

			//URLConnection에 대한 doOutput 필드값을 지정된 값으로 설정한다. URL 연결은 입출력에 사용될 수 있다. URL 연결을 출력용으로 사용하려는 경우 DoOutput 플래그를 true로 설정하고, 그렇지 않은 경우는 false로 설정해야 한다. 기본값은 false이다.
            con.setDoOutput(false);

			StringBuilder sb = new StringBuilder();
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				//Stream을 처리해줘야 하는 귀찮음이 있음.
				BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();

				String jsontext = sb.toString();
				JSONObject json = new JSONObject(jsontext);

				JSONObject data1 = (JSONObject) json.get("Data");
				JSONArray data = (JSONArray) data1.get("Data");

				float[] times = new float[121];
				float[] opens = new float[121];
				float[] highs = new float[121];
				float[] lows = new float[121];
				float[] closes = new float[121];

				//Object
				for (int i = 0; i < data.length(); i++) {
					JSONObject result = (JSONObject) data.get(i);

					float time = Long.parseLong(result.get("time").toString());
					float open = Float.parseFloat(result.get("open").toString());
					float high = Float.parseFloat(result.get("high").toString());
					float low = Float.parseFloat(result.get("low").toString());
					float close = Float.parseFloat(result.get("close").toString());

					Calendar calendar = convertTimestamp(time);
					Date date = calendar.getTime();
					SimpleDateFormat sDate = new SimpleDateFormat("yyyyMMdd");

					times[i] = time;
					opens[i] = open;
					highs[i] = high;
					lows[i] = low;
					closes[i] = close;
				}

				try {
                    FileWriter file = new FileWriter("data/coin/" + this.symbol.toUpperCase() + ".json");
                    file.write(data.toString());
                    file.flush();
                    file.close();

					OHLCChart pricechart = new OHLCChartBuilder().width(1920).height(1080)
							.title(this.fullname + " - PRICE LAST 2H")
							.xAxisTitle("TIMESTAMP")
							.yAxisTitle("USD")
							.theme(Styler.ChartTheme.XChart)
							.build();

					OHLCStyler styler = pricechart.getStyler();

					styler.setChartBackgroundColor(new Color(0, 0, 0));
					styler.setChartTitleBoxBackgroundColor(new Color(0, 0, 0));
					styler.setChartTitleFont(Main.CAFE24.deriveFont(60f));
					styler.setChartFontColor(new Color(255, 255, 255));
					styler.setAxisTickLabelsFont(Main.CAFE24.deriveFont(15f));
					styler.setAxisTitleFont(Main.CAFE24.deriveFont(20f));
					styler.setAnnotationsFontColor(new Color(255, 255, 255));
					styler.setAxisTickLabelsColor(new Color(255, 255, 255));
					styler.setMarkerSize(30);
					styler.setPlotBackgroundColor(new Color(120, 120, 120));
					pricechart.addSeries(this.fullname + this.symbol, times, opens, highs, lows, closes)
							.setUpColor(new Color(210, 60, 75)).setDownColor(new Color(30, 95, 210));

					File chart = new File("data/coin/" + this.symbol.toUpperCase() + ".png");

					BitmapEncoder.saveBitmap(pricechart, chart.getPath(), BitmapEncoder.BitmapFormat.PNG);
					return chart;

				} catch (IOException ee) {
					System.err.println(ee.toString());
				}

			}
		} catch (Exception ee) {
			System.err.println(ee.toString());
		}

		return null;
	}

	public static Calendar convertTimestamp(float timestamp) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTimeInMillis((long) (timestamp * 1000));

	    return cal;
	}

	public static EmbedBuilder getPreferCoin(int page) {
		//추천코인 API 를! 사용하는 나

		try {
			URL url = new URL("https://min-api.cryptocompare.com/data/top/totaltoptiervolfull?limit=10&tsym=USD&page=" + page);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
			con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
			con.addRequestProperty("x-api-key", Main.API_KEY); //key값 설정
			con.setRequestMethod("GET");

			//URLConnection에 대한 doOutput 필드값을 지정된 값으로 설정한다. URL 연결은 입출력에 사용될 수 있다. URL 연결을 출력용으로 사용하려는 경우 DoOutput 플래그를 true로 설정하고, 그렇지 않은 경우는 false로 설정해야 한다. 기본값은 false이다.
			con.setDoOutput(false);

			StringBuilder sb = new StringBuilder();
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				//Stream을 처리해줘야 하는 귀찮음이 있음.
				BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), "utf-8"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();

				String jsontext = sb.toString();
				JSONObject json = new JSONObject(jsontext);
				JSONArray data = (JSONArray) json.get("Data");

				EmbedBuilder embed = new EmbedBuilder(); //Creates the embed.
				//Sets the contents of the embed
				embed.setTitle(":medal:추천 코인:medal:", "https://www.cryptocompare.com/coins/list/all/USD/" + page);
				embed.setAuthor("이 추천은 CRYPTOCOMPARE API 팀에서 이루어집니다.");
				embed.setColor(Color.MAGENTA);

				embed.setFooter("페이지 " + (page + 1) + "/5");

				for (int i = 0; i < data.length(); i++) {
					JSONObject tempcoin = (JSONObject) data.get(i);

					JSONObject coininfo = (JSONObject) tempcoin.get("CoinInfo");
					JSONObject display = (JSONObject) tempcoin.get("DISPLAY");

					String COINNAME = coininfo.get("FullName").toString();
					String SYMBOL = coininfo.get("Name").toString();

					float pct24h = Float.parseFloat(((JSONObject) display.get("USD"))
							.get("CHANGEPCT24HOUR").toString());
					String CHANGEPCT24HOUR = (pct24h >= 0 ? Main.UP : Main.DOWN) + pct24h;

					JSONObject allrating = (JSONObject) coininfo.get("Rating");
					JSONObject weiss = (JSONObject) allrating.get("Weiss");

					String COINEVAL = weiss.get("Rating").toString();

					embed.addField((i + 1) + ". **" + COINNAME + "**",
							SYMBOL + " - **[24시간 수익률]** " + CHANGEPCT24HOUR + " - **[코인 가치]** " + COINEVAL, false);
				}

				return embed;

			} else {
				System.out.println("WHY");
			}
		} catch (Exception ee) {
			System.out.println("HOW");
		}

		return null;
	}
}
