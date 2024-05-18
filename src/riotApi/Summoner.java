package riotApi;
import java.util.*;
import java.util.regex.Matcher;
import java.text.*;
import java.time.*;
import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import json.*;
import static java.lang.Math.*;

abstract class RApi {
	private static String apiKey = "RGAPI-ab71529d-8a98-4eed-b550-20dd6a8cb654";
	private static String apiUrlSummoner = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/";
	protected static String apiUrlLeague = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/";
	protected static String apiUrlMatch = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/";
	protected static String apiUrlMatchPara = "/ids?start=0&count=";
	protected static String apiUrlMatchId = "https://asia.api.riotgames.com/lol/match/v5/matches/";
	private static String apiUrlBack = "?api_key=" + apiKey;
	private static String apiUrlBackPara = "&api_key=" + apiKey;
	
	public static Summoner initSummoner(String summonerName) throws InvalidRequestException {
		String responseString = checkValidRequest(getResponse(false, apiUrlSummoner, summonerName));
		Map<String, String> response = parseJSON(responseString);
		Summoner summoner = new Summoner(response);
		return summoner;
	}
	
	protected static String getResponse(Boolean hasPara, String...urls ) {
		String response = "";
		try {
			String urlString = "";
			for(String str : urls)
				urlString += str;
			if(hasPara) urlString += apiUrlBackPara;
			else urlString += apiUrlBack;
			URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuffer responseBuffer = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                	responseBuffer.append(line);
                }
                reader.close();
                response = responseBuffer.toString();
            } 
            else {
                System.out.println("API 호출 실패: " + responseCode);
            }
		} catch (IOException e) {e.printStackTrace();}
		return response;
	}
	
	protected static Map<String, String> parseJSON(String resonseString) {
		JSONObject jsonObject = new JSONObject(resonseString);
		Map<String, String> map = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            map.put(key, jsonObject.get(key).toString());
        }
        return map;
    }
	
	protected static String checkValidRequest(String responseString) throws InvalidRequestException {
		if(responseString.equals("")) throw new InvalidRequestException();
		return responseString;
	}
	
	protected static String checkFoundMatch(String responseString) throws UnFoundMatchException {
		if(responseString.equals("")) throw new UnFoundMatchException();
		return responseString;
	}
	
	protected String getDate(long timeInMillis) {
		Date date = new Date(timeInMillis);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(date);
	}
	
	protected JSONObject getJsonObject(String responseString, String targetKey, String targetValue) {
		JSONArray jsonArray = new JSONArray(responseString);
        
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String value = jsonObject.getString(targetKey);
            if(value.equals(targetValue)) {
            	return jsonObject;
            }
        }
        return null;
	}
}

public class Summoner extends RApi {
	private String id = "";
	private String accountID = "";
	private String puuid = "";
	private String name = "";
	private int level = 0;
	private long lastPlayedTime = 0;
	private String tier = "UNRANKED";
	private String rank = "";
	private int wins = 0;
	private int losses = 0;
	private int matchNum = 10;
	private ArrayList<Match> matches = new ArrayList<Match>();
	
	Summoner(Map<String, String> response) {
		setSummoner(response);
		setLeague();
		setMatches();
	}
	
	private void setSummoner(Map<String, String> response) {
		this.id = response.get("id");
		this.accountID = response.get("accountID");
		this.puuid = response.get("puuid");
		this.name = response.get("name");
		this.level = Integer.parseInt(response.get("summonerLevel"));
		this.lastPlayedTime = Long.parseLong(response.get("revisionDate"));
	}
	
	private void setLeague() {        
        String responseString = getResponse(false, apiUrlLeague, id);
        JSONObject jsonObject = getJsonObject(responseString, "queueType", "RANKED_SOLO_5x5");
        
        if(jsonObject != null) {
        	tier = jsonObject.getString("tier");
            rank = jsonObject.getString("rank");
            wins = jsonObject.getInt("wins");
            losses = jsonObject.getInt("losses");
        }
	}
	
