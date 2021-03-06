// Copyright 2015-2020 SWIM.AI inc.
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

import {Random, Objects} from "@swim/util";
import {STreePage} from "./STreePage";

/** @hidden */
export abstract class STreeContext<V, I> {
  pageSplitSize: number;
 
  identify(value: V): I {
    const id = new Uint8Array(6);
    Random.fillBytes(id);
    return id as unknown as I;
  }

  compare(x: I, y: I): number {
    return Objects.compare(x, y);
  }

  pageShouldSplit(page: STreePage<V, I>): boolean {
    return page.arity > this.pageSplitSize;
  }

  pageShouldMerge(page: STreePage<V, I>): boolean {
    return page.arity < this.pageSplitSize >>> 1;
  }
}
STreeContext.prototype.pageSplitSize = 32;
