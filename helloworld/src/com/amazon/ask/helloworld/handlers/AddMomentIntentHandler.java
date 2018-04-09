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

import java.util.Map;
import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.helloworld.db.HelloworldDynamoDB;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;

public class AddMomentIntentHandler implements RequestHandler {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AddMomentIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		HelloworldDynamoDB db = HelloworldDynamoDB.getInstance();
		String userId = input.getRequestEnvelope().getSession().getUser().getUserId();

		Map<String, Slot> slots = ((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent().getSlots();
		String message = slots.get("happymoment").getValue();
		db.addItem(userId, message);
		String speechText = "Happy Event added successfully";
		return input.getResponseBuilder().withSpeech(speechText).withSimpleCard("HappyEvent", speechText).build();
	}

}