	public void setMatches() {
		try {
			String responseString = checkFoundMatch(getResponse(true, apiUrlMatch, String.valueOf(puuid), apiUrlMatchPara, String.valueOf(matchNum)));
			JSONArray jsonArray = new JSONArray(responseString);
			for (int i = 0; i < jsonArray.length(); i++) {
				matches.add(new Match(jsonArray.getString(i), name));
			}
		} catch(UnFoundMatchException e) {}
	}
	
	public void printMatches() {
		System.out.println();
		System.out.println();
		for(int i = 0; i < matches.size(); ++i) {
			System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
			System.out.printf("%-3d", i + 1);
			matches.get(i).printInfo();
		}
		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
		System.out.println();
		System.out.println();
		System.out.println();
	}
	
	public void printInfo() {
		System.out.println("이름 : " + name);
		System.out.println("레벨 : " + level);
		System.out.println("티어 : " + tier + " " + rank);
		System.out.printf("솔로랭크 : %d승 %d패(%.2f%%)\n", wins, losses, 100 * getWinRate());
		System.out.printf("마지막 게임 : %s\n", getDate(lastPlayedTime));
		System.out.println();
	}
	
	public Match getMatch(String matchIndexString) {
		try {
			int matchIndex = Integer.parseInt(matchIndexString);
			Match match = matches.get(matchIndex - 1);
			if(match.getGameMode().equals("etc")) throw new Exception();
			return match;
		} catch(Exception e) {return null;}
	}
	
	public void setMatch(String matchId) throws InvalidRequestException {
		String reponseString = checkValidRequest(getResponse(false, apiUrlMatchId, matchId));
	}
	
	private double getWinRate() {
		int totalGame = wins + losses;
		if(totalGame == 0)
			return 0;
		return (double)wins / totalGame;
 	}
	public String getName() {return name;}
	public String getId() {return id;}
	public String getPuuid() {return puuid;}
	public int getMatchNum() {return matches.size();}
}

class Match extends RApi {
	private String summonerName = "";
	private String id = "";
	private long gameStartTime = 0;
	private int gameDuration = 0;
	private int queueId = 0;
	private String gameMode = "etc";
	//private ArrayList<Player> players = new ArrayList<Player>();
	//private ArrayList<Player> winPlayers = new ArrayList<Player>();
	//private ArrayList<Player> lossPlayers = new ArrayList<Player>();
	
	private Team winnerTeam = new Team(true);
	private Team losserTeam = new Team(false);
	
	private Player playerSelf;
	JSONObject infoObject = null;
	
	Match(String id, String summonerName) throws UnFoundMatchException {
		this.id = id;
		this.summonerName = summonerName;
		setInfo();
		setGameMode();
		setPlayers();
	}
	
	private void setInfo() throws UnFoundMatchException {
		String responseString = checkFoundMatch(getResponse(false, apiUrlMatchId, id));
		
		infoObject = new JSONObject(new JSONObject(responseString).get("info").toString());
		gameStartTime = infoObject.getLong("gameStartTimestamp");
		gameDuration = infoObject.getInt("gameDuration");
		queueId = infoObject.getInt("queueId");
	}
	
	private void setPlayers() {
		JSONArray jsonArray = new JSONArray(infoObject.get("participants").toString());
		for(int i = 0; i < jsonArray.length(); ++i) {
			Player player = new Player(jsonArray.getJSONObject(i));
			if(player.getWinLoss()) {
				winnerTeam.addPlayer(player);
			}
			else {
				losserTeam.addPlayer(player);
			}
			if(player.getName().equals(summonerName)) {
				playerSelf = player;
				player.setSelf();
			}
		}
		winnerTeam.setPlayers();
		losserTeam.setPlayers();
	}
	
	public String getId() {return id;}
	public String getGameMode() {return gameMode;}
	
