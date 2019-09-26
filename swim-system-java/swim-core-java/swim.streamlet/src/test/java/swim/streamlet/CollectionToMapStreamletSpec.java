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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import swim.collections.HashTrieSet;
import swim.streaming.persistence.MapPersister;
import swim.streaming.persistence.TrivialPersistenceProvider;
import swim.streamlet.ConnectorUtilities.MapAction;
import swim.structure.Form;
import swim.util.Pair;

public class CollectionToMapStreamletSpec extends ConnectorTest {

  @DataProvider(name = "withState")
  public Object[][] withOrWithoutState() {
    return new Object[][] {{true}, {false}};
  }

  @Test(dataProvider = "withState")
  public void initializeFromNothing(final boolean withState) {

    if (withState) {
      final TrivialPersistenceProvider provider = new TrivialPersistenceProvider();
      final MapPersister<Integer, String> persister = provider.forMap(
          "state", Form.forInteger(), Form.forString());
      final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(n -> n, Object::toString, persister);

      initializeFromNothing(streamlet);

      Assert.assertEquals(persister.keys(), HashTrieSet.of(1, 2, 3));
      Assert.assertEquals(persister.get(1), "1");
      Assert.assertEquals(persister.get(2), "2");
      Assert.assertEquals(persister.get(3), "3");
    } else {
      final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(n -> n, Object::toString, Form.forString());

      initializeFromNothing(streamlet);
    }
  }

  private void initializeFromNothing(final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet) {
    final ArrayList<MapAction<Integer, String>> results = ConnectorUtilities.pushData(
        streamlet, HashTrieSet.of(1, 2, 3));

    Assert.assertEquals(results.size(), 3);

    final HashSet<Integer> keys = new HashSet<>();
    for (final MapAction<Integer, String> action : results) {
      expectUpdate(action, (k, v, m) -> {
        keys.add(k);
        Assert.assertEquals(v, k.toString());
        Assert.assertEquals(m.size(), keys.size());
        for (final Integer key : keys) {
          Assert.assertTrue(m.containsKey(key));
          Assert.assertEquals(m.get(key).get(), key.toString());
        }
      });
    }
  }

  @Test(dataProvider = "withState")
  public void addAdditionalEntry(final boolean withState) {
    if (withState) {
      final TrivialPersistenceProvider provider = new TrivialPersistenceProvider();
      final MapPersister<Integer, String> persister = provider.forMap(
          "state", Form.forInteger(), Form.forString());
      final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(n -> n, Object::toString, persister);

      addAdditionalEntry(streamlet);

      Assert.assertEquals(persister.keys(), HashTrieSet.of(1, 2, 3, 4));
      Assert.assertEquals(persister.get(1), "1");
      Assert.assertEquals(persister.get(2), "2");
      Assert.assertEquals(persister.get(3), "3");
      Assert.assertEquals(persister.get(4), "4");
    } else {
      final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(n -> n, Object::toString, Form.forString());

      addAdditionalEntry(streamlet);
    }

  }

  private void addAdditionalEntry(final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet) {
    ConnectorUtilities.pushData(
        streamlet, HashTrieSet.of(1, 2, 3));

    final ArrayList<MapAction<Integer, String>> results = ConnectorUtilities.pushData(
        streamlet, HashTrieSet.of(1, 2, 3, 4));

    Assert.assertEquals(results.size(), 1);

    expectUpdate(results.get(0), (k, v, m) -> {
      Assert.assertEquals(k.intValue(), 4);
      Assert.assertEquals(v, k.toString());
      Assert.assertEquals(m.size(), 4);
      for (int key = 1; key <= 4; ++key) {
        Assert.assertTrue(m.containsKey(key));
        Assert.assertEquals(m.get(key).get(), Integer.toString(key));
      }
    });
  }

  @Test(dataProvider = "withState")
  public void alterExistingEntry(final boolean withState) {
    if (withState) {
      final TrivialPersistenceProvider provider = new TrivialPersistenceProvider();

      final MapPersister<Integer, String> persister = provider.forMap(
          "state", Form.forInteger(), Form.forString());

      final CollectionToMapStreamlet<Pair<Integer, String>, List<Pair<Integer, String>>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(Pair::getFirst, Pair::getSecond, persister);

      alterExistingEntry(streamlet);

      Assert.assertEquals(persister.keys(), HashTrieSet.of(1, 2, 3));
      Assert.assertEquals(persister.get(1), "a");
      Assert.assertEquals(persister.get(2), "NEW!");
      Assert.assertEquals(persister.get(3), "c");
    } else {
      final CollectionToMapStreamlet<Pair<Integer, String>, List<Pair<Integer, String>>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(Pair::getFirst, Pair::getSecond, Form.forString());

      alterExistingEntry(streamlet);
    }


  }

  private void alterExistingEntry(final CollectionToMapStreamlet<Pair<Integer, String>, List<Pair<Integer, String>>, Integer, String> streamlet) {
    ConnectorUtilities.pushData(
        streamlet, Arrays.asList(Pair.pair(1, "a"), Pair.pair(2, "b"), Pair.pair(3, "c")));

    final ArrayList<MapAction<Integer, String>> results = ConnectorUtilities.pushData(
        streamlet, Arrays.asList(Pair.pair(1, "a"), Pair.pair(2, "NEW!"), Pair.pair(3, "c")));

    Assert.assertEquals(results.size(), 1);

    expectUpdate(results.get(0), (k, v, m) -> {
      Assert.assertEquals(k.intValue(), 2);
      Assert.assertEquals(v, "NEW!");
      Assert.assertEquals(m.size(), 3);
      Assert.assertEquals(m.get(1).get(), "a");
      Assert.assertEquals(m.get(2).get(), "NEW!");
      Assert.assertEquals(m.get(3).get(), "c");
    });
  }

  @Test(dataProvider = "withState")
  public void removeExistingEntry(final boolean withState) {
    if (withState) {
      final TrivialPersistenceProvider provider = new TrivialPersistenceProvider();
      final MapPersister<Integer, String> persister = provider.forMap(
          "state", Form.forInteger(), Form.forString());
      final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(n -> n, Object::toString, persister);

      removeExistingEntry(streamlet);

      Assert.assertEquals(persister.keys(), HashTrieSet.of(1, 3));
      Assert.assertEquals(persister.get(1), "1");
      Assert.assertEquals(persister.get(3), "3");
    } else {
      final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet =
          new CollectionToMapStreamlet<>(n -> n, Object::toString, Form.forString());

      removeExistingEntry(streamlet);
    }

  }

  private void removeExistingEntry(final CollectionToMapStreamlet<Integer, Set<Integer>, Integer, String> streamlet) {
    ConnectorUtilities.pushData(
        streamlet, HashTrieSet.of(1, 2, 3));

    final ArrayList<MapAction<Integer, String>> results = ConnectorUtilities.pushData(
        streamlet, HashTrieSet.of(1, 3));

    Assert.assertEquals(results.size(), 1);

    expectRemoval(results.get(0), (k, m) -> {
      Assert.assertEquals(k.intValue(), 2);
      Assert.assertEquals(m.size(), 2);
      Assert.assertEquals(m.get(1).get(), "1");
      Assert.assertEquals(m.get(3).get(), "3");
    });
  }

}
