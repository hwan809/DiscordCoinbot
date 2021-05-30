import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Coiner implements Serializable {
	
	private String tag;

	public float money = 10000;

	public Map<Coin, Float> stocks_amount = new HashMap<>();
	public Map<Coin, Float> stocks_money = new HashMap<>();
	public ArrayList<Coin> interests = new ArrayList<>();
	public boolean coinmode = true;
	
	public Coiner(String tag) {
		this.tag = tag;
	}
	
	public Coiner(String tag, float money, Map<Coin, Float> stocks_amount, Map<Coin, Float> stocks_money, ArrayList<Coin> interests, boolean coinmode) {
		this.tag = tag;
		
		this.money = money;
		this.stocks_amount = stocks_amount;
		this.stocks_money = stocks_money;
		this.interests = interests;
		this.coinmode = coinmode;
	}

	public Map<Coin, Float> getStocks_amount() {
		return stocks_amount;
	}

	public void setStocks_amount(Map<Coin, Float> stocks_amount) {
		this.stocks_amount = stocks_amount;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public float getMoney() {
		return money;
	}

	public void setMoney(float money) {
		this.money = money;
	}

	public Map<Coin, Float> getStocks_money() {
		return stocks_money;
	}

	public void setStocks_money(Map<Coin, Float> stocks_money) {
		this.stocks_money = stocks_money;
	}

	public ArrayList<Coin> getInterests() {
		return interests;
	}

	public void setInterests(ArrayList<Coin> interests) {
		this.interests = interests;
	}

	public boolean isCoinmode() {
		return coinmode;
	}

	public void setCoinmode(boolean coinmode) {
		this.coinmode = coinmode;
	}
}