	public void printInfo() {
		System.out.printf("%-8s", "[" + playerSelf.getWinLossString() + "]");
		System.out.printf("%-12s", "[" + gameMode + "]");
		System.out.printf("%-16s", "[" + playerSelf.getChampion() + "]");
		System.out.printf("%-17s", "[" + playerSelf.getKDAString() + "]");
		System.out.printf("%-12s", "[" + playerSelf.getCsString() + "]");
		System.out.printf("%-10s", "[" + getDuration() + "]");
		System.out.printf("[" + getDate(gameStartTime) + "]");
		System.out.println();
	}
	
	public void printDetails() {
		/*
		Player.printChampion("[WIN] AVG : ");
		Player.printDamage(round(winTeamDamageDealt / winPlayers.size()) + " (20%)");
		Player.printPercent(round(100 * winTeamDamageDealt / winTeamGold) + " ");
		Player.print100thousand(round(winTeamDamageDealt / winTeamKills) + " ");
		Player.printDamage(round(winTeamDamageTaken / winPlayers.size()) + " (20%)");
		Player.printPercent(round(100 * winTeamDamageTaken / winTeamDeaths) + " ");
		Player.print100thousand(round(winTeamCCing / winPlayers.size()) + " "); */
		System.out.println();
		System.out.println();
		System.out.println();
		printInfo();
		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
		printCommentary();
		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
		printHeader();
		System.out.println();
		winnerTeam.printInfo();
		System.out.println();
		losserTeam.printInfo();
		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
		System.out.println();
		System.out.println();
		/*
		Player.printChampion("[WIN] AVG : ");
		for(int i = 0; i < winnerTeam.size(); ++i) {
			winnerTeam.getPlayer(i).printInfo();
		}
		System.out.println();
		
		System.out.println("[LOSS] AVG : ");
		for(int i = 0; i < losserTeam.size(); ++i) {
			losserTeam.getPlayer(i).printInfo();
		} */
	}
	
	private void printHeader() {
		Player.printChampion("CHAMPION");
		Player.printDamage("DEALT (RATE)");
		Player.printPercent("DPG");
		Player.print100thousand("DPK");
		Player.printDamage("TAKEN (RATE)");
		Player.print100thousand("TPD");
		Player.printThousand("CC");
		System.out.print("NAME");
		System.out.println();
	}
	
	private void printCommentary() {
		System.out.printf("%-20s", "DEALT : 가한 딜량");
		System.out.printf("%-20s", "RATE : 팀 내 비중");
		System.out.printf("%-20s", "DPG : 골드당 딜량");
		System.out.printf("%-20s", "DPK : 킬당 딜량");
		System.out.println();
		System.out.printf("%-20s", "TAKEN : 받은 피해량");
		System.out.printf("%-20s", "RATE : 팀 내 비중");
		System.out.printf("%-20s", "TPD : 데스당 피해량");
		System.out.printf("%-20s", "CC : 적이 받은 CC기 시간");
		System.out.println();
	}
	
	private String getDuration() {
		int hours = gameDuration / 60;
		int mins = gameDuration % 60;
		return String.format("%d분%d초", hours, mins);
	}
	
	private void setGameMode() {
		switch (queueId) {
		case 400:
		case 430: gameMode = "Normal"; return;
		case 420: gameMode = "SoloRank"; return;
		case 440: gameMode = "FlexRank"; return;
		case 450: gameMode = "ARAM"; return;
		}
	}
}

class Team {
	private boolean isWin = false;
	private int totalDamageDealt = 0;
	private int totalDamageTaken = 0;
	private int totalGold = 0;
	private int totalKills = 0;
	private int totalDeaths = 0;
	private int totalCCing = 0;
	
	Team(boolean isWin) {
		this.isWin = isWin;
	}
	
	private ArrayList<Player> players = new ArrayList<Player>();
	
	private void addStat(Player player) {
		totalDamageDealt += player.getDamageDealt();
		totalDamageTaken += player.getDamageTaken();
		totalGold += player.getGold();
		totalKills += player.getKills();
		totalDeaths += player.getDeaths();
		totalCCing += player.getCCing();
	}
	
