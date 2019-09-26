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
import swim.streaming.Junction;
import swim.streaming.SwimStream;
import swim.streaming.SwimStreamContext;
import swim.streaming.persistence.ValuePersister;
import swim.streamlet.FirstValueStreamlet;

/**
 * A stream that will only ever contain the first value received from its source.
 *
 * @param <T> The type of the values.
 */
final class FirstStream<T> extends AbstractSwimStream<T> {

  private final SwimStream<T> in;
  private final boolean isTransient;

  /**
   * @param in          The input stream.
   * @param con         The instantiation context.
   * @param isTransient Whether to persist the first value.
   */
  FirstStream(final SwimStream<T> in, final BindingContext con,
              final boolean isTransient) {
    super(in.form(), con);
    this.in = in;
    this.isTransient = isTransient;
  }

  /**
   * @param in          The input stream.
   * @param con         The instantiation context.
   * @param isTransient Whether to persist the first value.
   * @param ts          Assigns timestamps to the values.
   */
  FirstStream(final SwimStream<T> in, final BindingContext con,
              final boolean isTransient,
              final ToLongFunction<T> ts) {
    super(in.form(), con, ts);
    this.in = in;
    this.isTransient = isTransient;
  }

  @Override
  public SwimStream<T> updateTimestamps(final ToLongFunction<T> datation) {
    return new FirstStream<>(in, getContext(), isTransient, datation);
  }

  @Override
  public Junction<T> instantiate(final SwimStreamContext.InitContext context) {
    final Junction<T> source = context.createFor(in);

    final FirstValueStreamlet<T> streamlet;
    if (isTransient) {
      streamlet = new FirstValueStreamlet<>();
    } else {
      final ValuePersister<T> persister = context.getPersistenceProvider().forValue(
          StateTags.stateTag(id()), form().unit(null));
      streamlet = new FirstValueStreamlet<>(persister);
    }
    source.subscribe(streamlet);
    return streamlet;
  }
}
