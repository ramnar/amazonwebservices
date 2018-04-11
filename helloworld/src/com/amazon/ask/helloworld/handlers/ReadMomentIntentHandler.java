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
import com.amazon.ask.helloworld.db.MomentsDynamoDB;
import com.amazon.ask.model.Response;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;

public class ReadMomentIntentHandler implements RequestHandler {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("ReadMomentIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		MomentsDynamoDB db = MomentsDynamoDB.getInstance();
		String userId = input.getRequestEnvelope().getSession().getUser().getUserId();
		ItemCollection<QueryOutcome> items = db.getIAlltems(userId);

		String speechText = "";
		try {
			Item item = null;
			if (items != null && items.getAccumulatedItemCount() > 0) {
				Iterator<Item> iterator = items.iterator();
				while (iterator.hasNext()) {
					item = iterator.next();
					speechText += item.getString("happy_event") + ". ";

				}
			} else {
				speechText = "Sorry! No happy moments are present";
			}

		} catch (Exception e) {
			System.err.println("Unable to query happy_events table");
			System.err.println(e.getMessage());
		}
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("Happy Moments", speechText).build();
	}

}
