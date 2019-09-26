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

import java.util.function.BinaryOperator;
import java.util.function.ToLongFunction;
import swim.streaming.Junction;
import swim.streaming.SwimStream;
import swim.streaming.SwimStreamContext;
import swim.streaming.persistence.ValuePersister;
import swim.streaming.sampling.Sampling;
import swim.streamlet.StatefulStreamlets;
import swim.streamlet.StreamInterpretation;
import swim.streamlet.Streamlet;

/**
 * Reduces the values of the stream over time.
 *
 * @param <T> The type of the values.
 */
class ReducedStream<T> extends AbstractSwimStream<T> {
  private final SwimStream<T> in;
  private final BinaryOperator<T> operator;
  private final Sampling sampling;
  private final boolean isTransient;

  /**
   * @param inputs      The input stream.
   * @param context     Instantiation context.
   * @param op          Operation to apply on each new value and the previous state of the reduction.
   * @param sampleStrat Sampling strategy for the link.
   * @param isTransient Whether the state of this stream is stored persistently.
   */
  ReducedStream(final SwimStream<T> inputs,
                final BindingContext context,
                final BinaryOperator<T> op,
                final Sampling sampleStrat,
                final boolean isTransient) {
    super(inputs.form(), context);
    in = inputs;
    operator = op;
    sampling = sampleStrat;
    this.isTransient = isTransient;
  }

  ReducedStream(final SwimStream<T> inputs,
                final BindingContext context,
                final BinaryOperator<T> op,
                final Sampling sampleStrat,
                final boolean isTransient,
                final ToLongFunction<T> ts) {
    super(inputs.form(), context, ts);
    in = inputs;
    operator = op;
    sampling = sampleStrat;
    this.isTransient = isTransient;
  }

  @Override
  public SwimStream<T> updateTimestamps(final ToLongFunction<T> datation) {
    return new ReducedStream<>(in, getContext(), operator, sampling, isTransient, datation);
  }

  @Override
  public Junction<T> instantiate(final SwimStreamContext.InitContext context) {
    final Junction<T> source = StreamDecoupling.sampleStream(id(), context,
        context.createFor(in), sampling, StreamInterpretation.DISCRETE);
    final Streamlet<T, T> streamlet;
    if (isTransient) {
      streamlet = StatefulStreamlets.reduce(operator);
    } else {
      final ValuePersister<T> statePersister = context.getPersistenceProvider().forValue(
          StateTags.stateTag(id()), form(), null);
      streamlet = StatefulStreamlets.reduce(statePersister, operator);
    }
    source.subscribe(streamlet);
    return streamlet;
  }

}
