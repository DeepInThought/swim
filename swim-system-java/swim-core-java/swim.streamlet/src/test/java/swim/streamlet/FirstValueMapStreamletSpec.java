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

import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import swim.collections.HashTrieSet;
import swim.streaming.persistence.MapPersister;
import swim.streaming.persistence.TrivialPersistenceProvider.TrivialMapPersister;
import swim.streamlet.ConnectorUtilities.MapAction;
import swim.structure.Form;
import static swim.streamlet.ConnectorUtilities.remove;
import static swim.streamlet.ConnectorUtilities.update;

public class FirstValueMapStreamletSpec extends ConnectorTest {

  @DataProvider(name = "resetOnRemoval")
  public Object[][] restOnRemoval() {
    return new Object[][] {{true}, {false}};
  }

  @Test(dataProvider = "resetOnRemoval")
  public void emitsTheFirstValueForAKey(final boolean resetOnRemoval) {

    final FirstValueMapStreamlet<String, Integer> streamlet =
        new FirstValueMapStreamlet<>(resetOnRemoval, Form.forInteger());

    final ArrayList<MapAction<String, Integer>> results = ConnectorUtilities.pushData(streamlet, update("a", 3));

    Assert.assertEquals(results.size(), 1);

    expectUpdate(results.get(0), (k, v, m) -> {
      Assert.assertEquals(k, "a");
      Assert.assertEquals(v.intValue(), 3);
      Assert.assertEquals(m.size(), 1);
      Assert.assertEquals(m.get("a").get().intValue(), 3);
    });
  }

  @Test(dataProvider = "resetOnRemoval")
  public void doesNotEmitValuesAfterFirstForAKey(final boolean resetOnRemoval) {
    final FirstValueMapStreamlet<String, Integer> streamlet =
        new FirstValueMapStreamlet<>(resetOnRemoval, Form.forInteger());

    final ArrayList<MapAction<String, Integer>> results = ConnectorUtilities.pushData(streamlet, update("a", 3));

    Assert.assertEquals(results.size(), 1);

    final ArrayList<MapAction<String, Integer>> results2 = ConnectorUtilities.pushData(streamlet, update("a", 5));

    Assert.assertTrue(results2.isEmpty());
  }

  @Test(dataProvider = "resetOnRemoval")
  public void remembersValuesForPastKeys(final boolean resetOnRemoval) {
    final FirstValueMapStreamlet<String, Integer> streamlet =
        new FirstValueMapStreamlet<>(resetOnRemoval, Form.forInteger());

    final ArrayList<MapAction<String, Integer>> results = ConnectorUtilities.pushData(streamlet, update("a", 3));

    Assert.assertEquals(results.size(), 1);

    final ArrayList<MapAction<String, Integer>> results2 = ConnectorUtilities.pushData(streamlet, update("b", 7));

    Assert.assertEquals(results2.size(), 1);

    expectUpdate(results2.get(0), (k, v, m) -> {
      Assert.assertEquals(k, "b");
      Assert.assertEquals(v.intValue(), 7);
      Assert.assertEquals(m.size(), 2);
      Assert.assertEquals(m.get("a").get().intValue(), 3);
      Assert.assertEquals(m.get("b").get().intValue(), 7);
    });
  }

  @Test
  public void ignoresRemovalsInNoResetMode() {
    final FirstValueMapStreamlet<String, Integer> streamlet =
        new FirstValueMapStreamlet<>(false, Form.forInteger());

    final ArrayList<MapAction<String, Integer>> results = ConnectorUtilities.pushData(streamlet, update("a", 3));

    Assert.assertEquals(results.size(), 1);

    final ArrayList<MapAction<String, Integer>> results2 = ConnectorUtilities.pushData(streamlet, remove("a"));

    Assert.assertTrue(results2.isEmpty());

    final ArrayList<MapAction<String, Integer>> results3 = ConnectorUtilities.pushData(streamlet, update("a", 5));

    Assert.assertTrue(results3.isEmpty());

    final ArrayList<MapAction<String, Integer>> results4 = ConnectorUtilities.pushData(streamlet, update("b", 7));

    Assert.assertEquals(results4.size(), 1);

    expectUpdate(results4.get(0), (k, v, m) -> {
      Assert.assertEquals(k, "b");
      Assert.assertEquals(v.intValue(), 7);
      Assert.assertEquals(m.size(), 2);
      Assert.assertEquals(m.get("a").get().intValue(), 3);
      Assert.assertEquals(m.get("b").get().intValue(), 7);
    });
  }