	public void addPlayer(Player player) {
		players.add(player);
		addStat(player);
	}
	
	public void printInfo() {
		printAverage();
		printPlayers();
	}
	
	private void printAverage() {
		DecimalFormat df = new DecimalFormat("###,###");
		if(isWin) {
			System.out.print(Color.brightCyan);
			Player.printChampion("[WIN] AVG : ");
		}
		else {
			System.out.print(Color.brightRed);
			Player.printChampion("[LOSE] AVG : ");
		}
		Player.printDamage(df.format(totalDamageDealt / size()) + " (" + round(100 / size()) + "%)");

		Player.printPercent(Player.toPercent(Player.valuePerDivison(totalDamageDealt, totalGold, 2)));
		Player.print100thousand(df.format((int)round(Player.valuePerDivison(totalDamageDealt, totalKills, 1))));
		Player.printDamage(df.format(totalDamageTaken / size()) + " (" + round(100 / size()) + "%)");
		String dpd = Player.checkPerfect(Player.valuePerDivison(totalDamageTaken, totalDeaths, 1), true);
		try { 
			dpd = df.format(Integer.parseInt(dpd));
		} catch(NumberFormatException e) {
		} finally {Player.print100thousand(dpd);}
		Player.printThousand(String.valueOf(round(totalCCing / size())) + "s");
		System.out.println(Color.exit);
	}
	
	private void printPlayers() {
		for(Player player : players) {
			if(player.getSelg()) {
				System.out.print(Color.yellow);
			}
			player.printInfo();
			System.out.print(Color.exit);
		}
	}
	
	public Player getPlayer(int i) {return players.get(i);}
	public int size() {return players.size();}
	public int getDamageDealt() {return totalDamageDealt;}
	public int getDamageTaken() {return totalDamageTaken;}
	
	public void setPlayers() {
		for(Player player : players) {
			player.setDamageDealtRate(totalDamageDealt);
			player.setDamageTakenRate(totalDamageTaken);
		}
	}
}

class Player {
	private String champion = "";
	private String summonerName = "";
	private boolean isWin = false;
	private double playedMinutes = 0;
	private int kills = 0;
	private int deaths = 0;
	private int assists = 0;
	private int creepScore = 0;
	private int gold = 0;
	private int damageDealt = 0;
	private int damageTaken = 0;
	private int ccingOthers = 0;
	
	private double damageDealtRate = 0;
	private double damageTakenRate = 0;
	
	private boolean isSelf = false;
	
	Player(JSONObject jsonObject) {
		champion = jsonObject.getString("championName");
		summonerName = jsonObject.getString("summonerName");
		isWin = jsonObject.getBoolean("win");
		playedMinutes = jsonObject.getInt("timePlayed") / 60.0;
		kills = jsonObject.getInt("kills");
		deaths = jsonObject.getInt("deaths");
		assists = jsonObject.getInt("assists");
		creepScore = jsonObject.getInt("totalMinionsKilled");
		gold = jsonObject.getInt("goldEarned");
		damageDealt = jsonObject.getInt("totalDamageDealtToChampions");
		damageTaken = jsonObject.getInt("totalDamageTaken");
		ccingOthers = jsonObject.getInt("timeCCingOthers");
	}
	
	public String getWinLossString() { return isWin ? "WIN" : "LOSE";}
	public boolean getWinLoss() { return isWin;}
	
	public String getCsString() {
		String cs = String.valueOf(creepScore);
		cs += "(" + valuePerDivison(creepScore, playedMinutes, 1) + ")";
		return cs;
	}
	
	public String getChampion() {return champion;}
	
	public String getKDAString() {
		String kda = String.format("%d/%d/%d", kills, deaths, assists);
		kda += "(" + checkPerfect(valuePerDivison(kills + assists, deaths, 1), false) + ")";
		return kda;
	}
	public String getName() {return summonerName;}
	
