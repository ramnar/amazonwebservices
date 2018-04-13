package com.ramnar.ask.core;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * Three good things skill
 * @author Ramanarayana_M
 *
 */
public class MomentsSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    static {
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("amzn1.ask.skill.f3cb310b-0896-4a9c-ad68-ee48c19ffd2f");
        
        LoggerContext ctx = (LoggerContext) LogManager.getContext();
        LoggerConfig log = ctx.getConfiguration().getRootLogger();
        log.setLevel(Level.DEBUG);
        ctx.updateLoggers();
    }

    public MomentsSpeechletRequestStreamHandler() {
        super(new MomentsSpeechlet(), supportedApplicationIds);
    }
}
