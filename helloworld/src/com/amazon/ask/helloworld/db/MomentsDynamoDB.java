package com.amazon.ask.helloworld.db;

import java.text.SimpleDateFormat;
import java.util.Date;
/*
 * Copyright 2012-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class MomentsDynamoDB {

	/*
	 * Before running the code: Fill in your AWS access credentials in the provided
	 * credentials file template, and be sure to move the file to the default
	 * location (/home/tomato/.aws/credentials) where the sample code will load the
	 * credentials from.
	 * https://console.aws.amazon.com/iam/home?#security_credential
	 *
	 * WARNING: To avoid accidental leakage of your credentials, DO NOT keep the
	 * credentials file in your source directory.
	 */

	static AmazonDynamoDB amazonDynamoDB;
	static final String tableName = "happy-events-table";
	private static MomentsDynamoDB instance = null;

	public static MomentsDynamoDB getInstance() {
		if (null == instance) {
			instance = new MomentsDynamoDB();
		}
		return instance;
	}

	private MomentsDynamoDB() {
		try {
			init();
		} catch (Exception e) {
			System.err.println("Failed to Initialize table");
			System.err.println(e.getMessage());
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private static void init() throws Exception {

		amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	}

	public void addItem(String userId, String happyEvent){
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("s_no", new AttributeValue(UUID.randomUUID().toString()));
		item.put("user_id", new AttributeValue(userId));
		item.put("happy_event", new AttributeValue(happyEvent));

		item.put("insert_timestamp", new AttributeValue().withN("" + System.currentTimeMillis()));

		PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
		PutItemResult putItemResult = amazonDynamoDB.putItem(putItemRequest);
		System.out.println("Result: " + putItemResult);
	}

	public ItemCollection<QueryOutcome> getItems(String userId, String inputDate) {
		ItemCollection<QueryOutcome> items = null;
		try {
			DynamoDB dynamoDB1 = new DynamoDB(amazonDynamoDB);
			Table table = dynamoDB1.getTable(tableName);

			HashMap<String, String> nameMap = new HashMap<String, String>();
			nameMap.put("#userid", "user_id");
			nameMap.put("#insert", "insert_timestamp");

			HashMap<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put(":v_userid", userId);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

			Date date = format.parse(inputDate);
			valueMap.put(":v_insert", date.getTime());

			QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#userid = :v_userid")
					.withFilterExpression("#insert > :v_insert").withNameMap(nameMap).withValueMap(valueMap);

			System.out.println("Happy events for user");
			items = table.query(querySpec);

		} catch (Exception e) {
			System.err.println("Unable to query happy_events table");
			System.err.println(e.getMessage());
		}
		return items;
	}
	
	
	public ItemCollection<QueryOutcome> getIAlltems(String userId) {
		ItemCollection<QueryOutcome> items = null;
		try {

			HashMap<String, String> nameMap = new HashMap<String, String>();
			nameMap.put("#userid", "user_id");

			HashMap<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put(":v_userid", userId);

			QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#userid = :v_userid").withNameMap(nameMap).withValueMap(valueMap);

			DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
			Table table = dynamoDB.getTable(tableName);
			items = table.query(querySpec);

		} catch (Exception e) {
			System.err.println("Unable to query happy_events table");
			System.err.println(e.getMessage());
		}
		return items;
	}

}