	public int getDamageDealt() {return damageDealt;}
	
	public int getDamageTaken() {return damageTaken;}
	
	public int getGold() {return gold;}
	
	public int getKills() {return kills;}
	
	public int getDeaths() {return deaths;}
	
	public int getCCing() {return ccingOthers;}
	
	public void printInfo() {
		DecimalFormat df = new DecimalFormat("###,###");
		printChampion(champion);
		printDamage(df.format(damageDealt) + " (" + toPercent(damageDealtRate) + ")");
		printPercent(toPercent(valuePerDivison(damageDealt, gold, 2)));
		print100thousand(df.format((int)round(valuePerDivison(damageDealt, kills, 1))));
		printDamage(df.format(damageTaken) + " (" + toPercent(damageTakenRate) + ")");
		String dpd = checkPerfect(valuePerDivison(damageTaken, deaths, 1), true);
		try { 
			dpd = df.format(Integer.parseInt(dpd));
		} catch(NumberFormatException e) {
		} finally {print100thousand(dpd);}
		printThousand(String.valueOf(ccingOthers) +"s");
		System.out.printf("%s", summonerName);
		System.out.println();
	}
	
	public void setDamageDealtRate(int totalDamageDealt) {
		damageDealtRate = (double)damageDealt / totalDamageDealt;
	}
	
	public void setDamageTakenRate(int totalDamageTaken) {
		damageTakenRate = (double)damageTaken / totalDamageTaken;
	}
	
	public void setSelf() {isSelf = true;}
	public boolean getSelg() {return isSelf;}
	
	public static double valuePerDivison(int value, int division, int decimalPoint) {
		if(division == 0)
			return 0;
		double powerOfTen = (double)Math.pow(10, decimalPoint);
		return Math.round(powerOfTen * value / division) / powerOfTen;
	}
	
	public static double valuePerDivison(int value, double division, int decimalPoint) {
		if(division == 0)
			return 0;
		double powerOfTen = (double)Math.pow(10, decimalPoint);
		return Math.round(powerOfTen * value / division) / powerOfTen;
	}
	
	public static String toPercent(double num) {
		return Math.round(num * 100) + "%" ;
	}
	
	public static String checkPerfect(double num, boolean toInt) {
		if(num == 0) return "PF";
		if(toInt) return String.valueOf(round(num));
		else return String.valueOf(num);
	}
	
	public static void printChampion(String str) {System.out.printf("%-16s", str);}
	public static void printDamage(String str) {System.out.printf("%-15s", str);}
	public static void printPercent(String str) {System.out.printf("%-8s", str);}
	public static void print100thousand(String str) {System.out.printf("%-12s", str);}
	public static void printThousand(String str) {System.out.printf("%-7s", str);}
}


abstract class Color {
	public static final String black    = "\u001B[30m" ;
    public static final String red      = "\u001B[31m" ;
    public static final String green    = "\u001B[32m" ;
    public static final String yellow   = "\u001B[33m" ;
    public static final String blue     = "\u001B[34m" ;
    public static final String purple   = "\u001B[35m" ;
    public static final String cyan     = "\u001B[36m" ;
    public static final String grey     = "\u001B[37m" ;
    
    public static final String brightBlack    = "\u001B[1;30m" ;
    public static final String brightRed      = "\u001B[1;31m" ;
    public static final String brightGreen    = "\u001B[1;32m" ;
    public static final String brightYellow   = "\u001B[1;33m" ;
    public static final String brightBlue     = "\u001B[1;34m" ;
    public static final String brightPurple   = "\u001B[1;35m" ;
    public static final String brightCyan     = "\u001B[1;36m" ;
    public static final String brightGrey     = "\u001B[1;37m" ;

    public static final String exit     = "\u001B[0m" ;
}

class InvalidRequestException extends Exception {
	InvalidRequestException() {
		super();
	}
}

class UnFoundMatchException extends Exception {
	UnFoundMatchException() {
		super();
	}
}
