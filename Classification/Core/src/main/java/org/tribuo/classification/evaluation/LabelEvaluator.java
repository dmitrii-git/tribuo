/*
 * Copyright (c) 2015-2020, Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tribuo.classification.evaluation;

import org.tribuo.Model;
import org.tribuo.Prediction;
import org.tribuo.classification.Label;
import org.tribuo.evaluation.AbstractEvaluator;
import org.tribuo.evaluation.Evaluator;
import org.tribuo.evaluation.metrics.MetricID;
import org.tribuo.evaluation.metrics.MetricTarget;
import org.tribuo.provenance.EvaluationProvenance;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * An {@link Evaluator} for {@link Label}s.
 * <p>
 * The default set of metrics is taken from {@link LabelMetrics}. If the supplied
 * model generates probabilities, then it also calculates {@link LabelMetrics#AUCROC} and
 * {@link LabelMetrics#AVERAGED_PRECISION}.
 */
public final class LabelEvaluator extends AbstractEvaluator<Label, LabelMetric.Context, LabelEvaluation, LabelMetric> {

    @Override
    protected Set<LabelMetric> createMetrics(Model<Label> model) {
        Set<LabelMetric> metrics = new HashSet<>();
        //
        // Populate labelwise values
        for (Label label : model.getOutputIDInfo().getDomain()) {
            MetricTarget<Label> tgt = new MetricTarget<>(label);
            metrics.add(LabelMetrics.TP.forTarget(tgt));
            metrics.add(LabelMetrics.FP.forTarget(tgt));
            metrics.add(LabelMetrics.TN.forTarget(tgt));
            metrics.add(LabelMetrics.FN.forTarget(tgt));
            metrics.add(LabelMetrics.PRECISION.forTarget(tgt));
            metrics.add(LabelMetrics.RECALL.forTarget(tgt));
            metrics.add(LabelMetrics.F1.forTarget(tgt));
            metrics.add(LabelMetrics.ACCURACY.forTarget(tgt));
            if (model.generatesProbabilities()) {
                metrics.add(LabelMetrics.AUCROC.forTarget(tgt));
                metrics.add(LabelMetrics.AVERAGED_PRECISION.forTarget(tgt));
            }
        }

        //
        // Populate averaged values.
        MetricTarget<Label> micro = MetricTarget.microAverageTarget();
        metrics.add(LabelMetrics.TP.forTarget(micro));
        metrics.add(LabelMetrics.FP.forTarget(micro));
        metrics.add(LabelMetrics.TN.forTarget(micro));
        metrics.add(LabelMetrics.FN.forTarget(micro));
        metrics.add(LabelMetrics.PRECISION.forTarget(micro));
        metrics.add(LabelMetrics.RECALL.forTarget(micro));
        metrics.add(LabelMetrics.F1.forTarget(micro));
        metrics.add(LabelMetrics.ACCURACY.forTarget(micro));

        MetricTarget<Label> macro = MetricTarget.macroAverageTarget();
        metrics.add(LabelMetrics.TP.forTarget(macro));
        metrics.add(LabelMetrics.FP.forTarget(macro));
        metrics.add(LabelMetrics.TN.forTarget(macro));
        metrics.add(LabelMetrics.FN.forTarget(macro));
        metrics.add(LabelMetrics.PRECISION.forTarget(macro));
        metrics.add(LabelMetrics.RECALL.forTarget(macro));
        metrics.add(LabelMetrics.F1.forTarget(macro));
        metrics.add(LabelMetrics.ACCURACY.forTarget(macro));

        // Target doesn't matter for balanced error rate, so we just use
        // average.macro as it's the macro average of recalls.
        metrics.add(LabelMetrics.BALANCED_ERROR_RATE.forTarget(macro));

        return metrics;
    }

    @Override
    protected LabelMetric.Context createContext(Model<Label> model, List<Prediction<Label>> predictions) {
        return new LabelMetric.Context(model, predictions);
    }

    @Override
    protected LabelEvaluation createEvaluation(LabelMetric.Context ctx,
                                               Map<MetricID<Label>, Double> results,
                                               EvaluationProvenance provenance) {
        return new LabelEvaluationImpl(results, ctx, provenance);
    }
}