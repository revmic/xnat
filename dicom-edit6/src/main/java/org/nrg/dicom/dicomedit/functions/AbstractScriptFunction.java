/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.Function
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for script functions not needing external context.
 */
public abstract class AbstractScriptFunction extends BaseFunction {

    public AbstractScriptFunction(String name, String nameSpace, String usage, String description) {
        super(name, nameSpace, usage, description);
    }

    public AbstractScriptFunction(Properties properties) {
        super(properties);
    }

    abstract Value apply(List<Value> args, DicomObjectI dicomObject) throws ScriptEvaluationException;
    public Value apply(List<Value> args, Map<String, Variable> variables, DicomObjectI dicomObject) throws ScriptEvaluationException {
        return apply(args,dicomObject);
    }

    /**
     * Result for a function whose input attribute is missing or empty. Assigning the null value it holds leaves the
     * target attribute as it was, so the rest of the script still runs, as delete and ?= already do for a missing tag.
     *
     * @param argument The argument whose value was missing, logged so a mistyped tag can be found.
     *
     * @return A constant holding null.
     */
    protected Value skipMissing(final Value argument) {
        logger.warn("{}: skipping missing or empty value for {}", getName(), argument);
        return new ConstantValue(null);
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractScriptFunction.class);

}
