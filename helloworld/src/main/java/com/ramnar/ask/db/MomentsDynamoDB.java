package com.ramnar.ask.db;

/**
 * ramnar
 */
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

/**
 * Class to do crud operations on a table in amazon dynamo db.
 */
public class MomentsDynamoDB {

	static final Logger log = LogManager.getLogger(MomentsDynamoDB.class);

	private static AmazonDynamoDB amazonDynamoDB;
	private static final String tableName = "happy-events-table";
	private static MomentsDynamoDB instance = null;

	public static MomentsDynamoDB getInstance() {
		log.info("Entering getInstance");
		if (null == instance) {
			amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
			instance = new MomentsDynamoDB();
		}
		log.info("Exiting getInstance");
		return instance;
	}

	private MomentsDynamoDB() {

	}

	public boolean addItem(String userId, String happyEvent) {
		log.info("Entering addItem");

		if (userId == null || userId.trim().length() == 0 || happyEvent == null || happyEvent.trim().length() == 0) {
			log.info("user id or happy event are not valid. userId={} happyEvent={}", userId, happyEvent);
			return false;
		}

		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("s_no", new AttributeValue(UUID.randomUUID().toString()));
		item.put("user_id", new AttributeValue(userId));
		item.put("happy_event", new AttributeValue(happyEvent));
		item.put("insert_timestamp", new AttributeValue().withN("" + System.currentTimeMillis()));

		log.info("Values being insert into the table are s_no={}, userId={}, happy_event={},insert_timestamp={}", item.get("s_no"), item.get("user_id"), item.get("happy_event"), item.get("s_no"), item.get("insert_timestamp"));

		try {
			amazonDynamoDB.putItem(new PutItemRequest(tableName, item));
			log.info("Record Inserted successfully");
		} catch (Exception e) {
			log.error("Exception in adding record {}", e.getMessage());
			return false;
		} finally {
			log.info("Exiting addItem");
		}
		return true;
	}

	public ItemCollection<QueryOutcome> getItems(String userId, long timestamp) {
		log.info("Entering getItems");
		ItemCollection<QueryOutcome> items = null;
		try {
			DynamoDB dynamoDB1 = new DynamoDB(amazonDynamoDB);
			Table table = dynamoDB1.getTable(tableName);

			HashMap<String, String> nameMap = new HashMap<String, String>();
			nameMap.put("#userid", "user_id");

			HashMap<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put(":v_userid", userId);

			QuerySpec querySpec = null;
			if (timestamp != -1) {
				nameMap.put("#insert", "insert_timestamp");
				valueMap.put(":v_insert", timestamp);
				querySpec = new QuerySpec().withKeyConditionExpression("#userid = :v_userid").withFilterExpression("#insert > :v_insert").withNameMap(nameMap).withValueMap(valueMap);
			} else {
				querySpec = new QuerySpec().withKeyConditionExpression("#userid = :v_userid").withNameMap(nameMap).withValueMap(valueMap);
			}

			log.info("Filter parameters for querying the table are userId={}, timestamp={}", userId, timestamp);
			
			items = table.query(querySpec);
			
			log.info("Items are fetched succesfully from the table items={}", items);
		} catch (Exception e) {
			log.error("Exception in getting items {}", e.getMessage());
			throw e;
		} finally {
			log.info("Exiting getItems");
		}
		return items;
	}

}
