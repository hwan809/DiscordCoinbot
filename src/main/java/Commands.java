import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

public class Commands extends ListenerAdapter {

	public static final String[] NUM_EMOTES = {"U+25C0", "U+0031U+FE0FU+20E3", "U+0032U+FE0FU+20E3", "U+0033U+FE0FU+20E3","U+0034U+FE0FU+20E3",
												"U+0035U+FE0FU+20E3", "U+0036U+FE0FU+20E3", "U+0037U+FE0FU+20E3", "U+0038U+FE0FU+20E3", "U+0039U+FE0FU+20E3", "U+1F51F"};
	public static final String[] NUM_EMOTES_REAL = {"0️⃣", "1️⃣", "2️⃣", "3️⃣","4️⃣",
			"5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F"};

	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		TextChannel channel = e.getChannel();
		
		String m = e.getMessage().getContentRaw();
		String[] args = e.getMessage().getContentRaw().split("\\s+");

		User user = e.getMember().getUser();
		boolean isuser = Main.USERS.get(e.getMember().getUser().getAsTag()) != null;
		
		if (args[0].equalsIgnoreCase("!ㅋㅇ") || args[0].equalsIgnoreCase("!코인") || args[0].equalsIgnoreCase("!coin")) { 
			if (!isuser) {
				channel.sendMessage("유저 등록이 되어 있지 않습니다.\n **!등록**으로 참여해주세요.").queue(); return;
			}
			
			Coiner coiner = Main.USERS.get(e.getMember().getUser().getAsTag());
			
			if (args[1].equalsIgnoreCase("ㅊㅊ") || args[1].equalsIgnoreCase("추천") || args[1].equalsIgnoreCase("tier")) {

				channel.sendMessage(Coin.getPreferCoin(0).build()).queue(message -> {
					message.addReaction("U+25C0").queue();

					for (int a = 0; a <= 10; a++) {
						message.addReaction(NUM_EMOTES[a]).queue();
					}
					message.addReaction("U+25B6").queue();
				});

			} else if (args[1].equalsIgnoreCase("ㅈㄱ") || args[1].equalsIgnoreCase("잔고") || args[1].equalsIgnoreCase("deposit")) {

				EmbedBuilder avatarEmbed = new EmbedBuilder(); //Creates the embed.
				//Sets the contents of the embed
				avatarEmbed.setTitle(user.getName() + "님!", e.getGuild().getIconUrl());
				avatarEmbed.setColor(Color.GREEN);
				avatarEmbed.addField("[NickName]", e.getMember().getAsMention(), false);
				avatarEmbed.addField("[Deposit-KRW]", coiner.money + "$", false);

				avatarEmbed.setFooter(user.getAsTag(), e.getGuild().getIconUrl());
				channel.sendMessage(avatarEmbed.build()).queue();

			} else if (args[1].equalsIgnoreCase("정보") || args[1].equalsIgnoreCase("ㅈㅂ") || args[1].equalsIgnoreCase("info")) {
				if (args.length == 2) {
					channel.sendMessage("코인 단위를 입력하세요.").queue(); return;
				}
				if (Main.NAME_TO_COIN.get(args[2].trim()) != null) {
					Coin c = Main.NAME_TO_COIN.get(args[2].trim());

					EmbedBuilder eb = c.getInfo();
					channel.sendMessage(eb.build()).addFile(c.getGraph(), c.getSymbol().toUpperCase() + ".png").queue();
				} else {
					channel.sendMessage("그런 이름의 단위가 없습니다.").queue();
				}
			} else if (args[1].equalsIgnoreCase("매수") || args[1].equalsIgnoreCase("ㅁㅅ") || args[1].equalsIgnoreCase("buy") ||
					args[1].equalsIgnoreCase("매도") || args[1].equalsIgnoreCase("ㅁㄷ") || args[1].equalsIgnoreCase("sell") ||
					args[1].equalsIgnoreCase("풀매수") || args[1].equalsIgnoreCase("ㅍㅁㅅ") || args[1].equalsIgnoreCase("buyall") ||
					args[1].equalsIgnoreCase("풀매도") || args[1].equalsIgnoreCase("ㅍㅁㄷ") || args[1].equalsIgnoreCase("sellall")) {
				boolean isall = args[1].contains("ㅍ") || args[1].contains("풀") || args[1].contains("all");

				if (args.length == 2) {
					channel.sendMessage("코인 단위를 입력하세요.").queue(); return;
				} else if (args.length == 3 && !isall) {
					if (coiner.coinmode) {
						channel.sendMessage("살 코인의 개수를 입력하세요.").queue();
					} else {
						channel.sendMessage("꼬라박을 돈을 입력하세요.").queue();
					}
					
					return;
				}

				Coin c = Main.NAME_TO_COIN.get(args[2].trim());
				c.refreshPrice();

				if (c.getUSD() <= 0) {
					channel.sendMessage(user.getAsMention() + "님, 상장되지 않은 코인입니다...").queue();
					return;
				}

				float coinamount = 0f;
				float moneydif = 0f;
				float value = 0;
				
				try {
					if (!isall) value = Float.parseFloat(args[3]);
				} catch (Exception e2) {
					channel.sendMessage("숫자로 입력해주세요.").queue(); return;
				}

				boolean isbuy = args[1].contains("매수") || args[1].contains("ㅁㅅ") || args[1].contains("buy");

				c.refreshPrice();
				float coinprice = c.getUSD();

				if (isall) {
					if (isbuy) {
						moneydif = coiner.money;
						coinamount = coiner.money / coinprice;
					} else {
						try {
							moneydif = coiner.stocks_amount.get(c) * c.getUSD();
							coinamount = coiner.stocks_amount.get(c);

						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}

				} else {
					if (coiner.coinmode) {
						coinamount = value;
					} else {
						coinamount = value / coinprice;
					}

					moneydif = coinamount * coinprice;
				}

				if (moneydif < 10) {
					channel.sendMessage("거래량이 부족합니다. **10 USD** 이상부터 거래").queue(); return;
				}

				EmbedBuilder embedBuilder = new EmbedBuilder();

				String type = isbuy ? "매수" : "매도";
				Color color = isbuy ? Color.RED	: Color.GREEN;

				embedBuilder.setTitle(user.getName() + " - " + type);
				embedBuilder.setThumbnail(Main.IMAGE_BASELINK + c.getImageUrl());
				embedBuilder.setColor(color);
				embedBuilder.addField("[거래자]", e.getMember().getAsMention(), true);
				embedBuilder.addField("[거래 가격]", coinprice + "$", true);
				embedBuilder.addField("[지갑]", isbuy ? coiner.money - moneydif + "$" : coiner.money + moneydif + "$", true);
				embedBuilder.addField("[변동 코인량]", coinamount + " " + c.getSymbol(), true);

				int random = new Random().nextInt(6);
				System.out.println(random);

				embedBuilder.setImage("attachment://" + random + ".jpg");
				embedBuilder.setFooter(user.getAsTag(), e.getGuild().getIconUrl());

				if (args[1].contains("매수") || args[1].contains("ㅁㅅ") || args[1].contains("buy")) {
					if (moneydif > coiner.money) {
						channel.sendMessage(user.getAsMention() + "님, 돈이 부족합니다.").queue(); return;
					}
					coiner.money -= moneydif;
					if (coiner.stocks_amount.containsKey(c)) {
						float beforec = coiner.stocks_amount.get(c);
						float beforem = coiner.stocks_money.get(c);
						coiner.stocks_amount.put(c, beforec + coinamount);
						coiner.stocks_money.put(c, beforem + moneydif);
					} else {
						coiner.stocks_amount.put(c, coinamount);
						coiner.stocks_money.put(c, moneydif);
					}

					embedBuilder.addField("[총 코인 보유량]", coiner.stocks_amount.get(c) + " " + c.getSymbol(), true);
					channel.sendMessage(embedBuilder.build()).addFile(new File("data\\images\\buy\\" + random + ".jpg")).queue();
				} else if (args[1].contains("매도") || args[1].contains("ㅁㄷ") || args[1].contains("sell")) {
					if (coiner.stocks_amount.get(c) != null && coiner.stocks_amount.get(c) >= coinamount) {
						coiner.stocks_amount.put(c, coiner.stocks_amount.get(c) - coinamount);
						coiner.stocks_money.put(c, coiner.stocks_money.get(c) - moneydif);
						coiner.money += moneydif;

						embedBuilder.addField("[총 코인 보유량]", coiner.stocks_amount.get(c) + " " + c.getSymbol(), true);
						channel.sendMessage(embedBuilder.build()).addFile(new File("data\\images\\sell\\" + random + ".jpg")).queue();
					} else {
						channel.sendMessage(user.getAsMention() + "님, 구매한 코인량이 부족합니다.").queue();
					}
				}
			} else if (args[1].equalsIgnoreCase("ㄴㅋㅇ") || args[1].equalsIgnoreCase("내코인") || args[1].equalsIgnoreCase("mycoin")) {
				EmbedBuilder embed = new EmbedBuilder(); //Creates the embed.
				//Sets the contents of the embed
				embed.setTitle(":money_with_wings:내 코인:money_with_wings:");
				embed.setAuthor(user.getName() + "님의 코인");
				embed.setColor(Color.BLACK);

				int i = 0;

				for (Coin c : coiner.stocks_amount.keySet()) {
					c.refreshPrice();
					float percent = (c.getUSD() * coiner.stocks_amount.get(c) / coiner.stocks_money.get(c) - 1) * 100;
					String percentstr = (percent >= 0 ? Main.UP : Main.DOWN) + percent + "%";

					embed.addField((i + 1) + ". **" + c.getFullName() + "**",
							c.getSymbol() + " - **[구매량]** " + coiner.stocks_amount.get(c) + " - **[내 수익률]** " + percentstr + " - **[거래가]** " + c.getUSD() + "$", false);

					i++;
				}

				channel.sendMessage(embed.build()).queue();
			}

		} else if (args[0].equalsIgnoreCase("!ㄷㄹ") || args[0].equalsIgnoreCase("!등록") || args[0].equalsIgnoreCase("!join")) {
			if (isuser) return;

			Coiner coiner = new Coiner(user.getAsTag());
			Main.USERS.put(user.getAsTag(), coiner);
			
            EmbedBuilder avatarEmbed = new EmbedBuilder(); //Creates the embed.
            //Sets the contents of the embed
            avatarEmbed.setTitle(user.getName() + "님이 코인 거래소에 참여하셨습니다!", e.getGuild().getIconUrl());
            avatarEmbed.setColor(Color.GREEN);
            avatarEmbed.addField("[NickName]", e.getMember().getAsMention(), false);
            avatarEmbed.addField("[Deposit-KRW]", "10000$", false);
                
            avatarEmbed.setImage("https://i.ibb.co/QJGb4Lb/Kakao-Talk-20210513-122821147.png");
            avatarEmbed.setFooter(user.getAsTag(), e.getGuild().getIconUrl());
            channel.sendMessage(avatarEmbed.build()).queue(); //Send the embed as a message
		} else if (args[0].equalsIgnoreCase("!ㅁㄷ") || args[0].equalsIgnoreCase("!모드") || args[0].equalsIgnoreCase("!mode")) {
			if (!isuser) return;
			if (args.length == 1) return;

			Coiner coiner = Main.USERS.get(e.getMember().getUser().getAsTag());

			if (args[1].equalsIgnoreCase("거래") || args[1].equalsIgnoreCase("ㄱㄹ") || args[1].equalsIgnoreCase("trade")) {
				if (coiner.coinmode) {
					channel.sendMessage("거래 모드가 **통화량**으로 변경되었습니다.\n거래를 위해 지불할 통화량을 입력하세요.").queue();
					coiner.coinmode = false;
				} else {
					channel.sendMessage("거래 모드가 **코인량**으로 변경되었습니다.\n거래를 위해 구매할 코인량을 입력하세요.").queue();
					coiner.coinmode = true;
				}
			}
//			  else if (args[1].equalsIgnoreCase("그래프") || args[1].equalsIgnoreCase("ㄱㄹㅍ") || args[1].equalsIgnoreCase("graph")) {
//				if (coiner.default_currency.equals("USD")) {
//					channel.sendMessage("그래프 표시가 **KRW (₩)**으로 변경되었습니다.").queue();
//					coiner.default_currency = "KRW";
//				} else {
//					channel.sendMessage("그래프 표시가 **USD ($)**으로 변경되었습니다.").queue();
//					coiner.default_currency = "USD";
//				}
//			}
		} else if (args[0].equalsIgnoreCase("!동사") || args[0].equalsIgnoreCase("!ㄷㅅ") || args[0].equalsIgnoreCase("!verb")) {
			try {
				URL url = new URL("https://lt-nlgservice.herokuapp.com/rest/english/conjugate?verb=" + args[1]);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
				con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
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
					String result = (String) json.get("result");

					if (result.equals("OK")) {
						JSONArray data = (JSONArray) json.get("conjugated_forms");

						String p1 = data.get(1).toString().split("\"")[3].split("\"")[0];
						String p2 = data.get(2).toString().split("\"")[3].split("\"")[0];

						EmbedBuilder embedBuilder = new EmbedBuilder();

						embedBuilder.setTitle(args[1].toUpperCase());
						embedBuilder.setColor(Color.RED);
						embedBuilder.addField("[현재]", args[1].toLowerCase(), true);
						embedBuilder.addField("[과거]", p1, true);
						embedBuilder.addField("[과거완료]", p2, true);

						channel.sendMessage(embedBuilder.build()).queue();
					} else {
						channel.sendMessage("올바른 동사가 아닙니다").queue();
					}
				}
			} catch (Exception ee) {
				ee.printStackTrace();
				System.out.println("HOW");
			}

			Main.savePlayerData();
		}
    }

	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		TextChannel channel = e.getChannel();

		User user = e.getMember().getUser();
		boolean isuser = Main.USERS.get(e.getMember().getUser().getAsTag()) != null;

		Message message = e.getChannel().retrieveMessageById(e.getMessageId()).complete();
		if (message.getAuthor().isBot() && !e.getMember().getUser().isBot()) {
			String name = e.getReactionEmote().getAsCodepoints();
			if (message.getEmbeds().get(0).getTitle().contains("추천 코인") || message.getEmbeds().get(0).getTitle().contains("내 코인")) {
				MessageEmbed embedBuilder = message.getEmbeds().get(0);

				for (int i = 1; i <= 10; i++) {

					if (e.getReactionEmote().getName().equals(NUM_EMOTES_REAL[i])) {
						String symbol = embedBuilder.getFields().get(i - 1).getValue().split("-")[0].trim();

						if (Main.NAME_TO_COIN.get(symbol) != null) {
							Coin c = Main.NAME_TO_COIN.get(symbol);

							EmbedBuilder eb = c.getInfo();
							message.delete().queue();
							channel.sendMessage(eb.build()).addFile(c.getGraph(), c.getSymbol().toUpperCase() + ".png").queue();

						} else {
							channel.sendMessage("그런 이름의 단위가 없습니다.").queue();
						}
					}
				}
			} else {
				System.out.println("NO");
			}
		}
	}
}
