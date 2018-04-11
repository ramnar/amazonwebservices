package com.ramnar.ask.core;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * Three good things skill
 * @author Ramanarayana_M
 *
 */
public class MomentsSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("amzn1.ask.skill.f3cb310b-0896-4a9c-ad68-ee48c19ffd2f");
    }

    public MomentsSpeechletRequestStreamHandler() {
        super(new MomentsSpeechlet(), supportedApplicationIds);
    }
}
