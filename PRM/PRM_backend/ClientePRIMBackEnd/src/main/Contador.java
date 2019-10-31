package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Contador {
	public static int getNext(){
		int next = 0;
		try(BufferedReader br = new BufferedReader(new FileReader("cuenta.txt"))){
			next = Integer.parseInt(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try(PrintWriter pw = new PrintWriter("cuenta.txt")){
			pw.println(next+1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return next;
	}
}
