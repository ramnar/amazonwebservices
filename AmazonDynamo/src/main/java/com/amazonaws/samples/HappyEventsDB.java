package com.amazonaws.samples;

import java.text.ParseException;
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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

/**
 * This sample demonstrates how to perform a few simple operations with the
 * Amazon DynamoDB service.
 */
public class HappyEventsDB {

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

	static AmazonDynamoDB dynamoDB;
	static final String tableName = "happy-events-table";

	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed automatically.
	 * Client parameters, such as proxies, can be specified in an optional
	 * ClientConfiguration object when constructing a client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.ProfilesConfigFile
	 * @see com.amazonaws.ClientConfiguration
	 */
	private static void init() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default] credential profile
		 * by reading from the credentials file located at
		 * (/home/tomato/.aws/credentials).
		 */
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (/home/tomato/.aws/credentials), and is in valid format.", e);
		}
		dynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider)
				// .withRegion("us-west-2")
				// .build();
				.withEndpointConfiguration(
						new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
				.build();
	}

	public static void main(String[] args) throws Exception {
		init();

		try {

			// Create a table with a primary hash key named 'name', which holds a string
			CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
					.withKeySchema(new KeySchemaElement().withAttributeName("user_id").withKeyType(KeyType.HASH),
							new KeySchemaElement().withAttributeName("s_no").withKeyType(KeyType.RANGE))
					.withAttributeDefinitions(
							new AttributeDefinition().withAttributeName("user_id")
									.withAttributeType(ScalarAttributeType.S),
							new AttributeDefinition().withAttributeName("s_no")
									.withAttributeType(ScalarAttributeType.S))
					.withProvisionedThroughput(
							new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

			// Create table if it does not exist yet
			TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
			// wait for the table to move into ACTIVE state
			TableUtils.waitUntilActive(dynamoDB, tableName);

			getItems("ram", "2018-04-08");

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public static void addItem(String userId, String happyEvent) throws ParseException {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("s_no", new AttributeValue(UUID.randomUUID().toString()));
		item.put("user_id", new AttributeValue(userId));
		item.put("happy_event", new AttributeValue(happyEvent));

		item.put("insert_timestamp", new AttributeValue().withN("" + System.currentTimeMillis()));

		PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
		PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
		System.out.println("Result: " + putItemResult);
	}

	public static void getItems(String userId, String inputDate) throws ParseException {

		DynamoDB dynamoDB1 = new DynamoDB(dynamoDB);
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

		ItemCollection<QueryOutcome> items = null;
		Iterator<Item> iterator = null;
		Item item = null;

		try {
			System.out.println("Happy events for user");
			items = table.query(querySpec);

			iterator = items.iterator();
			while (iterator.hasNext()) {
				item = iterator.next();
				System.out.println(item.getString("user_id") + ": " + item.getString("happy_event"));
			}

		} catch (Exception e) {
			System.err.println("Unable to query happy_events table");
			System.err.println(e.getMessage());
		}
	}

	
}
