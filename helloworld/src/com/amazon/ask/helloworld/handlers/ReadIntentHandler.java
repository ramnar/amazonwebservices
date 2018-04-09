/*
     Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

     Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
     except in compliance with the License. A copy of the License is located at

         http://aws.amazon.com/apache2.0/

     or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
     the specific language governing permissions and limitations under the License.
*/

package com.amazon.ask.helloworld.handlers;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.Iterator;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.helloworld.db.HelloworldDynamoDB;
import com.amazon.ask.model.Response;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;

public class ReadIntentHandler implements RequestHandler {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ReadIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		HelloworldDynamoDB db = HelloworldDynamoDB.getInstance();
		ItemCollection<QueryOutcome> items = db.getItems("ram", "2018-04-07");

		Iterator<Item> iterator = null;
		Item item = null;
		String speechText = "";
		try {

			iterator = items.iterator();
			while (iterator.hasNext()) {
				item = iterator.next();
				System.out.println(item.getString("user_id") + ": " + item.getString("happy_event"));
				speechText += item.getString("happy_event") + " .";

			}
		} catch (Exception e) {
			System.err.println("Unable to query happy_events table");
			System.err.println(e.getMessage());
		}
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("HelloWorld", speechText).build();
	}

}
