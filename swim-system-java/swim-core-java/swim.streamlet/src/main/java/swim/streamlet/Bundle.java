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

package swim.streamlet;

/**
 * Interface for junctions that aggregate any number of inputs into a single output.
 *
 * @param <In>  The type of the inputs.
 * @param <Out> The type of the output.
 */
public interface Bundle<In, Out> extends Junction<Out> {

  /**
   * @return The input receptacles.
   */
  Iterable<Receptacle<In>> inputs();

}
