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

package swim.dataflow.connector;

import java.util.ArrayList;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class TransformConduitSpec {

  @Test
  public void transformInput() {
    final TransformConduit<Integer, Integer> conduit = new TransformConduit<>(n -> 2 * n);

    final ArrayList<Integer> results = ConnectorTestUtil.pushData(conduit, 2);

    assertEquals(results.size(), 1);
    assertEquals(results.get(0).intValue(), 4);
  }

}
