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

package swim.dataflow.graph.persistence;

import swim.structure.Form;
import swim.structure.Item;
import swim.structure.Record;
import swim.util.Unit;

public final class UnitForm extends Form<Unit> {
  private UnitForm() { }

  @Override
  public Class<?> type() {
    return Unit.class;
  }

  @Override
  public Item mold(final Unit object) {
    return Record.of();
  }

  @Override
  public Unit cast(final Item item) {
    return Unit.INSTANCE;
  }

  public static final Form<Unit> INSTANCE = new UnitForm();

}
