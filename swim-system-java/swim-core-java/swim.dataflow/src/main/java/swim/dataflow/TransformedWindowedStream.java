// Copyright 2015-2019 SWIM.AI inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package swim.dataflow;

import java.util.function.ToLongFunction;
import swim.collections.FingerTrieSeq;
import swim.dataflow.windows.DefaultPaneManager;
import swim.dataflow.windows.NoOpEvictor;
import swim.dataflow.windows.PaneEvictor;
import swim.dataflow.windows.SequencePaneUpdater;
import swim.dataflow.windows.WindowAccumulators;
import swim.dataflow.windows.WindowFunctionEvaluator;
import swim.dataflow.windows.WindowStreamlet;
import swim.streaming.Junction;
import swim.streaming.SwimStream;
import swim.streaming.SwimStreamContext;
import swim.streaming.sampling.Sampling;
import swim.streaming.timestamps.TimestampAssigner;
import swim.streaming.timestamps.WithTimestamp;
import swim.streaming.windows.WindowFunction;
import swim.streaming.windows.WindowSpec;
import swim.streaming.windows.WindowState;
import swim.streamlet.StreamInterpretation;
import swim.structure.Form;

/**
 * Stream formed by applying a function across the windows of a windowed stream.
 *
 * @param <T> The type of the values in the windows.
 * @param <W> The type of the windows.
 * @param <U> The type of the outputs.
 */
class TransformedWindowedStream<T, W, S extends WindowState<W, S>, U> extends AbstractSwimStream<U> {

  private final SwimStream<T> in;
  private final WindowSpec<T, W, S> spec;
  private final WindowFunction<T, W, U> winFun;
  private final Sampling sampling;

  /**
   * @param input         The source stream.
   * @param windowSpec    The specification of the windows.
   * @param context       The instantiation context.
   * @param fun           The window function.
   * @param form          The form of the type of the outputs.
   * @param samplingStrat The sampling strategy for the link.
   */
  TransformedWindowedStream(final SwimStream<T> input,
                            final WindowSpec<T, W, S> windowSpec,
                            final BindingContext context,
                            final WindowFunction<T, W, U> fun,
                            final Form<U> form,
                            final Sampling samplingStrat) {
    super(form, context);
    in = input;
    spec = windowSpec;
    winFun = fun;
    sampling = samplingStrat;
  }

  /**
   * @param input         The source stream.
   * @param windowSpec    The specification of the windows.
   * @param context       The instantiation context.
   * @param fun           The window function.
   * @param form          The form of the type of the outputs.
   * @param samplingStrat The sampling strategy for the link.
   * @param ts            Timestamp assigner for the stream.
   */
  TransformedWindowedStream(final SwimStream<T> input,
                            final WindowSpec<T, W, S> windowSpec,
                            final BindingContext context,
                            final WindowFunction<T, W, U> fun,
                            final Form<U> form,
                            final Sampling samplingStrat,
                            final ToLongFunction<U> ts) {
    super(form, context, ts);
    in = input;
    spec = windowSpec;
    winFun = fun;
    sampling = samplingStrat;
  }


  @Override
  public SwimStream<U> updateTimestamps(final ToLongFunction<U> datation) {
    return new TransformedWindowedStream<>(in, spec, getContext(), winFun, form(), sampling, datation);
  }

  @Override
  public Junction<U> instantiate(final SwimStreamContext.InitContext context) {
    final SequencePaneUpdater<T, W> updater = new SequencePaneUpdater<>();
    final PaneEvictor<T, W, FingerTrieSeq<WithTimestamp<T>>> evictor = spec.getEvictionStrategy().match(
        none -> NoOpEvictor.instance(),
        EvictionUtil::createEvictor
    );
    final WindowFunctionEvaluator<T, W, U> evaluator = new WindowFunctionEvaluator<>(winFun);

    final WindowAccumulators<W, FingerTrieSeq<WithTimestamp<T>>> accumulators = WindowStates.forSequencesState(
        spec.isTransient(), StateTags.stateTag(id()), context.getPersistenceProvider(), spec.getWindowForm(), in.form());
    final DefaultPaneManager<T, W, S, FingerTrieSeq<WithTimestamp<T>>, U> paneManager =
        new DefaultPaneManager<>(accumulators,
            spec.getAssigner(), spec.getTrigger(), updater, evictor, evaluator);

    final ToLongFunction<T> ts = in.getTimestamps();
    final TimestampAssigner<T> timestamps =
        ts == null ? TimestampAssigner.fromClock() : TimestampAssigner.fromData(ts);


    final WindowStreamlet<T, W, U> streamlet = new WindowStreamlet<>(
        context.getSchedule(), paneManager, timestamps);

    final Junction<T> source = StreamDecoupling.sampleStream(id(), context, context.createFor(in), sampling, StreamInterpretation.DISCRETE);

    source.subscribe(streamlet);
    return streamlet;
  }

}
