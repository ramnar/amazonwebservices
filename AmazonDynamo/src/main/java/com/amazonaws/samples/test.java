package com.amazonaws.samples;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class test {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Date date = format.parse("2018-05-09");
		System.out.println(date.getTime());

	}

}