  @Test
  public void removesFirstValueInResetMode() {
    final FirstValueMapStreamlet<String, Integer> streamlet =
        new FirstValueMapStreamlet<>(true, Form.forInteger());

    final ArrayList<MapAction<String, Integer>> results = ConnectorUtilities.pushData(streamlet, update("a", 3));

    Assert.assertEquals(results.size(), 1);

    final ArrayList<MapAction<String, Integer>> results2 = ConnectorUtilities.pushData(streamlet, remove("a"));

    Assert.assertEquals(results2.size(), 1);

    expectRemoval(results2.get(0), (k, m) -> {
      Assert.assertEquals(k, "a");
      Assert.assertEquals(m.size(), 0);
    });

    final ArrayList<MapAction<String, Integer>> results3 = ConnectorUtilities.pushData(streamlet, update("b", 7));

    Assert.assertEquals(results3.size(), 1);

    expectUpdate(results3.get(0), (k, v, m) -> {
      Assert.assertEquals(k, "b");
      Assert.assertEquals(v.intValue(), 7);
      Assert.assertEquals(m.size(), 1);
      Assert.assertEquals(m.get("b").get().intValue(), 7);
    });

    final ArrayList<MapAction<String, Integer>> results4 = ConnectorUtilities.pushData(streamlet, update("a", 5));

    Assert.assertEquals(results4.size(), 1);

    expectUpdate(results4.get(0), (k, v, m) -> {
      Assert.assertEquals(k, "a");
      Assert.assertEquals(v.intValue(), 5);
      Assert.assertEquals(m.size(), 2);
      Assert.assertEquals(m.get("a").get().intValue(), 5);
      Assert.assertEquals(m.get("b").get().intValue(), 7);
    });

  }

  @Test(dataProvider = "resetOnRemoval")
  public void storesFirstValuesInState(final boolean resetOnRemoval) {
    final MapPersister<String, Integer> persister = new TrivialMapPersister<>(Form.forInteger());

    final FirstValueMapStreamlet<String, Integer> streamlet = new FirstValueMapStreamlet<>(resetOnRemoval, persister);

    final ArrayList<MapAction<String, Integer>> results = ConnectorUtilities.pushData(streamlet, update("a", 3));

    Assert.assertEquals(results.size(), 1);

    Assert.assertEquals(persister.keys(), HashTrieSet.of("a"));
    Assert.assertEquals(persister.get("a").intValue(), 3);
  }

  @Test(dataProvider = "resetOnRemoval")
  public void restoresFromState(final boolean resetOnRemoval) {
    final MapPersister<String, Integer> persister = new TrivialMapPersister<>(Form.forInteger());
    persister.put("a", 3);

    final FirstValueMapStreamlet<String, Integer> streamlet = new FirstValueMapStreamlet<>(resetOnRemoval, persister);

    final ArrayList<MapAction<String, Integer>> results1 = ConnectorUtilities.pushData(streamlet, update("a", 5));

    Assert.assertTrue(results1.isEmpty());

    final ArrayList<MapAction<String, Integer>> results2 = ConnectorUtilities.pushData(streamlet, update("b", 7));

    Assert.assertEquals(results2.size(), 1);

    expectUpdate(results2.get(0), (k, v, m) -> {
      Assert.assertEquals(k, "b");
      Assert.assertEquals(v.intValue(), 7);
      Assert.assertEquals(m.size(), 2);
      Assert.assertEquals(m.get("a").get().intValue(), 3);
      Assert.assertEquals(m.get("b").get().intValue(), 7);
    });

  }

}
