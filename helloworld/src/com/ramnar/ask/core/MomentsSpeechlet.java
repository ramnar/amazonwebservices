package com.ramnar.ask.core;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.ramnar.ask.db.MomentsDynamoDB;

/**
 * speechlet for three good things skill
 * @author Ramanarayana_M
 *
 */
public class MomentsSpeechlet implements SpeechletV2 {

	private static final String ERROR_MESSAGE = "Sorry this is not supported. Please try something else";
	private static final String GOODTHINGS_SLOT = "goodthings";
	private static final String DURATION_SLOT = "duration";
	private static final String WELCOME_MESSAGE = "Welcome to three good things! Tell one thing that went well for you today by saying something like Today I played awesome cricket";
	private static final String WELCOME_REPROMPT_MESSAGE = "Tell one thing that went well for you today by saying something like Today I played awesome cricket";
	private static final String SKILL_TITLE = "Three Good Things";
	private static final String HELP_MESSAGE = "Three good things is a skill to add one or more good experiences you had today and to listen to your past experiences";
	private static final String STOP_MESSAGE = "Good Bye!";
	private static final String FIRST_MESSAGE = "First one added successfully. Tell the second one. If you dont have any thing, say stop";
	private static final String SECOND_MESSAGE = "Second one added successfully. Tell the third one. If you dont have any thing, say stop";
	private static final String THIRD_MESSAGE = "Third one added successfully. Thankyou. Have a nice day";
	private static final String EXCEPTION_MESSAGE = "Sorry something went wrong. Please try after some time";

	private static final Logger log = LoggerFactory.getLogger(MomentsSpeechlet.class);

	private static final String COUNTER = "COUNTER";

	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(), requestEnvelope.getSession().getSessionId());
		return getWelcomeResponse();
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		IntentRequest request = requestEnvelope.getRequest();
		Session session = requestEnvelope.getSession();

		Intent intent = request.getIntent();
		log.info("Values for requestId={}, sessionId={}, intentName={}", request.getRequestId(), session, intent.getName());


		String intentName = (intent != null) ? intent.getName() : null;
		switch (intentName) {
		case "AddMomentIntent":
			return processAddMomentIntent(intent, session);

		case "ReadMomentIntent":
			return processReadMomentsIntent(intent, session);

		case "AMAZON.HelpIntent":
			return getSpeechletResponse(HELP_MESSAGE, HELP_MESSAGE, true);
		case "AMAZON.CancelIntent":
		case "AMAZON.StopIntent":

			return getSpeechletResponse(STOP_MESSAGE, STOP_MESSAGE, false);
		default:
			String errorSpeech = ERROR_MESSAGE;
			return getSpeechletResponse(errorSpeech, errorSpeech, true);
		}
	}

	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
	}

	private SpeechletResponse processAddMomentIntent(final Intent intent, final Session session) {
		log.info("Entering processAddMomentIntent intent={}, session={}", intent.getName(), session);
		
		MomentsDynamoDB db = MomentsDynamoDB.getInstance();
		String userId = session.getUser().getUserId();
		Map<String, Object> attributes = session.getAttributes();

		Map<String, Slot> slots = intent.getSlots();
		String message = slots.get(GOODTHINGS_SLOT).getValue();
		Object counter = attributes.get(COUNTER);
		
		log.info("Property values are message={}, counter={}", message, counter);
		
		boolean isAdded = db.addItem(userId, message);
		if (!isAdded) {
			return getSpeechletResponse(EXCEPTION_MESSAGE, EXCEPTION_MESSAGE, true);
		}
		String speechText = "";

		boolean rePrompt = false;

		if (counter == null) {
			attributes.put(COUNTER, new Integer(1));
			speechText = FIRST_MESSAGE;
			rePrompt = true;
		} else if (((Integer) counter) == 1) {
			attributes.put(COUNTER, (Integer) counter + 1);
			speechText = SECOND_MESSAGE;
			rePrompt = true;
		} else if (((Integer) counter) == 2) {
			attributes.put(COUNTER, (Integer) counter + 1);
			speechText = THIRD_MESSAGE;
		}

		log.info("Exiting processAddMomentIntent");
		return getSpeechletResponse(speechText, speechText, rePrompt);
	}

	private SpeechletResponse processReadMomentsIntent(final Intent intent, final Session session) {
		log.info("Entering processReadMomentsIntent intent={}, session={}", intent, session);
		Map<String, Slot> slots = intent.getSlots();
		String message = slots.get(DURATION_SLOT).getValue();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long timestamp = 0;

		switch (message) {
		case "all":
			timestamp = -1;
			break;
		case "today":
			timestamp = cal.getTimeInMillis();
			break;
		case "yesterday":
			cal.add(Calendar.DATE, -1);
			timestamp = cal.getTimeInMillis();
			break;
		case "one week":
		case "1 week":
			cal.add(Calendar.DATE, -6);
			timestamp = cal.getTimeInMillis();
			break;
		case "one month":
		case "1 month":
			cal.add(Calendar.DATE, -29);
			timestamp = cal.getTimeInMillis();
			break;
		default:
			break;
		}

		String speechText = "";
		try {
			MomentsDynamoDB momentsDynamoDB = MomentsDynamoDB.getInstance();
			String userId = session.getUser().getUserId();
			log.debug("userId={}, slot message={}, timestamp={}", userId, message, timestamp);
			Item item = null;
			ItemCollection<QueryOutcome> items = momentsDynamoDB.getItems(userId, timestamp);
			if (items != null) {
				Iterator<Item> iterator = items.iterator();
				while (iterator.hasNext()) {
					item = iterator.next();
					speechText += item.getString("happy_event") + ". ";
				}
			} else {
				speechText = "Sorry! No good things are present";
			}

		} catch (Exception e) {
			log.error("Exception in getting items {}", e.getMessage());
			speechText = EXCEPTION_MESSAGE;
		}
		log.info("speechText={}", speechText);
		log.info("Exiting processReadMomentsIntent");
		return getSpeechletResponse(speechText, speechText, false);
	}

	/**
	 * Creates and returns a {@code SpeechletResponse} with a welcome message.
	 *
	 * @return SpeechletResponse spoken and visual welcome message
	 */
	private SpeechletResponse getWelcomeResponse() {
		return getSpeechletResponse(WELCOME_MESSAGE, WELCOME_REPROMPT_MESSAGE, true);
	}

	/**
	 * Returns a Speechlet response for a speech and reprompt text.
	 */
	private SpeechletResponse getSpeechletResponse(String speechText, String repromptText, boolean isAskResponse) {
		// Create the Simple card content.
		SimpleCard card = new SimpleCard();
		card.setTitle(SKILL_TITLE);
		card.setContent(speechText);

		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		if (isAskResponse) {
			// Create reprompt
			PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
			repromptSpeech.setText(repromptText);
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(repromptSpeech);

			return SpeechletResponse.newAskResponse(speech, reprompt, card);

		} else {
			return SpeechletResponse.newTellResponse(speech, card);
		}
	}
}
