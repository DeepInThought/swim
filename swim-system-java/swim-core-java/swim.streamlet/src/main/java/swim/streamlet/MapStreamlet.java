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

import swim.streaming.MapJunction;
import swim.streaming.MapReceptacle;

/**
 * A streamlet that consumes a map and outputs another map.
 *
 * @param <KIn>  The type of the input keys.
 * @param <KOut> The type of the output keys.
 * @param <VIn>  The type of the input values.
 * @param <VOut> The type of the output values.
 */
public interface MapStreamlet<KIn, KOut, VIn, VOut> extends MapReceptacle<KIn, VIn>, MapJunction<KOut, VOut> {
}
