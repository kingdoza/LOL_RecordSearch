package riotApi;
import java.util.*;
import java.text.*;
import java.time.*;
import java.io.*;
import java.net.*;

public class RiotApi {
	static ArrayList<Summoner> summoners = new ArrayList<Summoner>();
	
	static Summoner getSummoner(String summonerName) {
		for(Summoner summoner : summoners) {
			if(summoner.getName().equals(summonerName)) {
				return summoner;
			}
		}
		return null;
	}
	
	static String askSummonerName() throws IOException {
		BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("닉네임 입력(q:종료) >>");
		String summonerName = scanner.readLine();
		if(summonerName.equalsIgnoreCase("q")) {
			System.out.println("프로그램을 종료합니다.");
			System.exit(0);
		}
		System.out.println();
		return summonerName;
	}
	
	static String askCheckMatches(Summoner summoner) throws IOException, UnFoundMatchException {
		BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
		int matchNum = summoner.getMatchNum();
		if(matchNum == 0)
			throw new UnFoundMatchException();

		System.out.printf("%d개의 최근 게임 내역이 있습니다.\n", matchNum);
		System.out.print("확인하시겠습니까?(y:예,non-y:아니오) >>");
		System.out.println();
		return scanner.readLine();
	}
	
	static void askMatchId(Summoner summoner) {
		BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			String matchIndex;
			System.out.print("자세히보고 싶은 게임(etc제외)의 번호를 입력하세요(q:닉네임 입력) >>");
			try {
				matchIndex = scanner.readLine();
				if(matchIndex.equalsIgnoreCase("q")) return;
				Match match = summoner.getMatch(matchIndex);
				if(match != null) {
					match.printDetails();
					System.out.println();
				}
				else {
					System.out.println("해당 번호가 없습니다.\n");
				}
			} catch(IOException e) {
				System.out.println("적절한 입력이 아닙니다.");
			}
		}
	}
	
	public static void main(String[] args) {
		BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			try {
				String answer = askSummonerName();
				Summoner summoner = getSummoner(answer);
				if(summoner == null) {
					summoner = RApi.initSummoner(answer);
					summoners.add(summoner);
				}
				summoner.printInfo();
				answer = askCheckMatches(summoner);
				if(answer.equalsIgnoreCase("y")) {
					summoner.printMatches();
					askMatchId(summoner);
				}
				
			} catch (IOException e) {
				System.out.println("적절한 입력이 아닙니다.");
			} catch(InvalidRequestException e) {
				System.out.println("해당 정보가 없습니다.");
			} catch(UnFoundMatchException e) {
				System.out.println("최근 게임 내역이 없습니다.");
			} finally {
				System.out.println();
			}
		}
	}
}
